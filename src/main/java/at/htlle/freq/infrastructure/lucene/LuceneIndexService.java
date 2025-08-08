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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LuceneIndexService {

    public static final Path INDEX_PATH = Path.of("lucene-index");
    private static final Logger log = LoggerFactory.getLogger(LuceneIndexService.class);

    private final JdbcTemplate jdbc;
    private final StandardAnalyzer analyzer = new StandardAnalyzer();
    private IndexWriter writer;

    // Fortschritt für UI
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

    /* ───────────── Helpers ───────────── */

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

    private static String safeLower(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    /* ───────────── Per-Entity Indexing (grundlegend) ───────────── */

    /** Account – Basisfelder (wird z. B. bei Einzelindex verwendet). */
    public void indexAccount(int id, String name, String country, String email) {
        Document d = baseDoc("account", id);
        d.add(new TextField("txt", name, Field.Store.YES));
        d.add(new TextField("country", safeLower(country), Field.Store.YES));
        d.add(new TextField("email", safeLower(email), Field.Store.NO));
        // eigene ID für konsistente Abfragen
        d.add(new StringField("accountId", String.valueOf(id), Field.Store.NO));
        write(d);
    }

    /* ───────────── Search ───────────── */

    // für Controller genutzte Felderliste – inkl. neuer denormalisierter Felder
    private static final String[] SEARCH_FIELDS = {
            "txt","type","country","variant","fireZone","os","brand","vplat",
            "sap","email","mode","digitalStandard","direction","phoneType",
            // NEU: Relationen & Rollups
            "accountId","projectId","siteId","clientId",
            "serverBrand","serverOS","serverVplat","hasServer"
    };

    // Overload für Lucene-Query-Objekte
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

    // String-Variante
    public List<SearchHit> search(String q) {
        try {
            Query query = new MultiFieldQueryParser(SEARCH_FIELDS, analyzer).parse(q);
            return search(query);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /* ───────────── Voll-Index mit Denormalisierung + Fortschritt ───────────── */

    // Kleine DTOs für das Zusammenbauen denormalisierter Felder
    private static class AccData {
        int id; String name, country, email;
        final Set<String> serverBrand = new LinkedHashSet<>();
        final Set<String> serverOS    = new LinkedHashSet<>();
        final Set<String> serverVplat = new LinkedHashSet<>();
        boolean hasServer;
    }
    private static class ProjData { int id; String name, variant, sap; int accountId; }
    private static class SiteData {
        int id; String name, fireZone; int projectId, accountId;
        final Set<String> serverBrand = new LinkedHashSet<>();
        final Set<String> serverOS    = new LinkedHashSet<>();
        final Set<String> serverVplat = new LinkedHashSet<>();
        boolean hasServer;
    }
    private static class ClientRef { int clientId, siteId, projectId, accountId; }

    public void reindexAll() {
        Map<String, Integer> totals = new LinkedHashMap<>();
        totals.put("Account",         jdbc.queryForObject("SELECT COUNT(*) FROM Account", Integer.class));
        totals.put("Project",         jdbc.queryForObject("SELECT COUNT(*) FROM Project", Integer.class));
        totals.put("Site",            jdbc.queryForObject("SELECT COUNT(*) FROM Site", Integer.class));
        totals.put("Server",          jdbc.queryForObject("SELECT COUNT(*) FROM Server", Integer.class));
        totals.put("WorkingPosition", jdbc.queryForObject("SELECT COUNT(*) FROM WorkingPosition", Integer.class));
        totals.put("Radio",           jdbc.queryForObject("SELECT COUNT(*) FROM Radio", Integer.class));
        totals.put("AudioDevice",     jdbc.queryForObject("SELECT COUNT(*) FROM AudioDevice", Integer.class));
        totals.put("PhoneIntegration",jdbc.queryForObject("SELECT COUNT(*) FROM PhoneIntegration", Integer.class));
        final int grandTotal = totals.values().stream().mapToInt(Integer::intValue).sum();

        progress.start(totals);
        long t0 = System.nanoTime();
        final AtomicInteger lastLoggedPercent = new AtomicInteger(-1);

        try {
            writer.deleteAll();
            writer.commit();
        } catch (IOException e) {
            log.error("Clearing Lucene index failed", e);
            throw new RuntimeException("Clearing Lucene index failed", e);
        }

        // ---------- 1) Stammdaten laden (Accounts/Projects/Sites) ----------
        Map<Integer, AccData>  accMap  = new HashMap<>();
        Map<Integer, ProjData> projMap = new HashMap<>();
        Map<Integer, SiteData> siteMap = new HashMap<>();

        jdbc.query("""
            SELECT AccountID, AccountName, Country, ContactEmail
            FROM Account
        """, rs -> {
            while (rs.next()) {
                AccData a = new AccData();
                a.id = rs.getInt("AccountID");
                a.name = rs.getString("AccountName");
                a.country = rs.getString("Country");
                a.email = rs.getString("ContactEmail");
                accMap.put(a.id, a);
            }
        });

        jdbc.query("""
            SELECT ProjectID, ProjectName, DeploymentVariant, ProjectSAPID, AccountID
            FROM Project
        """, rs -> {
            while (rs.next()) {
                ProjData p = new ProjData();
                p.id = rs.getInt("ProjectID");
                p.name = rs.getString("ProjectName");
                p.variant = rs.getString("DeploymentVariant");
                p.sap = rs.getString("ProjectSAPID");
                p.accountId = rs.getInt("AccountID");
                projMap.put(p.id, p);

                // Projekt-Dokument direkt schreiben (rollupfrei)
                Document d = baseDoc("project", p.id);
                d.add(new TextField("txt", p.name, Field.Store.YES));
                d.add(new TextField("variant", safeLower(p.variant), Field.Store.YES));
                d.add(new TextField("sap", safeLower(p.sap), Field.Store.YES));
                d.add(new StringField("projectId", String.valueOf(p.id), Field.Store.NO));
                d.add(new StringField("accountId", String.valueOf(p.accountId), Field.Store.NO));
                write(d);
                progress.inc("Project");
                lastLoggedPercent.set(logMaybe(lastLoggedPercent.get(), grandTotal, t0));
            }
        });

        jdbc.query("""
            SELECT s.SiteID, s.SiteName, s.FireZone, s.ProjectID, p.AccountID
            FROM Site s
            JOIN Project p ON p.ProjectID = s.ProjectID
        """, rs -> {
            while (rs.next()) {
                SiteData s = new SiteData();
                s.id = rs.getInt("SiteID");
                s.name = rs.getString("SiteName");
                s.fireZone = rs.getString("FireZone");
                s.projectId = rs.getInt("ProjectID");
                s.accountId = rs.getInt("AccountID");
                siteMap.put(s.id, s);
                // Sites schreiben wir später (nach Server-Pass) mit Rollups
            }
        });

        // ---------- 2) Server & abhängige Entitäten (füllen Rollups) ----------
        jdbc.query("""
            SELECT ServerID, ServerName, ServerBrand, ServerOS, VirtualPlatform, SiteID
            FROM Server
        """, rs -> {
            while (rs.next()) {
                int id   = rs.getInt("ServerID");
                String nm= rs.getString("ServerName");
                String br= rs.getString("ServerBrand");
                String os= rs.getString("ServerOS");
                String vp= rs.getString("VirtualPlatform");
                int siteId = rs.getInt("SiteID");

                SiteData s = siteMap.get(siteId);
                int projectId = (s != null ? s.projectId : 0);
                int accountId = (s != null ? s.accountId : 0);

                // Server-Dokument
                Document d = baseDoc("server", id);
                d.add(new TextField("txt", nm, Field.Store.YES));
                d.add(new TextField("brand", safeLower(br), Field.Store.YES));
                d.add(new TextField("os", safeLower(os), Field.Store.YES));
                d.add(new TextField("vplat", safeLower(vp), Field.Store.YES));
                d.add(new StringField("siteId", String.valueOf(siteId), Field.Store.NO));
                if (projectId != 0) d.add(new StringField("projectId", String.valueOf(projectId), Field.Store.NO));
                if (accountId != 0) d.add(new StringField("accountId", String.valueOf(accountId), Field.Store.NO));
                write(d);

                // Rollups
                if (s != null) {
                    s.hasServer = true;
                    if (br != null) s.serverBrand.add(safeLower(br));
                    if (os != null) s.serverOS.add(safeLower(os));
                    if (vp != null) s.serverVplat.add(safeLower(vp));
                }
                AccData a = accMap.get(accountId);
                if (a != null) {
                    a.hasServer = true;
                    if (br != null) a.serverBrand.add(safeLower(br));
                    if (os != null) a.serverOS.add(safeLower(os));
                    if (vp != null) a.serverVplat.add(safeLower(vp));
                }

                progress.inc("Server");
                lastLoggedPercent.set(logMaybe(lastLoggedPercent.get(), grandTotal, t0));
            }
        });

        // Clients (WorkingPosition)
        Map<Integer, ClientRef> clientMap = new HashMap<>();
        jdbc.query("""
            SELECT ClientID, ClientName, ClientBrand, ClientOS, SiteID
            FROM WorkingPosition
        """, rs -> {
            while (rs.next()) {
                int id     = rs.getInt("ClientID");
                String nm  = rs.getString("ClientName");
                String br  = rs.getString("ClientBrand");
                String os  = rs.getString("ClientOS");
                int siteId = rs.getInt("SiteID");

                SiteData s = siteMap.get(siteId);
                int projectId = (s != null ? s.projectId : 0);
                int accountId = (s != null ? s.accountId : 0);

                // Client-Dokument
                Document d = baseDoc("client", id);
                d.add(new TextField("txt", nm, Field.Store.YES));
                d.add(new TextField("brand", safeLower(br), Field.Store.YES));
                d.add(new TextField("os", safeLower(os), Field.Store.YES));
                d.add(new StringField("clientId", String.valueOf(id), Field.Store.NO));
                d.add(new StringField("siteId", String.valueOf(siteId), Field.Store.NO));
                if (projectId != 0) d.add(new StringField("projectId", String.valueOf(projectId), Field.Store.NO));
                if (accountId != 0) d.add(new StringField("accountId", String.valueOf(accountId), Field.Store.NO));
                write(d);

                ClientRef cr = new ClientRef();
                cr.clientId = id; cr.siteId = siteId; cr.projectId = projectId; cr.accountId = accountId;
                clientMap.put(id, cr);

                progress.inc("WorkingPosition");
                lastLoggedPercent.set(logMaybe(lastLoggedPercent.get(), grandTotal, t0));
            }
        });

        // Radios
        jdbc.query("""
            SELECT RadioID, RadioBrand, Mode, DigitalStandard, SiteID, AssignedClientID
            FROM Radio
        """, rs -> {
            while (rs.next()) {
                int id     = rs.getInt("RadioID");
                String br  = rs.getString("RadioBrand");
                String md  = rs.getString("Mode");
                String ds  = rs.getString("DigitalStandard");
                int siteId = rs.getInt("SiteID");
                Integer assignedClientId = (Integer) rs.getObject("AssignedClientID");

                SiteData s = siteMap.get(siteId);
                int projectId = (s != null ? s.projectId : 0);
                int accountId = (s != null ? s.accountId : 0);

                Document d = baseDoc("radio", id);
                d.add(new TextField("txt", br, Field.Store.YES));
                d.add(new TextField("brand", safeLower(br), Field.Store.YES));
                d.add(new TextField("mode", safeLower(md), Field.Store.YES));
                if (ds != null) d.add(new TextField("digitalStandard", safeLower(ds), Field.Store.YES));
                d.add(new StringField("siteId", String.valueOf(siteId), Field.Store.NO));
                if (assignedClientId != null) d.add(new StringField("clientId", String.valueOf(assignedClientId), Field.Store.NO));
                if (projectId != 0) d.add(new StringField("projectId", String.valueOf(projectId), Field.Store.NO));
                if (accountId != 0) d.add(new StringField("accountId", String.valueOf(accountId), Field.Store.NO));
                write(d);

                progress.inc("Radio");
                lastLoggedPercent.set(logMaybe(lastLoggedPercent.get(), grandTotal, t0));
            }
        });

        // Audio Devices
        jdbc.query("""
            SELECT AudioDeviceID, AudioDeviceBrand, AudioDeviceFirmware, Direction, ClientID
            FROM AudioDevice
        """, rs -> {
            while (rs.next()) {
                int id     = rs.getInt("AudioDeviceID");
                String br  = rs.getString("AudioDeviceBrand");
                String fw  = rs.getString("AudioDeviceFirmware");
                String dir = rs.getString("Direction");
                int clientId = rs.getInt("ClientID");

                ClientRef cr = clientMap.get(clientId);
                int siteId    = (cr != null ? cr.siteId : 0);
                int projectId = (cr != null ? cr.projectId : 0);
                int accountId = (cr != null ? cr.accountId : 0);

                Document d = baseDoc("audio", id);
                d.add(new TextField("txt", br, Field.Store.YES));
                d.add(new TextField("brand", safeLower(br), Field.Store.YES));
                if (fw != null) d.add(new TextField("firmware", safeLower(fw), Field.Store.NO));
                d.add(new TextField("direction", safeLower(dir), Field.Store.YES));
                d.add(new StringField("clientId", String.valueOf(clientId), Field.Store.NO));
                if (siteId != 0)    d.add(new StringField("siteId", String.valueOf(siteId), Field.Store.NO));
                if (projectId != 0) d.add(new StringField("projectId", String.valueOf(projectId), Field.Store.NO));
                if (accountId != 0) d.add(new StringField("accountId", String.valueOf(accountId), Field.Store.NO));
                write(d);

                progress.inc("AudioDevice");
                lastLoggedPercent.set(logMaybe(lastLoggedPercent.get(), grandTotal, t0));
            }
        });

        // Phone Integrations
        jdbc.query("""
            SELECT PhoneIntegrationID, PhoneType, PhoneBrand, ClientID
            FROM PhoneIntegration
        """, rs -> {
            while (rs.next()) {
                int id       = rs.getInt("PhoneIntegrationID");
                String type  = rs.getString("PhoneType");
                String br    = rs.getString("PhoneBrand");
                int clientId = rs.getInt("ClientID");

                ClientRef cr = clientMap.get(clientId);
                int siteId    = (cr != null ? cr.siteId : 0);
                int projectId = (cr != null ? cr.projectId : 0);
                int accountId = (cr != null ? cr.accountId : 0);

                Document d = baseDoc("phone", id);
                d.add(new TextField("txt", br, Field.Store.YES));
                d.add(new TextField("brand", safeLower(br), Field.Store.YES));
                d.add(new TextField("phoneType", safeLower(type), Field.Store.YES));
                d.add(new StringField("clientId", String.valueOf(clientId), Field.Store.NO));
                if (siteId != 0)    d.add(new StringField("siteId", String.valueOf(siteId), Field.Store.NO));
                if (projectId != 0) d.add(new StringField("projectId", String.valueOf(projectId), Field.Store.NO));
                if (accountId != 0) d.add(new StringField("accountId", String.valueOf(accountId), Field.Store.NO));
                write(d);

                progress.inc("PhoneIntegration");
                lastLoggedPercent.set(logMaybe(lastLoggedPercent.get(), grandTotal, t0));
            }
        });

        // ---------- 3) Sites & Accounts jetzt mit Rollups schreiben ----------
        for (SiteData s : siteMap.values()) {
            Document d = baseDoc("site", s.id);
            d.add(new TextField("txt", s.name, Field.Store.YES));
            d.add(new TextField("fireZone", safeLower(s.fireZone), Field.Store.YES));
            d.add(new StringField("siteId", String.valueOf(s.id), Field.Store.NO));
            d.add(new StringField("projectId", String.valueOf(s.projectId), Field.Store.NO));
            d.add(new StringField("accountId", String.valueOf(s.accountId), Field.Store.NO));
            // Rollups
            for (String v : s.serverBrand) d.add(new TextField("serverBrand", v, Field.Store.NO));
            for (String v : s.serverOS)    d.add(new TextField("serverOS",    v, Field.Store.NO));
            for (String v : s.serverVplat) d.add(new TextField("serverVplat", v, Field.Store.NO));
            if (s.hasServer) d.add(new StringField("hasServer", "true", Field.Store.NO));
            write(d);

            progress.inc("Site");
            lastLoggedPercent.set(logMaybe(lastLoggedPercent.get(), grandTotal, t0));
        }

        for (AccData a : accMap.values()) {
            Document d = baseDoc("account", a.id);
            d.add(new TextField("txt", a.name, Field.Store.YES));
            d.add(new TextField("country", safeLower(a.country), Field.Store.YES));
            d.add(new TextField("email", safeLower(a.email), Field.Store.NO));
            d.add(new StringField("accountId", String.valueOf(a.id), Field.Store.NO));
            // Rollups
            for (String v : a.serverBrand) d.add(new TextField("serverBrand", v, Field.Store.NO));
            for (String v : a.serverOS)    d.add(new TextField("serverOS",    v, Field.Store.NO));
            for (String v : a.serverVplat) d.add(new TextField("serverVplat", v, Field.Store.NO));
            if (a.hasServer) d.add(new StringField("hasServer", "true", Field.Store.NO));
            write(d);

            progress.inc("Account");
            lastLoggedPercent.set(logMaybe(lastLoggedPercent.get(), grandTotal, t0));
        }

        try {
            writer.commit();
        } catch (IOException e) {
            log.error("Commit Lucene index failed", e);
            throw new RuntimeException("Commit Lucene index failed", e);
        }

        progress.finish();
        log.info("Lucene reindex finished ({} docs) in {} ms", grandTotal, (System.nanoTime() - t0) / 1_000_000);
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
}