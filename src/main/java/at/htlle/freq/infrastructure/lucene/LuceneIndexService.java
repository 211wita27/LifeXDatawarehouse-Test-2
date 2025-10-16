// src/main/java/at/htlle/freq/infrastructure/lucene/LuceneIndexService.java
package at.htlle.freq.infrastructure.lucene;


import at.htlle.freq.infrastructure.search.SearchHit;

import org.apache.lucene.search.Query;

import java.util.List;

public interface LuceneIndexService {

    // Wird u.a. in SuggestService benutzt
    String INDEX_PATH = "var/lucene";

    /* Admin */
    void reindexAll();

    /* Suche */
    List<SearchHit> search(String query);
    List<SearchHit> search(Query query);

    /* Index-Writer pro Entity */
    void indexAccount(String id, String name, String country, String email);
    void indexDeploymentVariant(String id, String code, String name, boolean active);
    void indexProject(String id, String name, String sapId, String accountId, String deploymentVariantId, boolean active);
    void indexAddress(String id, String street, String cityId);
    void indexCity(String id, String name, String countryCode);
    void indexCountry(String code, String name);
    void indexSite(String id, String name, String projectId, String addressId, String fireZone, Integer tenantCount);
    void indexServer(String id, String siteId, String name, String brand, String os, String virtualPlatform, boolean ha);
    void indexClient(String id, String siteId, String name, String brand, String os, String installType);
    void indexRadio(String id, String siteId, String assignedClientId, String brand, String mode, String digitalStandard);
    void indexAudioDevice(String id, String clientId, String brand, String deviceType);
    void indexPhoneIntegration(String id, String clientId, String type, String brand);
    void indexSoftware(String id, String name, String release, String revision, String supportPhase);
    void indexInstalledSoftware(String id, String siteId, String softwareId);
    void indexUpgradePlan(String id, String siteId, String softwareId, String status, String windowStart, String windowEnd);
    void indexServiceContract(String id, String accountId, String projectId, String siteId, String contractNumber, String status);
}
