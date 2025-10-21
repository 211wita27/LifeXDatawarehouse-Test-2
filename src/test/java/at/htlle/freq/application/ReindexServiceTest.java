package at.htlle.freq.application;

import at.htlle.freq.domain.*;
import at.htlle.freq.infrastructure.lucene.IndexProgress;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import at.htlle.freq.infrastructure.search.SearchHit;
import at.htlle.freq.web.IndexProgressController;
import org.apache.lucene.search.Query;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class ReindexServiceTest {

    @Test
    void indexProgressReflectsRunningReindex() throws InterruptedException {
        UUID accountId1 = UUID.randomUUID();
        UUID accountId2 = UUID.randomUUID();
        UUID addressId1 = UUID.randomUUID();
        String cityId1 = "CITY-1";
        String cityId2 = "CITY-2";
        UUID siteId1 = UUID.randomUUID();
        UUID projectId1 = UUID.randomUUID();
        UUID deploymentVariantId1 = UUID.randomUUID();
        UUID clientId1 = UUID.randomUUID();
        UUID audioDeviceId1 = UUID.randomUUID();
        UUID phoneIntegrationId1 = UUID.randomUUID();
        UUID serverId1 = UUID.randomUUID();
        UUID radioId1 = UUID.randomUUID();
        UUID serviceContractId1 = UUID.randomUUID();
        UUID softwareId1 = UUID.randomUUID();
        UUID installedSoftwareId1 = UUID.randomUUID();
        UUID upgradePlanId1 = UUID.randomUUID();

        List<Account> accounts = List.of(
                new Account(accountId1, "Account A", "Alice", "a@example.com", "123", "AT-VAT", "AT"),
                new Account(accountId2, "Account B", "Bob", "b@example.com", "456", "DE-VAT", "DE")
        );
        List<Address> addresses = List.of(
                new Address(addressId1, "Main Street 1", cityId1)
        );
        List<City> cities = List.of(
                new City(cityId1, "Vienna", "AT"),
                new City(cityId2, "Berlin", "DE")
        );
        List<Country> countries = List.of(
                new Country("AT", "Austria"),
                new Country("DE", "Germany")
        );
        List<Clients> clients = List.of(
                new Clients(clientId1, siteId1, "Client Alpha", "BrandX", "SER123", "Windows", "2024-01", "LOCAL")
        );
        List<AudioDevice> audioDevices = List.of(
                new AudioDevice(audioDeviceId1, clientId1, "Plantronics", "DEV001", "1.0", "HEADSET")
        );
        List<DeploymentVariant> deploymentVariants = List.of(
                new DeploymentVariant(deploymentVariantId1, "DV-1", "Variant One", "Description", true)
        );
        List<InstalledSoftware> installedSoftware = List.of(
                new InstalledSoftware(installedSoftwareId1, siteId1, softwareId1)
        );
        List<PhoneIntegration> phoneIntegrations = List.of(
                new PhoneIntegration(phoneIntegrationId1, clientId1, "Emergency", "BrandPhone", "PH123", "2.0")
        );
        List<Project> projects = List.of(
                new Project(projectId1, "SAP-1", "Project One", deploymentVariantId1, "Bundle", "2024-01-01", true,
                        accountId1, addressId1)
        );
        List<Radio> radios = List.of(
                new Radio(radioId1, siteId1, clientId1, "Motorola", "RAD001", "Digital", "P25")
        );
        List<Server> servers = List.of(
                new Server(serverId1, siteId1, "ServerOne", "Dell", "SRV001", "Linux", "2024-01", "BareMetal",
                        "v1", true)
        );
        List<ServiceContract> serviceContracts = List.of(
                new ServiceContract(serviceContractId1, accountId1, projectId1, siteId1, "CN-1", "Approved",
                        "2024-01-01", "2024-12-31")
        );
        List<Site> sites = List.of(
                new Site(siteId1, "Site One", projectId1, addressId1, "Zone A", 5)
        );
        List<Software> software = List.of(
                new Software(softwareId1, "SoftOne", "1.0", "r1", "Production", "PerSeat", "2025-01-01",
                        "2024-01-01", "2026-01-01")
        );
        List<UpgradePlan> upgradePlans = List.of(
                new UpgradePlan(upgradePlanId1, siteId1, softwareId1, "2024-06-01T00:00", "2024-06-02T00:00",
                        "Planned", "2024-03-01", "Scheduler")
        );

        TaskExecutor executor = new SimpleAsyncTaskExecutor();
        SlowLuceneIndexService lucene = new SlowLuceneIndexService(5);
        ReindexService service = new ReindexService(
                executor,
                lucene,
                new StubAccountRepository(accounts),
                new StubAddressRepository(addresses),
                new StubCityRepository(cities),
                new StubCountryRepository(countries),
                new StubClientsRepository(clients),
                new StubAudioDeviceRepository(audioDevices),
                new StubDeploymentVariantRepository(deploymentVariants),
                new StubInstalledSoftwareRepository(installedSoftware),
                new StubPhoneIntegrationRepository(phoneIntegrations),
                new StubProjectRepository(projects),
                new StubRadioRepository(radios),
                new StubServerRepository(servers),
                new StubServiceContractRepository(serviceContracts),
                new StubSiteRepository(sites),
                new StubSoftwareRepository(software),
                new StubUpgradePlanRepository(upgradePlans)
        );

        IndexProgressController controller = new IndexProgressController();

        boolean started = service.triggerReindex();
        assertThat(started).isTrue();

        IndexProgress.Status active = awaitStatus(controller, IndexProgress.Status::active, 5_000);
        assertThat(active.active()).isTrue();

        IndexProgress.Status running = awaitStatus(controller,
                status -> status.active() && status.totalDone() > 0 && status.totalDone() < status.grandTotal(),
                5_000);
        assertThat(running.active()).isTrue();
        assertThat(running.totalDone()).isBetween(1, running.grandTotal() - 1);

        IndexProgress.Status finished = awaitStatus(controller, status -> !status.active(), 5_000);
        assertThat(finished.active()).isFalse();
        assertThat(finished.totalDone()).isEqualTo(finished.grandTotal());
        assertThat(finished.percent()).isEqualTo(100);
        assertThat(lucene.wasReset()).isTrue();
        assertThat(lucene.indexedDocuments()).isEqualTo(finished.grandTotal());
    }

    private IndexProgress.Status awaitStatus(IndexProgressController controller,
                                             Predicate<IndexProgress.Status> predicate,
                                             long timeoutMs) throws InterruptedException {
        long start = System.currentTimeMillis();
        IndexProgress.Status last = null;
        while (System.currentTimeMillis() - start <= timeoutMs) {
            last = controller.getStatus();
            if (predicate.test(last)) {
                return last;
            }
            Thread.sleep(10);
        }
        return last;
    }

    private static <T> Optional<T> find(List<T> data, Predicate<T> predicate) {
        return data.stream().filter(predicate).findFirst();
    }

    private static class SlowLuceneIndexService implements LuceneIndexService {
        private final long delayMs;
        private final AtomicInteger counter = new AtomicInteger();
        private final AtomicBoolean reset = new AtomicBoolean(false);

        SlowLuceneIndexService(long delayMs) {
            this.delayMs = delayMs;
        }

        @Override
        public void resetIndex() {
            reset.set(true);
            sleep();
        }

        @Override
        public List<SearchHit> search(String queryText) {
            return List.of();
        }

        @Override
        public List<SearchHit> search(Query query) {
            return List.of();
        }

        private void simulateWork() {
            counter.incrementAndGet();
            sleep();
        }

        private void sleep() {
            if (delayMs <= 0) {
                return;
            }
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public void indexAccount(String accountId, String accountName, String country, String contactEmail) {
            simulateWork();
        }

        @Override
        public void indexAddress(String addressId, String street, String cityId) {
            simulateWork();
        }

        @Override
        public void indexCity(String cityId, String cityName, String countryCode) {
            simulateWork();
        }

        @Override
        public void indexClient(String clientId, String siteId, String clientName, String clientBrand, String clientOS,
                                String installType) {
            simulateWork();
        }

        @Override
        public void indexCountry(String countryCode, String countryName) {
            simulateWork();
        }

        @Override
        public void indexAudioDevice(String audioDeviceId, String clientId, String brand, String serialNr,
                                     String firmware, String deviceType) {
            simulateWork();
        }

        @Override
        public void indexDeploymentVariant(String variantId, String variantCode, String variantName,
                                           String description, boolean active) {
            simulateWork();
        }

        @Override
        public void indexInstalledSoftware(String installedSoftwareId, String siteId, String softwareId) {
            simulateWork();
        }

        @Override
        public void indexPhoneIntegration(String phoneIntegrationId, String clientId, String phoneType,
                                          String phoneBrand, String phoneSerialNr, String phoneFirmware) {
            simulateWork();
        }

        @Override
        public void indexProject(String projectId, String projectSAPId, String projectName, String deploymentVariantId,
                                 String bundleType, boolean stillActive, String accountId, String addressId) {
            simulateWork();
        }

        @Override
        public void indexRadio(String radioId, String siteId, String assignedClientId, String radioBrand,
                               String radioSerialNr, String mode, String digitalStandard) {
            simulateWork();
        }

        @Override
        public void indexServer(String serverId, String siteId, String serverName, String serverBrand,
                                String serverSerialNr, String serverOS, String patchLevel, String virtualPlatform,
                                String virtualVersion, boolean highAvailability) {
            simulateWork();
        }

        @Override
        public void indexServiceContract(String contractId, String accountId, String projectId, String siteId,
                                         String contractNumber, String status, String startDate, String endDate) {
            simulateWork();
        }

        @Override
        public void indexSite(String siteId, String projectId, String addressId, String siteName, String fireZone,
                              Integer tenantCount) {
            simulateWork();
        }

        @Override
        public void indexSoftware(String softwareId, String name, String release, String revision, String supportPhase,
                                  String licenseModel, String endOfSalesDate, String supportStartDate,
                                  String supportEndDate) {
            simulateWork();
        }

        @Override
        public void indexUpgradePlan(String upgradePlanId, String siteId, String softwareId, String plannedWindowStart,
                                     String plannedWindowEnd, String status, String createdAt, String createdBy) {
            simulateWork();
        }

        int indexedDocuments() {
            return counter.get();
        }

        boolean wasReset() {
            return reset.get();
        }
    }

    private static class StubAccountRepository implements AccountRepository {
        private final List<Account> data;

        StubAccountRepository(List<Account> data) {
            this.data = data;
        }

        @Override
        public Optional<Account> findById(UUID id) {
            return find(data, a -> a.getAccountID().equals(id));
        }

        @Override
        public Optional<Account> findByName(String name) {
            return find(data, a -> a.getAccountName().equals(name));
        }

        @Override
        public Account save(Account account) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Account> findAll() {
            return data;
        }

        @Override
        public void deleteById(UUID id) {
            throw new UnsupportedOperationException();
        }
    }

    private static class StubAddressRepository implements AddressRepository {
        private final List<Address> data;

        StubAddressRepository(List<Address> data) {
            this.data = data;
        }

        @Override
        public Optional<Address> findById(UUID id) {
            return find(data, a -> a.getAddressID().equals(id));
        }

        @Override
        public Address save(Address address) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Address> findAll() {
            return data;
        }
    }

    private static class StubCityRepository implements CityRepository {
        private final List<City> data;

        StubCityRepository(List<City> data) {
            this.data = data;
        }

        @Override
        public Optional<City> findById(String id) {
            return find(data, c -> c.getCityID().equals(id));
        }

        @Override
        public List<City> findByCountry(String countryCode) {
            return data.stream().filter(c -> c.getCountryCode().equals(countryCode)).toList();
        }

        @Override
        public City save(City city) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<City> findAll() {
            return data;
        }
    }

    private static class StubCountryRepository implements CountryRepository {
        private final List<Country> data;

        StubCountryRepository(List<Country> data) {
            this.data = data;
        }

        @Override
        public Optional<Country> findById(String code) {
            return find(data, c -> c.getCountryCode().equals(code));
        }

        @Override
        public Country save(Country country) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Country> findAll() {
            return data;
        }
    }

    private static class StubClientsRepository implements ClientsRepository {
        private final List<Clients> data;

        StubClientsRepository(List<Clients> data) {
            this.data = data;
        }

        @Override
        public Optional<Clients> findById(UUID id) {
            return find(data, c -> c.getClientID().equals(id));
        }

        @Override
        public List<Clients> findBySite(UUID siteId) {
            return data.stream().filter(c -> c.getSiteID().equals(siteId)).toList();
        }

        @Override
        public Clients save(Clients client) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Clients> findAll() {
            return data;
        }
    }

    private static class StubAudioDeviceRepository implements AudioDeviceRepository {
        private final List<AudioDevice> data;

        StubAudioDeviceRepository(List<AudioDevice> data) {
            this.data = data;
        }

        @Override
        public Optional<AudioDevice> findById(UUID id) {
            return find(data, d -> d.getAudioDeviceID().equals(id));
        }

        @Override
        public List<AudioDevice> findByClient(UUID clientId) {
            return data.stream().filter(d -> d.getClientID().equals(clientId)).toList();
        }

        @Override
        public AudioDevice save(AudioDevice device) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<AudioDevice> findAll() {
            return data;
        }
    }

    private static class StubDeploymentVariantRepository implements DeploymentVariantRepository {
        private final List<DeploymentVariant> data;

        StubDeploymentVariantRepository(List<DeploymentVariant> data) {
            this.data = data;
        }

        @Override
        public Optional<DeploymentVariant> findById(UUID id) {
            return find(data, v -> v.getVariantID().equals(id));
        }

        @Override
        public Optional<DeploymentVariant> findByCode(String code) {
            return find(data, v -> v.getVariantCode().equals(code));
        }

        @Override
        public Optional<DeploymentVariant> findByName(String name) {
            return find(data, v -> v.getVariantName().equals(name));
        }

        @Override
        public DeploymentVariant save(DeploymentVariant dv) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<DeploymentVariant> findAll() {
            return data;
        }
    }

    private static class StubInstalledSoftwareRepository implements InstalledSoftwareRepository {
        private final List<InstalledSoftware> data;

        StubInstalledSoftwareRepository(List<InstalledSoftware> data) {
            this.data = data;
        }

        @Override
        public Optional<InstalledSoftware> findById(UUID id) {
            return find(data, s -> s.getInstalledSoftwareID().equals(id));
        }

        @Override
        public List<InstalledSoftware> findBySite(UUID siteId) {
            return data.stream().filter(s -> s.getSiteID().equals(siteId)).toList();
        }

        @Override
        public List<InstalledSoftware> findBySoftware(UUID softwareId) {
            return data.stream().filter(s -> s.getSoftwareID().equals(softwareId)).toList();
        }

        @Override
        public InstalledSoftware save(InstalledSoftware isw) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<InstalledSoftware> findAll() {
            return data;
        }
    }

    private static class StubPhoneIntegrationRepository implements PhoneIntegrationRepository {
        private final List<PhoneIntegration> data;

        StubPhoneIntegrationRepository(List<PhoneIntegration> data) {
            this.data = data;
        }

        @Override
        public Optional<PhoneIntegration> findById(UUID id) {
            return find(data, p -> p.getPhoneIntegrationID().equals(id));
        }

        @Override
        public List<PhoneIntegration> findByClient(UUID clientId) {
            return data.stream().filter(p -> p.getClientID().equals(clientId)).toList();
        }

        @Override
        public PhoneIntegration save(PhoneIntegration phone) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<PhoneIntegration> findAll() {
            return data;
        }
    }

    private static class StubProjectRepository implements ProjectRepository {
        private final List<Project> data;

        StubProjectRepository(List<Project> data) {
            this.data = data;
        }

        @Override
        public Optional<Project> findById(UUID id) {
            return find(data, p -> p.getProjectID().equals(id));
        }

        @Override
        public Optional<Project> findBySapId(String sapId) {
            return find(data, p -> p.getProjectSAPID().equals(sapId));
        }

        @Override
        public Project save(Project project) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Project> findAll() {
            return data;
        }
    }

    private static class StubRadioRepository implements RadioRepository {
        private final List<Radio> data;

        StubRadioRepository(List<Radio> data) {
            this.data = data;
        }

        @Override
        public Optional<Radio> findById(UUID id) {
            return find(data, r -> r.getRadioID().equals(id));
        }

        @Override
        public List<Radio> findBySite(UUID siteId) {
            return data.stream().filter(r -> r.getSiteID().equals(siteId)).toList();
        }

        @Override
        public Radio save(Radio radio) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Radio> findAll() {
            return data;
        }
    }

    private static class StubServerRepository implements ServerRepository {
        private final List<Server> data;

        StubServerRepository(List<Server> data) {
            this.data = data;
        }

        @Override
        public Optional<Server> findById(UUID id) {
            return find(data, s -> s.getServerID().equals(id));
        }

        @Override
        public List<Server> findBySite(UUID siteId) {
            return data.stream().filter(s -> s.getSiteID().equals(siteId)).toList();
        }

        @Override
        public Server save(Server server) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Server> findAll() {
            return data;
        }
    }

    private static class StubServiceContractRepository implements ServiceContractRepository {
        private final List<ServiceContract> data;

        StubServiceContractRepository(List<ServiceContract> data) {
            this.data = data;
        }

        @Override
        public Optional<ServiceContract> findById(UUID id) {
            return find(data, s -> s.getContractID().equals(id));
        }

        @Override
        public List<ServiceContract> findByAccount(UUID accountId) {
            return data.stream().filter(s -> s.getAccountID().equals(accountId)).toList();
        }

        @Override
        public ServiceContract save(ServiceContract contract) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<ServiceContract> findAll() {
            return data;
        }
    }

    private static class StubSiteRepository implements SiteRepository {
        private final List<Site> data;

        StubSiteRepository(List<Site> data) {
            this.data = data;
        }

        @Override
        public Optional<Site> findById(UUID id) {
            return find(data, s -> s.getSiteID().equals(id));
        }

        @Override
        public List<Site> findByProject(UUID projectId) {
            return data.stream().filter(s -> s.getProjectID().equals(projectId)).toList();
        }

        @Override
        public Site save(Site site) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Site> findAll() {
            return data;
        }
    }

    private static class StubSoftwareRepository implements SoftwareRepository {
        private final List<Software> data;

        StubSoftwareRepository(List<Software> data) {
            this.data = data;
        }

        @Override
        public Optional<Software> findById(UUID id) {
            return find(data, s -> s.getSoftwareID().equals(id));
        }

        @Override
        public List<Software> findByName(String name) {
            return data.stream().filter(s -> s.getName().equals(name)).toList();
        }

        @Override
        public Software save(Software software) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Software> findAll() {
            return data;
        }
    }

    private static class StubUpgradePlanRepository implements UpgradePlanRepository {
        private final List<UpgradePlan> data;

        StubUpgradePlanRepository(List<UpgradePlan> data) {
            this.data = data;
        }

        @Override
        public Optional<UpgradePlan> findById(UUID id) {
            return find(data, u -> u.getUpgradePlanID().equals(id));
        }

        @Override
        public List<UpgradePlan> findBySite(UUID siteId) {
            return data.stream().filter(u -> u.getSiteID().equals(siteId)).toList();
        }

        @Override
        public UpgradePlan save(UpgradePlan plan) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<UpgradePlan> findAll() {
            return data;
        }
    }
}
