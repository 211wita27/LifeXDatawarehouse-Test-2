package at.htlle.freq.application;

import at.htlle.freq.domain.*;
import at.htlle.freq.infrastructure.lucene.IndexProgress;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Service
public class ReindexService {

    private static final Logger log = LoggerFactory.getLogger(ReindexService.class);

    private static final String KEY_ACCOUNTS = "Accounts";
    private static final String KEY_ADDRESSES = "Addresses";
    private static final String KEY_CITIES = "Cities";
    private static final String KEY_COUNTRIES = "Countries";
    private static final String KEY_CLIENTS = "Clients";
    private static final String KEY_AUDIO_DEVICES = "AudioDevices";
    private static final String KEY_DEPLOYMENT_VARIANTS = "DeploymentVariants";
    private static final String KEY_INSTALLED_SOFTWARE = "InstalledSoftware";
    private static final String KEY_PHONE_INTEGRATIONS = "PhoneIntegrations";
    private static final String KEY_PROJECTS = "Projects";
    private static final String KEY_RADIOS = "Radios";
    private static final String KEY_SERVERS = "Servers";
    private static final String KEY_SERVICE_CONTRACTS = "ServiceContracts";
    private static final String KEY_SITES = "Sites";
    private static final String KEY_SOFTWARE = "Software";
    private static final String KEY_UPGRADE_PLANS = "UpgradePlans";

    private final TaskExecutor taskExecutor;
    private final LuceneIndexService lucene;
    private final AccountRepository accountRepository;
    private final AddressRepository addressRepository;
    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;
    private final ClientsRepository clientsRepository;
    private final AudioDeviceRepository audioDeviceRepository;
    private final DeploymentVariantRepository deploymentVariantRepository;
    private final InstalledSoftwareRepository installedSoftwareRepository;
    private final PhoneIntegrationRepository phoneIntegrationRepository;
    private final ProjectRepository projectRepository;
    private final RadioRepository radioRepository;
    private final ServerRepository serverRepository;
    private final ServiceContractRepository serviceContractRepository;
    private final SiteRepository siteRepository;
    private final SoftwareRepository softwareRepository;
    private final UpgradePlanRepository upgradePlanRepository;

    private final AtomicBoolean running = new AtomicBoolean(false);

    public ReindexService(
            @Qualifier("applicationTaskExecutor") TaskExecutor taskExecutor,
            LuceneIndexService lucene,
            AccountRepository accountRepository,
            AddressRepository addressRepository,
            CityRepository cityRepository,
            CountryRepository countryRepository,
            ClientsRepository clientsRepository,
            AudioDeviceRepository audioDeviceRepository,
            DeploymentVariantRepository deploymentVariantRepository,
            InstalledSoftwareRepository installedSoftwareRepository,
            PhoneIntegrationRepository phoneIntegrationRepository,
            ProjectRepository projectRepository,
            RadioRepository radioRepository,
            ServerRepository serverRepository,
            ServiceContractRepository serviceContractRepository,
            SiteRepository siteRepository,
            SoftwareRepository softwareRepository,
            UpgradePlanRepository upgradePlanRepository
    ) {
        this.taskExecutor = taskExecutor;
        this.lucene = lucene;
        this.accountRepository = accountRepository;
        this.addressRepository = addressRepository;
        this.cityRepository = cityRepository;
        this.countryRepository = countryRepository;
        this.clientsRepository = clientsRepository;
        this.audioDeviceRepository = audioDeviceRepository;
        this.deploymentVariantRepository = deploymentVariantRepository;
        this.installedSoftwareRepository = installedSoftwareRepository;
        this.phoneIntegrationRepository = phoneIntegrationRepository;
        this.projectRepository = projectRepository;
        this.radioRepository = radioRepository;
        this.serverRepository = serverRepository;
        this.serviceContractRepository = serviceContractRepository;
        this.siteRepository = siteRepository;
        this.softwareRepository = softwareRepository;
        this.upgradePlanRepository = upgradePlanRepository;
    }

    /**
     * Startet einen Reindex-Lauf asynchron. Läuft bereits ein Durchlauf, wird kein weiterer gestartet.
     *
     * @return {@code true}, wenn ein neuer Lauf gestartet wurde.
     */
    public boolean triggerReindex() {
        if (!running.compareAndSet(false, true)) {
            log.info("Ein Reindex-Lauf ist bereits aktiv – neuer Lauf wird ignoriert.");
            return false;
        }

        taskExecutor.execute(() -> {
            try {
                doReindex();
            } finally {
                running.set(false);
            }
        });
        return true;
    }

    public boolean isRunning() {
        return running.get();
    }

    private void doReindex() {
        IndexProgress progress = IndexProgress.get();
        boolean progressStarted = false;
        try {
            List<Account> accounts = safe(accountRepository.findAll());
            List<Address> addresses = safe(addressRepository.findAll());
            List<City> cities = safe(cityRepository.findAll());
            List<Country> countries = safe(countryRepository.findAll());
            List<Clients> clients = safe(clientsRepository.findAll());
            List<AudioDevice> audioDevices = safe(audioDeviceRepository.findAll());
            List<DeploymentVariant> deploymentVariants = safe(deploymentVariantRepository.findAll());
            List<InstalledSoftware> installedSoftware = safe(installedSoftwareRepository.findAll());
            List<PhoneIntegration> phoneIntegrations = safe(phoneIntegrationRepository.findAll());
            List<Project> projects = safe(projectRepository.findAll());
            List<Radio> radios = safe(radioRepository.findAll());
            List<Server> servers = safe(serverRepository.findAll());
            List<ServiceContract> serviceContracts = safe(serviceContractRepository.findAll());
            List<Site> sites = safe(siteRepository.findAll());
            List<Software> software = safe(softwareRepository.findAll());
            List<UpgradePlan> upgradePlans = safe(upgradePlanRepository.findAll());

            Map<String, Integer> totals = new LinkedHashMap<>();
            totals.put(KEY_ACCOUNTS, accounts.size());
            totals.put(KEY_ADDRESSES, addresses.size());
            totals.put(KEY_CITIES, cities.size());
            totals.put(KEY_COUNTRIES, countries.size());
            totals.put(KEY_CLIENTS, clients.size());
            totals.put(KEY_AUDIO_DEVICES, audioDevices.size());
            totals.put(KEY_DEPLOYMENT_VARIANTS, deploymentVariants.size());
            totals.put(KEY_INSTALLED_SOFTWARE, installedSoftware.size());
            totals.put(KEY_PHONE_INTEGRATIONS, phoneIntegrations.size());
            totals.put(KEY_PROJECTS, projects.size());
            totals.put(KEY_RADIOS, radios.size());
            totals.put(KEY_SERVERS, servers.size());
            totals.put(KEY_SERVICE_CONTRACTS, serviceContracts.size());
            totals.put(KEY_SITES, sites.size());
            totals.put(KEY_SOFTWARE, software.size());
            totals.put(KEY_UPGRADE_PLANS, upgradePlans.size());

            progress.start(totals);
            progressStarted = true;
            log.info("Reindex gestartet ({} Datensätze)", progress.grandTotal());

            lucene.resetIndex();

            indexCollection(progress, KEY_ACCOUNTS, accounts, account ->
                    lucene.indexAccount(uuid(account.getAccountID()), account.getAccountName(),
                            account.getCountry(), account.getContactEmail()));

            indexCollection(progress, KEY_ADDRESSES, addresses, address ->
                    lucene.indexAddress(uuid(address.getAddressID()), address.getStreet(), address.getCityID()));

            indexCollection(progress, KEY_CITIES, cities, city ->
                    lucene.indexCity(city.getCityID(), city.getCityName(), city.getCountryCode()));

            indexCollection(progress, KEY_COUNTRIES, countries, country ->
                    lucene.indexCountry(country.getCountryCode(), country.getCountryName()));

            indexCollection(progress, KEY_CLIENTS, clients, client ->
                    lucene.indexClient(uuid(client.getClientID()), uuid(client.getSiteID()), client.getClientName(),
                            client.getClientBrand(), client.getClientOS(), client.getInstallType()));

            indexCollection(progress, KEY_AUDIO_DEVICES, audioDevices, audio ->
                    lucene.indexAudioDevice(uuid(audio.getAudioDeviceID()), uuid(audio.getClientID()),
                            audio.getAudioDeviceBrand(), audio.getDeviceSerialNr(),
                            audio.getAudioDeviceFirmware(), audio.getDeviceType()));

            indexCollection(progress, KEY_DEPLOYMENT_VARIANTS, deploymentVariants, variant ->
                    lucene.indexDeploymentVariant(uuid(variant.getVariantID()), variant.getVariantCode(),
                            variant.getVariantName(), variant.getDescription(), variant.isActive()));

            indexCollection(progress, KEY_INSTALLED_SOFTWARE, installedSoftware, entry ->
                    lucene.indexInstalledSoftware(uuid(entry.getInstalledSoftwareID()), uuid(entry.getSiteID()),
                            uuid(entry.getSoftwareID())));

            indexCollection(progress, KEY_PHONE_INTEGRATIONS, phoneIntegrations, phone ->
                    lucene.indexPhoneIntegration(uuid(phone.getPhoneIntegrationID()), uuid(phone.getClientID()),
                            phone.getPhoneType(), phone.getPhoneBrand(), phone.getPhoneSerialNr(),
                            phone.getPhoneFirmware()));

            indexCollection(progress, KEY_PROJECTS, projects, project ->
                    lucene.indexProject(uuid(project.getProjectID()), project.getProjectSAPID(), project.getProjectName(),
                            uuid(project.getDeploymentVariantID()), project.getBundleType(), project.isStillActive(),
                            uuid(project.getAccountID()), uuid(project.getAddressID())));

            indexCollection(progress, KEY_RADIOS, radios, radio ->
                    lucene.indexRadio(uuid(radio.getRadioID()), uuid(radio.getSiteID()), uuid(radio.getAssignedClientID()),
                            radio.getRadioBrand(), radio.getRadioSerialNr(), radio.getMode(),
                            radio.getDigitalStandard()));

            indexCollection(progress, KEY_SERVERS, servers, server ->
                    lucene.indexServer(uuid(server.getServerID()), uuid(server.getSiteID()), server.getServerName(),
                            server.getServerBrand(), server.getServerSerialNr(), server.getServerOS(),
                            server.getPatchLevel(), server.getVirtualPlatform(), server.getVirtualVersion(),
                            server.isHighAvailability()));

            indexCollection(progress, KEY_SERVICE_CONTRACTS, serviceContracts, contract ->
                    lucene.indexServiceContract(uuid(contract.getContractID()), uuid(contract.getAccountID()),
                            uuid(contract.getProjectID()), uuid(contract.getSiteID()), contract.getContractNumber(),
                            contract.getStatus(), contract.getStartDate(), contract.getEndDate()));

            indexCollection(progress, KEY_SITES, sites, site ->
                    lucene.indexSite(uuid(site.getSiteID()), uuid(site.getProjectID()), uuid(site.getAddressID()),
                            site.getSiteName(), site.getFireZone(), site.getTenantCount()));

            indexCollection(progress, KEY_SOFTWARE, software, soft ->
                    lucene.indexSoftware(uuid(soft.getSoftwareID()), soft.getName(), soft.getRelease(),
                            soft.getRevision(), soft.getSupportPhase(), soft.getLicenseModel(),
                            soft.getEndOfSalesDate(), soft.getSupportStartDate(), soft.getSupportEndDate()));

            indexCollection(progress, KEY_UPGRADE_PLANS, upgradePlans, plan ->
                    lucene.indexUpgradePlan(uuid(plan.getUpgradePlanID()), uuid(plan.getSiteID()), uuid(plan.getSoftwareID()),
                            plan.getPlannedWindowStart(), plan.getPlannedWindowEnd(), plan.getStatus(),
                            plan.getCreatedAt(), plan.getCreatedBy()));

            log.info("Reindex abgeschlossen. {} Datensätze verarbeitet.", progress.totalDone());
        } catch (Exception e) {
            log.error("Reindex fehlgeschlagen", e);
        } finally {
            if (progressStarted) {
                progress.finish();
            }
        }
    }

    private <T> void indexCollection(IndexProgress progress, String key, List<T> items, Consumer<T> indexer) {
        for (T item : items) {
            try {
                indexer.accept(item);
            } catch (Exception e) {
                log.error("Fehler beim Indexieren in Kategorie {}", key, e);
            } finally {
                progress.inc(key);
            }
        }
    }

    private static <T> List<T> safe(List<T> items) {
        return items != null ? items : List.of();
    }

    private static String uuid(UUID id) {
        return id != null ? id.toString() : null;
    }
}
