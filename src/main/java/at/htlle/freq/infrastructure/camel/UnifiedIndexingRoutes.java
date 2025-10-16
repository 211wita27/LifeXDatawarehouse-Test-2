// src/main/java/at/htlle/freq/infrastructure/camel/UnifiedIndexingRoutes.java
package at.htlle.freq.infrastructure.camel;

import at.htlle.freq.domain.*;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Produzenten für ALLE Entities:
 *  - Timer-Reindex schickt Datensätze in eine gemeinsame SEDA-Queue
 *  - Single-Index-Endpunkte (direct:...) leiten ebenfalls in die Queue
 *
 *  Dadurch greifen nicht mehr mehrere Writer parallel auf Lucene zu.
 *  Der eigentliche Index-Schreibzugriff passiert NUR im LuceneIndexingHubRoute
 *  (ein Consumer, serialisiert).
 */
@Component("UnifiedIndexingRoutes")
public class UnifiedIndexingRoutes extends RouteBuilder {

    // --- Repositories: alle als Dependencies einsammeln ---
    private final AccountRepository accountRepo;
    private final AddressRepository addressRepo;
    private final AudioDeviceRepository audioDeviceRepo;
    private final CityRepository cityRepo;
    private final ClientsRepository clientsRepo;
    private final CountryRepository countryRepo;
    private final DeploymentVariantRepository deploymentVariantRepo;
    private final InstalledSoftwareRepository installedSoftwareRepo;
    private final PhoneIntegrationRepository phoneIntegrationRepo;
    private final ProjectRepository projectRepo;
    private final RadioRepository radioRepo;
    private final ServerRepository serverRepo;
    private final ServiceContractRepository serviceContractRepo;
    private final SiteRepository siteRepo;
    private final SoftwareRepository softwareRepo;
    private final UpgradePlanRepository upgradePlanRepo;

    public UnifiedIndexingRoutes(AccountRepository accountRepo,
                                 AddressRepository addressRepo,
                                 AudioDeviceRepository audioDeviceRepo,
                                 CityRepository cityRepo,
                                 ClientsRepository clientsRepo,
                                 CountryRepository countryRepo,
                                 DeploymentVariantRepository deploymentVariantRepo,
                                 InstalledSoftwareRepository installedSoftwareRepo,
                                 PhoneIntegrationRepository phoneIntegrationRepo,
                                 ProjectRepository projectRepo,
                                 RadioRepository radioRepo,
                                 ServerRepository serverRepo,
                                 ServiceContractRepository serviceContractRepo,
                                 SiteRepository siteRepo,
                                 SoftwareRepository softwareRepo,
                                 UpgradePlanRepository upgradePlanRepo) {
        this.accountRepo = accountRepo;
        this.addressRepo = addressRepo;
        this.audioDeviceRepo = audioDeviceRepo;
        this.cityRepo = cityRepo;
        this.clientsRepo = clientsRepo;
        this.countryRepo = countryRepo;
        this.deploymentVariantRepo = deploymentVariantRepo;
        this.installedSoftwareRepo = installedSoftwareRepo;
        this.phoneIntegrationRepo = phoneIntegrationRepo;
        this.projectRepo = projectRepo;
        this.radioRepo = radioRepo;
        this.serverRepo = serverRepo;
        this.serviceContractRepo = serviceContractRepo;
        this.siteRepo = siteRepo;
        this.softwareRepo = softwareRepo;
        this.upgradePlanRepo = upgradePlanRepo;
    }

    @Override
    public void configure() {

        // ===== Timer-Reindex für alle Entities → in gemeinsame SEDA-Queue =====
        from("timer://idxAccounts?period=60000").routeId("ReindexAccounts")
                .bean(accountRepo, "findAll").split(body()).streaming()
                .to("seda:lucene-index?size=2000&blockWhenFull=true");

        from("timer://idxAddresses?period=60000").routeId("ReindexAddresses")
                .bean(addressRepo, "findAll").split(body()).streaming()
                .to("seda:lucene-index?size=2000&blockWhenFull=true");

        from("timer://idxAudioDevices?period=60000").routeId("ReindexAudioDevices")
                .bean(audioDeviceRepo, "findAll").split(body()).streaming()
                .to("seda:lucene-index?size=2000&blockWhenFull=true");

        from("timer://idxCities?period=60000").routeId("ReindexCities")
                .bean(cityRepo, "findAll").split(body()).streaming()
                .to("seda:lucene-index?size=2000&blockWhenFull=true");

        from("timer://idxClients?period=60000").routeId("ReindexClients")
                .bean(clientsRepo, "findAll").split(body()).streaming()
                .to("seda:lucene-index?size=2000&blockWhenFull=true");

        from("timer://idxCountries?period=60000").routeId("ReindexCountries")
                .bean(countryRepo, "findAll").split(body()).streaming()
                .to("seda:lucene-index?size=2000&blockWhenFull=true");

        from("timer://idxDeploymentVariants?period=60000").routeId("ReindexDeploymentVariants")
                .bean(deploymentVariantRepo, "findAll").split(body()).streaming()
                .to("seda:lucene-index?size=2000&blockWhenFull=true");

        from("timer://idxInstalledSoftware?period=60000").routeId("ReindexInstalledSoftware")
                .bean(installedSoftwareRepo, "findAll").split(body()).streaming()
                .to("seda:lucene-index?size=2000&blockWhenFull=true");

        from("timer://idxPhoneIntegrations?period=60000").routeId("ReindexPhoneIntegrations")
                .bean(phoneIntegrationRepo, "findAll").split(body()).streaming()
                .to("seda:lucene-index?size=2000&blockWhenFull=true");

        from("timer://idxProjects?period=60000").routeId("ReindexProjects")
                .bean(projectRepo, "findAll").split(body()).streaming()
                .to("seda:lucene-index?size=2000&blockWhenFull=true");

        from("timer://idxRadios?period=60000").routeId("ReindexRadios")
                .bean(radioRepo, "findAll").split(body()).streaming()
                .to("seda:lucene-index?size=2000&blockWhenFull=true");

        from("timer://idxServers?period=60000").routeId("ReindexServers")
                .bean(serverRepo, "findAll").split(body()).streaming()
                .to("seda:lucene-index?size=2000&blockWhenFull=true");

        from("timer://idxServiceContracts?period=60000").routeId("ReindexServiceContracts")
                .bean(serviceContractRepo, "findAll").split(body()).streaming()
                .to("seda:lucene-index?size=2000&blockWhenFull=true");

        from("timer://idxSites?period=60000").routeId("ReindexSites")
                .bean(siteRepo, "findAll").split(body()).streaming()
                .to("seda:lucene-index?size=2000&blockWhenFull=true");

        from("timer://idxSoftware?period=60000").routeId("ReindexSoftware")
                .bean(softwareRepo, "findAll").split(body()).streaming()
                .to("seda:lucene-index?size=2000&blockWhenFull=true");

        from("timer://idxUpgradePlans?period=60000").routeId("ReindexUpgradePlans")
                .bean(upgradePlanRepo, "findAll").split(body()).streaming()
                .to("seda:lucene-index?size=2000&blockWhenFull=true");

        // ===== Single-Index-Endpoints → ebenfalls in die Queue =====
        from("direct:index-single-account").routeId("IndexSingleAccount").to("seda:lucene-index");
        from("direct:index-single-address").routeId("IndexSingleAddress").to("seda:lucene-index");
        from("direct:index-single-audioDevice").routeId("IndexSingleAudioDevice").to("seda:lucene-index");
        from("direct:index-single-city").routeId("IndexSingleCity").to("seda:lucene-index");
        from("direct:index-single-client").routeId("IndexSingleClient").to("seda:lucene-index");
        from("direct:index-single-country").routeId("IndexSingleCountry").to("seda:lucene-index");
        from("direct:index-single-deploymentVariant").routeId("IndexSingleDeploymentVariant").to("seda:lucene-index");
        from("direct:index-single-installedSoftware").routeId("IndexSingleInstalledSoftware").to("seda:lucene-index");
        from("direct:index-single-phoneIntegration").routeId("IndexSinglePhoneIntegration").to("seda:lucene-index");
        from("direct:index-single-project").routeId("IndexSingleProject").to("seda:lucene-index");
        from("direct:index-single-radio").routeId("IndexSingleRadio").to("seda:lucene-index");
        from("direct:index-single-server").routeId("IndexSingleServer").to("seda:lucene-index");
        from("direct:index-single-serviceContract").routeId("IndexSingleServiceContract").to("seda:lucene-index");
        from("direct:index-single-site").routeId("IndexSingleSite").to("seda:lucene-index");
        from("direct:index-single-software").routeId("IndexSingleSoftware").to("seda:lucene-index");
        from("direct:index-single-upgradePlan").routeId("IndexSingleUpgradePlan").to("seda:lucene-index");
    }
}
