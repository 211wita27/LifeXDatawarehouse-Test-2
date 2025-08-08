package at.htlle.freq.infrastructure.lucene;

import at.htlle.freq.domain.SearchHit;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LuceneIndexService {

    public static final Path INDEX_PATH = Path.of("lucene-index");

    private static final Logger log = LoggerFactory.getLogger(LuceneIndexService.class);

    private final JdbcTemplate jdbc;
    private final StandardAnalyzer analyzer = new StandardAnalyzer();
    private IndexWriter writer;

    // optionales (no-op) Fortschrittsobjekt
    private final IndexProgress progress = IndexProgress.get();

    public LuceneIndexService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /* ───────────── Init / Shutdown ───────────── */

    @PostConstruct
    public void init() throws IOException {
        FSDirectory dir = FSDirectory.open(INDEX_PATH);
        IndexWriterConfig cfg = new IndexWriterConfig(analyzer);
        writer = new IndexWriter(dir, cfg);
        writer.deleteAll();
        writer.commit();
    }

    @PreDestroy
    public void close() {
        try {
            if (writer != null) writer.close();
        } catch (IOException e) {
            log.warn("Closing Lucene writer failed", e);
        }
    }

    /* ───────────── Index Helpers ───────────── */

    private Document baseDoc(String type, int id) {
        Document d = new Document();
        d.add(new StringField("type", type, Field.Store.YES));
        d.add(new IntPoint("entityId", id));
        d.add(new StoredField("entityId", id));
        d.add(new StringField("typeId", type + "#" + id, Field.Store.NO));
        return d;
    }

    private void write(Document doc) {
        try {
            writer.updateDocument(new Term("typeId", doc.get("typeId")), doc);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write Lucene document " + doc, e);
        }
    }

    /* ───────────── Per-Entity Indexing ───────────── */

    public void indexAccount(int id, String name, String country, String email) {
        Document d = baseDoc("account", id);
        d.add(new TextField("txt", name, Field.Store.YES));
        d.add(new TextField("country", safeLower(country), Field.Store.YES));
        d.add(new TextField("email", safeLower(email), Field.Store.NO));
        write(d);
    }

    public void indexProject(int id, String name, String variant, String sap) {
        Document d = baseDoc("project", id);
        d.add(new TextField("txt", name, Field.Store.YES));
        d.add(new TextField("variant", safeLower(variant), Field.Store.YES));
        d.add(new TextField("sap", safeLower(sap), Field.Store.YES));
        write(d);
    }

    public void indexSite(int id, String name, String fireZone) {
        Document d = baseDoc("site", id);
        d.add(new TextField("txt", name, Field.Store.YES));
        d.add(new TextField("fireZone", safeLower(fireZone), Field.Store.YES));
        write(d);
    }

    public void indexServer(int id, String name, String brand, String os, String vplat) {
        Document d = baseDoc("server", id);
        d.add(new TextField("txt", name, Field.Store.YES));
        d.add(new TextField("brand", safeLower(brand), Field.Store.YES));
        d.add(new TextField("os", safeLower(os), Field.Store.YES));
        d.add(new TextField("vplat", safeLower(vplat), Field.Store.YES));
        write(d);
    }

    public void indexClient(int id, String name, String brand, String os) {
        Document d = baseDoc("client", id);
        d.add(new TextField("txt", name, Field.Store.YES));
        d.add(new TextField("brand", safeLower(brand), Field.Store.YES));
        d.add(new TextField("os", safeLower(os), Field.Store.YES));
        write(d);
    }

    public void indexRadio(int id, String brand, String mode, String digitalStd) {
        Document d = baseDoc("radio", id);
        d.add(new TextField("txt", brand, Field.Store.YES));
        d.add(new TextField("brand", safeLower(brand), Field.Store.YES));
        d.add(new TextField("mode", safeLower(mode), Field.Store.YES));
        if (digitalStd != null) d.add(new TextField("digitalStandard", safeLower(digitalStd), Field.Store.YES));
        write(d);
    }

    public void indexAudioDevice(int id, String brand, String firmware, String direction) {
        Document d = baseDoc("audio", id);
        d.add(new TextField("txt", brand, Field.Store.YES));
        d.add(new TextField("brand", safeLower(brand), Field.Store.YES));
        if (firmware != null) d.add(new TextField("firmware", safeLower(firmware), Field.Store.NO));
        d.add(new TextField("direction", safeLower(direction), Field.Store.YES));
        write(d);
    }

    public void indexPhoneIntegration(int id, String type, String brand) {
        Document d = baseDoc("phone", id);
        d.add(new TextField("txt", brand, Field.Store.YES));
        d.add(new TextField("brand", safeLower(brand), Field.Store.YES));
        d.add(new TextField("phoneType", safeLower(type), Field.Store.YES));
        write(d);
    }

    private static String safeLower(String s) { return s == null ? "" : s.toLowerCase(); }

    /* ───────────── Full Reindex with progress ───────────── */

    public void reindexAll() {
        Map<String, Integer> totals = new HashMap<>();
        totals.put("Account",         jdbc.queryForObject("SELECT COUNT(*) FROM Account", Integer.class));
        totals.put("Project",         jdbc.queryForObject("SELECT COUNT(*) FROM Project", Integer.class));
        totals.put("Site",            jdbc.queryForObject("SELECT COUNT(*) FROM Site", Integer.class));
        totals.put("Server",          jdbc.queryForObject("SELECT COUNT(*) FROM Server", Integer.class));
        totals.put("WorkingPosition", jdbc.queryForObject("SELECT COUNT(*) FROM WorkingPosition", Integer.class));
        totals.put("Radio",           jdbc.queryForObject("SELECT COUNT(*) FROM Radio", Integer.class));
        totals.put("AudioDevice",     jdbc.queryForObject("SELECT COUNT(*) FROM AudioDevice", Integer.class));
        totals.put("PhoneIntegration",jdbc.queryForObject("SELECT COUNT(*) FROM PhoneIntegration", Integer.class));
        int grandTotal = totals.values().stream().mapToInt(Integer::intValue).sum();
        progress.start(totals);

        long t0 = System.nanoTime();
        final int[] lastLoggedPercent = { -1 }; // effektiv final für Lambda

        try {
            writer.deleteAll();
            writer.commit();
        } catch (IOException e) {
            log.error("Clearing Lucene index failed", e);
            throw new RuntimeException("Clearing Lucene index failed", e);
        }

        jdbc.query("""
                SELECT AccountID, AccountName, Country, ContactEmail
                FROM Account
                """, rs -> {
            while (rs.next()) {
                indexAccount(
                        rs.getInt("AccountID"),
                        rs.getString("AccountName"),
                        rs.getString("Country"),
                        rs.getString("ContactEmail")
                );
                progress.inc("Account");
                lastLoggedPercent[0] = logMaybe(lastLoggedPercent[0], grandTotal, t0);
            }
        });

        jdbc.query("""
                SELECT ProjectID, ProjectName, DeploymentVariant, ProjectSAPID
                FROM Project
                """, rs -> {
            while (rs.next()) {
                indexProject(
                        rs.getInt("ProjectID"),
                        rs.getString("ProjectName"),
                        rs.getString("DeploymentVariant"),
                        rs.getString("ProjectSAPID")
                );
                progress.inc("Project");
                lastLoggedPercent[0] = logMaybe(lastLoggedPercent[0], grandTotal, t0);
            }
        });

        jdbc.query("""
                SELECT SiteID, SiteName, FireZone
                FROM Site
                """, rs -> {
            while (rs.next()) {
                indexSite(
                        rs.getInt("SiteID"),
                        rs.getString("SiteName"),
                        rs.getString("FireZone")
                );
                progress.inc("Site");
                lastLoggedPercent[0] = logMaybe(lastLoggedPercent[0], grandTotal, t0);
            }
        });

        jdbc.query("""
                SELECT ServerID, ServerName, ServerBrand, ServerOS, VirtualPlatform
                FROM Server
                """, rs -> {
            while (rs.next()) {
                indexServer(
                        rs.getInt("ServerID"),
                        rs.getString("ServerName"),
                        rs.getString("ServerBrand"),
                        rs.getString("ServerOS"),
                        rs.getString("VirtualPlatform")
                );
                progress.inc("Server");
                lastLoggedPercent[0] = logMaybe(lastLoggedPercent[0], grandTotal, t0);
            }
        });

        jdbc.query("""
                SELECT ClientID, ClientName, ClientBrand, ClientOS
                FROM WorkingPosition
                """, rs -> {
            while (rs.next()) {
                indexClient(
                        rs.getInt("ClientID"),
                        rs.getString("ClientName"),
                        rs.getString("ClientBrand"),
                        rs.getString("ClientOS")
                );
                progress.inc("WorkingPosition");
                lastLoggedPercent[0] = logMaybe(lastLoggedPercent[0], grandTotal, t0);
            }
        });

        jdbc.query("""
                SELECT RadioID, RadioBrand, Mode, DigitalStandard
                FROM Radio
                """, rs -> {
            while (rs.next()) {
                indexRadio(
                        rs.getInt("RadioID"),
                        rs.getString("RadioBrand"),
                        rs.getString("Mode"),
                        rs.getString("DigitalStandard")
                );
                progress.inc("Radio");
                lastLoggedPercent[0] = logMaybe(lastLoggedPercent[0], grandTotal, t0);
            }
        });

        jdbc.query("""
                SELECT AudioDeviceID, AudioDeviceBrand, AudioDeviceFirmware, Direction
                FROM AudioDevice
                """, rs -> {
            while (rs.next()) {
                indexAudioDevice(
                        rs.getInt("AudioDeviceID"),
                        rs.getString("AudioDeviceBrand"),
                        rs.getString("AudioDeviceFirmware"),
                        rs.getString("Direction")
                );
                progress.inc("AudioDevice");
                lastLoggedPercent[0] = logMaybe(lastLoggedPercent[0], grandTotal, t0);
            }
        });

        jdbc.query("""
                SELECT PhoneIntegrationID, PhoneType, PhoneBrand
                FROM PhoneIntegration
                """, rs -> {
            while (rs.next()) {
                indexPhoneIntegration(
                        rs.getInt("PhoneIntegrationID"),
                        rs.getString("PhoneType"),
                        rs.getString("PhoneBrand")
                );
                progress.inc("PhoneIntegration");
                lastLoggedPercent[0] = logMaybe(lastLoggedPercent[0], grandTotal, t0);
            }
        });

        try {
            writer.commit();
        } catch (IOException e) {
            log.error("Commit Lucene index failed", e);
            throw new RuntimeException("Commit Lucene index failed", e);
        }

        progress.finish();
        log.info("Lucene reindex finished ({} docs) in {} ms",
                grandTotal, (System.nanoTime() - t0) / 1_000_000);
    }

    private int logMaybe(int lastPercent, int grandTotal, long t0) {
        int done = progress.totalDone();
        int pct  = (grandTotal == 0) ? 100 : (done * 100 / grandTotal);
        if (pct != lastPercent && pct % 5 == 0) {
            long elapsedMs = (System.nanoTime() - t0) / 1_000_000;
            log.info("Lucene reindex: {}% ({}/{}) – {} ms", pct, done, grandTotal, elapsedMs);
            return pct;
        }
        return lastPercent;
    }

    /* ───────────── Search ───────────── */

    private static final String[] SEARCH_FIELDS =
            {"txt","type","country","variant","fireZone","os","brand","vplat",
                    "sap","email","mode","digitalStandard","direction","phoneType"};

    // Overload für Lucene-Query-Objekte (Controller ruft dies auf)
    public List<SearchHit> search(Query query) {
        List<SearchHit> hits = new ArrayList<>();
        try (DirectoryReader rd = DirectoryReader.open(writer)) {
            IndexSearcher s = new IndexSearcher(rd);
            TopDocs top = s.search(query, 50);
            for (ScoreDoc sd : top.scoreDocs) {
                Document d = s.doc(sd.doc);
                hits.add(new SearchHit(
                        d.get("type"),
                        d.getField("entityId").numericValue().intValue(),
                        d.get("txt")
                ));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return hits;
    }

    // Bestehende String-Variante: parsen und delegieren
    public List<SearchHit> search(String q) {
        try {
            Query query = new MultiFieldQueryParser(SEARCH_FIELDS, analyzer).parse(q);
            return search(query);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
