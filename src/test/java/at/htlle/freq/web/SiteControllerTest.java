package at.htlle.freq.web;

import at.htlle.freq.application.InstalledSoftwareService;
import at.htlle.freq.application.SiteService;
import at.htlle.freq.domain.Site;
import at.htlle.freq.web.dto.SiteSoftwareAssignmentDto;
import at.htlle.freq.web.dto.SiteUpsertRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SiteControllerTest {

    private NamedParameterJdbcTemplate jdbc;
    private SiteService siteService;
    private InstalledSoftwareService installedSoftwareService;
    private SiteController controller;

    @BeforeEach
    void setUp() {
        jdbc = mock(NamedParameterJdbcTemplate.class);
        siteService = mock(SiteService.class);
        installedSoftwareService = mock(InstalledSoftwareService.class);
        controller = new SiteController(jdbc, siteService, installedSoftwareService);
    }

    @Test
    void createDelegatesToServices() {
        UUID project = UUID.randomUUID();
        UUID address = UUID.randomUUID();
        UUID software = UUID.randomUUID();
        SiteUpsertRequest request = new SiteUpsertRequest(
                "Test Site",
                project,
                address,
                "Zone",
                5,
                List.of(new SiteSoftwareAssignmentDto(null, software, "Offered", "2024-01-01", null, null))
        );

        Site saved = new Site();
        UUID siteId = UUID.randomUUID();
        saved.setSiteID(siteId);
        when(siteService.createOrUpdateSite(any(Site.class))).thenReturn(saved);

        controller.create(request);

        verify(siteService).createOrUpdateSite(argThat(site ->
                "Test Site".equals(site.getSiteName()) &&
                        project.equals(site.getProjectID()) &&
                        address.equals(site.getAddressID())));
        verify(installedSoftwareService).replaceAssignmentsForSite(eq(siteId),
                argThat(list -> list.size() == 1 && software.equals(list.get(0).getSoftwareID())));
    }

    @Test
    void createWrapsDomainErrors() {
        SiteUpsertRequest request = new SiteUpsertRequest(
                "Test Site",
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                List.of()
        );
        when(siteService.createOrUpdateSite(any(Site.class)))
                .thenThrow(new IllegalArgumentException("invalid"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> controller.create(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verifyNoInteractions(installedSoftwareService);
    }

    @Test
    void updateReturnsNotFoundWhenSiteMissing() {
        UUID siteId = UUID.randomUUID();
        SiteUpsertRequest request = new SiteUpsertRequest(
                null,
                null,
                null,
                null,
                null,
                List.of()
        );
        when(siteService.updateSite(eq(siteId), any(Site.class))).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.update(siteId.toString(), request));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(installedSoftwareService, never()).replaceAssignmentsForSite(any(), any());
    }

    @Test
    void updateRejectsInvalidUuid() {
        SiteUpsertRequest request = new SiteUpsertRequest(
                null,
                null,
                null,
                null,
                null,
                List.of()
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.update("not-a-uuid", request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void updateDelegatesToInstalledSoftwareService() {
        UUID siteId = UUID.randomUUID();
        UUID software = UUID.randomUUID();
        SiteUpsertRequest request = new SiteUpsertRequest(
                "Updated Site",
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Zone",
                20,
                List.of(new SiteSoftwareAssignmentDto(UUID.randomUUID(), software, "Installed", null, "2024-02-02", null))
        );
        when(siteService.updateSite(eq(siteId), any(Site.class))).thenReturn(Optional.of(new Site()));

        controller.update(siteId.toString(), request);

        verify(installedSoftwareService).replaceAssignmentsForSite(eq(siteId),
                argThat(list -> list.size() == 1 && software.equals(list.get(0).getSoftwareID())));
    }
}
