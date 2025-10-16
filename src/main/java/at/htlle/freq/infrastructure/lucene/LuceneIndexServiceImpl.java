package at.htlle.freq.infrastructure.lucene;

import at.htlle.freq.infrastructure.search.SearchHit;
import jakarta.annotation.PreDestroy;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class LuceneIndexServiceImpl implements LuceneIndexService {

    private static final Logger log = LoggerFactory.getLogger(LuceneIndexServiceImpl.class);
    private static final String INDEX_PATH = "target/lifex-index";

    private final StandardAnalyzer analyzer = new StandardAnalyzer();
    private IndexWriter writer;
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    // =================== Setup ===================

    private void ensureWriter() {
        try {
            if (initialized.compareAndSet(false, true)) {
                Path path = Paths.get(INDEX_PATH);
                FSDirectory dir = FSDirectory.open(path);
                IndexWriterConfig config = new IndexWriterConfig(analyzer);
                config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
                this.writer = new IndexWriter(dir, config);
                log.info("Lucene writer initialized at {}", path.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error initializing Lucene writer", e);
        }
    }

    @PreDestroy
    public void close() {
        try {
            if (writer != null) {
                writer.close();
                log.info("Lucene writer closed.");
            }
        } catch (IOException e) {
            log.warn("Error closing Lucene writer", e);
        }
    }

    // =================== Search ===================

    @Override
    public List<SearchHit> search(String queryText) {
        try {
            QueryParser parser = new QueryParser("content", analyzer);
            Query query = parser.parse(queryText);
            return search(query);
        } catch (Exception e) {
            log.error("Fehler beim Parsen der Suchanfrage: {}", queryText, e);
            return List.of();
        }
    }

    @Override
    public List<SearchHit> search(Query query) {
        List<SearchHit> results = new ArrayList<>();
        try {
            ensureWriter();
            try (DirectoryReader reader = DirectoryReader.open(writer)) {
                IndexSearcher searcher = new IndexSearcher(reader);
                TopDocs topDocs = searcher.search(query, 50);

                for (ScoreDoc sd : topDocs.scoreDocs) {
                    Document doc = searcher.doc(sd.doc);
                    results.add(new SearchHit(
                            doc.get("id"),
                            doc.get("type"),
                            doc.get("content"),
                            doc.get("content")
                    ));
                }
            }
        } catch (Exception e) {
            log.error("Fehler bei der Suche", e);
        }
        return results;
    }

    // =================== Reindex ===================

    @Override
    public void reindexAll() {
        try {
            ensureWriter();
            writer.deleteAll();
            writer.commit();
            log.info("Lucene-Index geleert (bereit für Reindex).");
        } catch (Exception e) {
            log.error("Fehler beim Reindexieren", e);
        }
    }

    // =================== Helper ===================

    private void indexDocument(String id, String type, String... fields) {
        try {
            ensureWriter();
            Document doc = new Document();
            doc.add(new StringField("id", safe(id), Field.Store.YES));
            doc.add(new StringField("type", safe(type), Field.Store.YES));

            StringBuilder content = new StringBuilder();
            for (String f : fields) {
                content.append(safe(f)).append(" ");
            }
            doc.add(new TextField("content", content.toString().trim(), Field.Store.YES));

            writer.updateDocument(new Term("id", id), doc);
            writer.commit();
            log.info("{} indexiert: {}", type, id);
        } catch (Exception e) {
            log.error("Fehler beim Indexieren von {}", type, e);
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    // =================== Indexing Methods ===================

    @Override
    public void indexAccount(String accountId, String accountName, String country, String contactEmail) {
        indexDocument(accountId, "account", accountName, country, contactEmail);
    }

    @Override
    public void indexAddress(String addressId, String street, String cityId) {
        indexDocument(addressId, "address", street, cityId);
    }

    @Override
    public void indexCity(String cityId, String cityName, String countryCode) {
        indexDocument(cityId, "city", cityName, countryCode);
    }

    @Override
    public void indexClient(String clientId, String siteId, String clientName, String clientBrand, String clientOS, String installType) {
        indexDocument(clientId, "client", siteId, clientName, clientBrand, clientOS, installType);
    }

    @Override
    public void indexCountry(String countryCode, String countryName) {
        indexDocument(countryCode, "country", countryName);
    }

    @Override
    public void indexAudioDevice(String audioDeviceId, String clientId, String brand, String serialNr, String firmware, String deviceType) {
        indexDocument(audioDeviceId, "audioDevice", clientId, brand, serialNr, firmware, deviceType);
    }

    @Override
    public void indexDeploymentVariant(String variantId, String variantCode, String variantName, String description, boolean active) {
        indexDocument(variantId, "deploymentVariant", variantCode, variantName, description, String.valueOf(active));
    }

    @Override
    public void indexInstalledSoftware(String installedSoftwareId, String siteId, String softwareId) {
        indexDocument(installedSoftwareId, "installedSoftware", siteId, softwareId);
    }

    @Override
    public void indexPhoneIntegration(String phoneIntegrationId, String clientId, String phoneType, String phoneBrand, String phoneSerialNr, String phoneFirmware) {
        indexDocument(phoneIntegrationId, "phoneIntegration", clientId, phoneType, phoneBrand, phoneSerialNr, phoneFirmware);
    }

    @Override
    public void indexProject(String projectId, String projectSAPId, String projectName, String deploymentVariantId, String bundleType, boolean stillActive,
                             String accountId, String addressId) {
        indexDocument(projectId, "project", projectSAPId, projectName, deploymentVariantId, bundleType, String.valueOf(stillActive), accountId, addressId);
    }

    @Override
    public void indexRadio(String radioId, String siteId, String assignedClientId, String radioBrand, String radioSerialNr, String mode, String digitalStandard) {
        indexDocument(radioId, "radio", siteId, assignedClientId, radioBrand, radioSerialNr, mode, digitalStandard);
    }

    @Override
    public void indexServer(String serverId, String siteId, String serverName, String serverBrand, String serverSerialNr, String serverOS,
                            String patchLevel, String virtualPlatform, String virtualVersion, boolean highAvailability) {
        indexDocument(serverId, "server", siteId, serverName, serverBrand, serverSerialNr, serverOS, patchLevel, virtualPlatform, virtualVersion, String.valueOf(highAvailability));
    }

    @Override
    public void indexServiceContract(String contractId, String accountId, String projectId, String siteId, String contractNumber, String status,
                                     String startDate, String endDate) {
        indexDocument(contractId, "serviceContract", accountId, projectId, siteId, contractNumber, status, startDate, endDate);
    }

    @Override
    public void indexSite(String siteId, String projectId, String addressId, String siteName, String fireZone, Integer tenantCount) {
        indexDocument(siteId, "site", projectId, addressId, siteName, fireZone,
                tenantCount != null ? tenantCount.toString() : "");
    }

    @Override
    public void indexSoftware(String softwareId, String name, String release, String revision, String supportPhase,
                              String licenseModel, String endOfSalesDate, String supportStartDate, String supportEndDate) {
        indexDocument(softwareId, "software", name, release, revision, supportPhase, licenseModel, endOfSalesDate, supportStartDate, supportEndDate);
    }

    @Override
    public void indexUpgradePlan(String upgradePlanId, String siteId, String softwareId, String plannedWindowStart, String plannedWindowEnd,
                                 String status, String createdAt, String createdBy) {
        indexDocument(upgradePlanId, "upgradePlan", siteId, softwareId, plannedWindowStart, plannedWindowEnd, status, createdAt, createdBy);
    }
}
