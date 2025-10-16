// src/main/java/at/htlle/freq/infrastructure/lucene/LuceneIndexServiceImpl.java
package at.htlle.freq.infrastructure.lucene;

import at.htlle.freq.infrastructure.search.SearchHit;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class LuceneIndexServiceImpl implements LuceneIndexService {
    private static final Logger log = LoggerFactory.getLogger(LuceneIndexServiceImpl.class);

    private final Directory directory;
    private final StandardAnalyzer analyzer = new StandardAnalyzer();

    public LuceneIndexServiceImpl() {
        try {
            Path indexPath = Paths.get("lucene-index"); // Ordner für Indexdaten
            this.directory = FSDirectory.open(indexPath);
        } catch (IOException e) {
            throw new RuntimeException("Fehler beim Initialisieren von Lucene FSDirectory", e);
        }
    }

    private IndexWriter getWriter(boolean create) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        if (create) {
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        } else {
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        }
        return new IndexWriter(directory, config);
    }

    @Override
    public void reindexAll() {
        try (IndexWriter writer = getWriter(true)) {
            writer.deleteAll();
            writer.commit();
            log.info("Lucene Index geleert (bereit für Reindex).");
        } catch (IOException e) {
            log.error("Fehler beim Reindexieren", e);
        }
    }

    @Override
    public List<SearchHit> search(String query) {
        try {
            QueryParser parser = new QueryParser("content", analyzer);
            Query luceneQuery = parser.parse(query);
            return search(luceneQuery);
        } catch (Exception e) {
            log.error("Fehler beim Parsen der Query", e);
            return List.of();
        }
    }

    @Override
    public List<SearchHit> search(Query query) {
        List<SearchHit> results = new ArrayList<>();
        try (DirectoryReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs topDocs = searcher.search(query, 20);
            for (ScoreDoc sd : topDocs.scoreDocs) {
                Document doc = searcher.doc(sd.doc);
                results.add(new SearchHit(
                        doc.get("id"),
                        doc.get("type"),
                        doc.get("content"),   // name
                        doc.get("content")    // snippet
                ));
            }

        } catch (IOException e) {
            log.error("Fehler bei der Suche", e);
        }
        return results;
    }

    // ========== Hilfsmethode ==========
    private void indexDocument(String id, String type, String content) {
        try (IndexWriter writer = getWriter(false)) {
            Document doc = new Document();
            doc.add(new StringField("id", id != null ? id : "", Field.Store.YES));
            doc.add(new StringField("type", type, Field.Store.YES));
            doc.add(new TextField("content", content != null ? content : "", Field.Store.YES));
            writer.updateDocument(new Term("id", id), doc);
            writer.commit();
            log.info("{} indexiert: {}", type, id);
        } catch (IOException e) {
            log.error("Fehler beim Indexieren von {}", type, e);
        }
    }

    // ========== Alle Entities ==========
    @Override public void indexAccount(String id, String name, String country, String email) {
        indexDocument(id, "account", String.join(" ", safe(name), safe(country), safe(email)));
    }

    @Override public void indexDeploymentVariant(String id, String code, String name, boolean active) {
        indexDocument(id, "deploymentVariant", String.join(" ", safe(code), safe(name), String.valueOf(active)));
    }

    @Override public void indexProject(String id, String name, String sapId, String accountId, String deploymentVariantId, boolean active) {
        indexDocument(id, "project", String.join(" ", safe(name), safe(sapId), safe(accountId), safe(deploymentVariantId), String.valueOf(active)));
    }

    @Override public void indexAddress(String id, String street, String cityId) {
        indexDocument(id, "address", String.join(" ", safe(street), safe(cityId)));
    }

    @Override public void indexCity(String id, String name, String countryCode) {
        indexDocument(id, "city", String.join(" ", safe(name), safe(countryCode)));
    }

    @Override public void indexCountry(String code, String name) {
        indexDocument(code, "country", String.join(" ", safe(name)));
    }

    @Override public void indexSite(String id, String name, String projectId, String addressId, String fireZone, Integer tenantCount) {
        indexDocument(id, "site", String.join(" ", safe(name), safe(projectId), safe(addressId), safe(fireZone), tenantCount != null ? tenantCount.toString() : ""));
    }

    @Override public void indexServer(String id, String siteId, String name, String brand, String os, String virtualPlatform, boolean ha) {
        indexDocument(id, "server", String.join(" ", safe(siteId), safe(name), safe(brand), safe(os), safe(virtualPlatform), String.valueOf(ha)));
    }

    @Override public void indexClient(String id, String siteId, String name, String brand, String os, String installType) {
        indexDocument(id, "client", String.join(" ", safe(siteId), safe(name), safe(brand), safe(os), safe(installType)));
    }

    @Override public void indexRadio(String id, String siteId, String assignedClientId, String brand, String mode, String digitalStandard) {
        indexDocument(id, "radio", String.join(" ", safe(siteId), safe(assignedClientId), safe(brand), safe(mode), safe(digitalStandard)));
    }

    @Override public void indexAudioDevice(String id, String clientId, String brand, String deviceType) {
        indexDocument(id, "audioDevice", String.join(" ", safe(clientId), safe(brand), safe(deviceType)));
    }

    @Override public void indexPhoneIntegration(String id, String clientId, String type, String brand) {
        indexDocument(id, "phoneIntegration", String.join(" ", safe(clientId), safe(type), safe(brand)));
    }

    @Override public void indexSoftware(String id, String name, String release, String revision, String supportPhase) {
        indexDocument(id, "software", String.join(" ", safe(name), safe(release), safe(revision), safe(supportPhase)));
    }

    @Override public void indexInstalledSoftware(String id, String siteId, String softwareId) {
        indexDocument(id, "installedSoftware", String.join(" ", safe(siteId), safe(softwareId)));
    }

    @Override public void indexUpgradePlan(String id, String siteId, String softwareId, String status, String windowStart, String windowEnd) {
        indexDocument(id, "upgradePlan", String.join(" ", safe(siteId), safe(softwareId), safe(status), safe(windowStart), safe(windowEnd)));
    }

    @Override public void indexServiceContract(String id, String accountId, String projectId, String siteId, String contractNumber, String status) {
        indexDocument(id, "serviceContract", String.join(" ", safe(accountId), safe(projectId), safe(siteId), safe(contractNumber), safe(status)));
    }

    // kleine Helper-Methode
    private String safe(String s) {
        return s == null ? "" : s;
    }
}
