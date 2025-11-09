package at.htlle.freq.application;

import at.htlle.freq.domain.InstalledSoftware;
import at.htlle.freq.domain.InstalledSoftwareRepository;
import at.htlle.freq.domain.InstalledSoftwareStatus;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static at.htlle.freq.application.TestFixtures.UUID2;
import static at.htlle.freq.application.TestFixtures.UUID4;
import static at.htlle.freq.application.TestFixtures.UUID5;
import static at.htlle.freq.application.TestFixtures.installedSoftware;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class InstalledSoftwareServiceTest {

    private InstalledSoftwareRepository repo;
    private LuceneIndexService lucene;
    private InstalledSoftwareService service;

    @BeforeEach
    void setUp() {
        repo = mock(InstalledSoftwareRepository.class);
        lucene = mock(LuceneIndexService.class);
        service = new InstalledSoftwareService(repo, lucene);
    }

    @Test
    void getInstalledSoftwareByIdRejectsNull() {
        assertThrows(NullPointerException.class, () -> service.getInstalledSoftwareById(null));
    }

    @Test
    void getInstalledSoftwareBySiteRejectsNull() {
        assertThrows(NullPointerException.class, () -> service.getInstalledSoftwareBySite(null));
    }

    @Test
    void getInstalledSoftwareBySoftwareRejectsNull() {
        assertThrows(NullPointerException.class, () -> service.getInstalledSoftwareBySoftware(null));
    }

    @Test
    void createInstalledSoftwareRequiresSiteId() {
        InstalledSoftware value = new InstalledSoftware();
        value.setSoftwareID(UUID.randomUUID());
        assertThrows(IllegalArgumentException.class, () -> service.createOrUpdateInstalledSoftware(value));
    }

    @Test
    void createInstalledSoftwareRequiresSoftwareId() {
        InstalledSoftware value = new InstalledSoftware();
        value.setSiteID(UUID.randomUUID());
        assertThrows(IllegalArgumentException.class, () -> service.createOrUpdateInstalledSoftware(value));
    }

    @Test
    void createInstalledSoftwareIndexesImmediately() {
        InstalledSoftware value = installedSoftware();
        when(repo.save(value)).thenReturn(value);

        InstalledSoftware saved = service.createOrUpdateInstalledSoftware(value);
        assertSame(value, saved);
        verify(lucene).indexInstalledSoftware(eq(UUID2.toString()), eq(UUID4.toString()), eq(UUID5.toString()),
                eq(InstalledSoftwareStatus.OFFERED.dbValue()), eq(value.getOfferedDate()), isNull());
    }

    @Test
    void createInstalledSoftwareRegistersAfterCommit() {
        InstalledSoftware value = installedSoftware();
        when(repo.save(value)).thenReturn(value);

        List<TransactionSynchronization> synchronizations = TransactionTestUtils.executeWithinTransaction(() -> service.createOrUpdateInstalledSoftware(value));
        assertEquals(1, synchronizations.size());
        synchronizations.forEach(TransactionSynchronization::afterCommit);

        verify(lucene).indexInstalledSoftware(eq(UUID2.toString()), eq(UUID4.toString()), eq(UUID5.toString()),
                eq(InstalledSoftwareStatus.OFFERED.dbValue()), eq(value.getOfferedDate()), isNull());
    }

    @Test
    void createInstalledSoftwareContinuesWhenLuceneFails() {
        InstalledSoftware value = installedSoftware();
        when(repo.save(value)).thenReturn(value);
        doThrow(new RuntimeException("Lucene error")).when(lucene)
                .indexInstalledSoftware(any(), any(), any(), any(), any(), any());

        InstalledSoftware saved = service.createOrUpdateInstalledSoftware(value);
        assertSame(value, saved);
        verify(lucene).indexInstalledSoftware(eq(UUID2.toString()), eq(UUID4.toString()), eq(UUID5.toString()),
                eq(InstalledSoftwareStatus.OFFERED.dbValue()), eq(value.getOfferedDate()), isNull());
    }

    @Test
    void createInstalledSoftwareDefaultsStatusWhenMissing() {
        InstalledSoftware value = new InstalledSoftware();
        value.setSiteID(UUID4);
        value.setSoftwareID(UUID5);
        when(repo.save(any())).thenAnswer(invocation -> {
            InstalledSoftware arg = invocation.getArgument(0);
            arg.setInstalledSoftwareID(UUID2);
            return arg;
        });

        InstalledSoftware saved = service.createOrUpdateInstalledSoftware(value);
        assertEquals(InstalledSoftwareStatus.OFFERED.dbValue(), saved.getStatus());
        assertNotNull(saved.getOfferedDate());
        verify(lucene).indexInstalledSoftware(eq(UUID2.toString()), eq(UUID4.toString()), eq(UUID5.toString()),
                eq(InstalledSoftwareStatus.OFFERED.dbValue()), notNull(), isNull());
    }

    @Test
    void createInstalledSoftwareSetsDatesWhenStatusInstalled() {
        InstalledSoftware value = new InstalledSoftware();
        value.setSiteID(UUID4);
        value.setSoftwareID(UUID5);
        value.setStatus("installed");
        when(repo.save(any())).thenAnswer(invocation -> {
            InstalledSoftware arg = invocation.getArgument(0);
            arg.setInstalledSoftwareID(UUID2);
            return arg;
        });

        InstalledSoftware saved = service.createOrUpdateInstalledSoftware(value);
        assertEquals(InstalledSoftwareStatus.INSTALLED.dbValue(), saved.getStatus());
        assertNotNull(saved.getOfferedDate());
        assertNotNull(saved.getInstalledDate());
        verify(lucene).indexInstalledSoftware(eq(UUID2.toString()), eq(UUID4.toString()), eq(UUID5.toString()),
                eq(InstalledSoftwareStatus.INSTALLED.dbValue()), notNull(), notNull());
    }

    @Test
    void createInstalledSoftwareRejectsInvalidStatus() {
        InstalledSoftware value = installedSoftware();
        value.setStatus("Invalid");
        assertThrows(IllegalArgumentException.class, () -> service.createOrUpdateInstalledSoftware(value));
        verifyNoInteractions(repo);
    }

    @Test
    void updateInstalledSoftwareAppliesPatch() {
        InstalledSoftware existing = installedSoftware();
        when(repo.findById(UUID2)).thenReturn(Optional.of(existing));
        when(repo.save(existing)).thenReturn(existing);

        InstalledSoftware patch = new InstalledSoftware();
        patch.setSiteID(UUID.randomUUID());
        patch.setSoftwareID(UUID.randomUUID());

        List<TransactionSynchronization> synchronizations = TransactionTestUtils.executeWithinTransaction(() -> {
            Optional<InstalledSoftware> updated = service.updateInstalledSoftware(UUID2, patch);
            assertTrue(updated.isPresent());
            assertEquals(patch.getSiteID(), existing.getSiteID());
            assertEquals(patch.getSoftwareID(), existing.getSoftwareID());
        });
        synchronizations.forEach(TransactionSynchronization::afterCommit);

        verify(lucene).indexInstalledSoftware(eq(UUID2.toString()), eq(existing.getSiteID().toString()),
                eq(existing.getSoftwareID().toString()), eq(InstalledSoftwareStatus.OFFERED.dbValue()),
                eq(existing.getOfferedDate()), isNull());
    }

    @Test
    void updateInstalledSoftwareUpdatesStatusWhenProvided() {
        InstalledSoftware existing = installedSoftware();
        when(repo.findById(UUID2)).thenReturn(Optional.of(existing));
        when(repo.save(existing)).thenReturn(existing);

        InstalledSoftware patch = new InstalledSoftware();
        patch.setStatus("rejected");

        List<TransactionSynchronization> synchronizations = TransactionTestUtils.executeWithinTransaction(() -> {
            Optional<InstalledSoftware> updated = service.updateInstalledSoftware(UUID2, patch);
            assertTrue(updated.isPresent());
            assertEquals(InstalledSoftwareStatus.REJECTED.dbValue(), updated.get().getStatus());
        });
        synchronizations.forEach(TransactionSynchronization::afterCommit);

        verify(lucene).indexInstalledSoftware(eq(UUID2.toString()), eq(existing.getSiteID().toString()),
                eq(existing.getSoftwareID().toString()), eq(InstalledSoftwareStatus.REJECTED.dbValue()),
                eq(existing.getOfferedDate()), isNull());
    }

    @Test
    void updateInstalledSoftwareSetsOfferedDateWhenStatusBecomesOffered() {
        InstalledSoftware existing = new InstalledSoftware(UUID2, UUID4, UUID5,
                InstalledSoftwareStatus.REJECTED.dbValue(), null, null);
        when(repo.findById(UUID2)).thenReturn(Optional.of(existing));
        when(repo.save(existing)).thenReturn(existing);

        InstalledSoftware patch = new InstalledSoftware();
        patch.setStatus("offered");

        List<TransactionSynchronization> synchronizations = TransactionTestUtils.executeWithinTransaction(() -> {
            Optional<InstalledSoftware> updated = service.updateInstalledSoftware(UUID2, patch);
            assertTrue(updated.isPresent());
            assertEquals(InstalledSoftwareStatus.OFFERED.dbValue(), updated.get().getStatus());
            assertNotNull(updated.get().getOfferedDate());
        });
        synchronizations.forEach(TransactionSynchronization::afterCommit);

        verify(lucene).indexInstalledSoftware(eq(UUID2.toString()), eq(existing.getSiteID().toString()),
                eq(existing.getSoftwareID().toString()), eq(InstalledSoftwareStatus.OFFERED.dbValue()),
                notNull(), isNull());
    }

    @Test
    void updateInstalledSoftwareSetsInstalledDateWhenStatusBecomesInstalled() {
        InstalledSoftware existing = new InstalledSoftware(UUID2, UUID4, UUID5,
                InstalledSoftwareStatus.OFFERED.dbValue(), "2024-02-01", null);
        when(repo.findById(UUID2)).thenReturn(Optional.of(existing));
        when(repo.save(existing)).thenReturn(existing);

        InstalledSoftware patch = new InstalledSoftware();
        patch.setStatus("installed");

        List<TransactionSynchronization> synchronizations = TransactionTestUtils.executeWithinTransaction(() -> {
            Optional<InstalledSoftware> updated = service.updateInstalledSoftware(UUID2, patch);
            assertTrue(updated.isPresent());
            assertEquals(InstalledSoftwareStatus.INSTALLED.dbValue(), updated.get().getStatus());
            assertNotNull(updated.get().getInstalledDate());
        });
        synchronizations.forEach(TransactionSynchronization::afterCommit);

        verify(lucene).indexInstalledSoftware(eq(UUID2.toString()), eq(existing.getSiteID().toString()),
                eq(existing.getSoftwareID().toString()), eq(InstalledSoftwareStatus.INSTALLED.dbValue()),
                eq(existing.getOfferedDate()), notNull());
    }

    @Test
    void updateInstalledSoftwareReturnsEmptyWhenUnknown() {
        when(repo.findById(UUID2)).thenReturn(Optional.empty());
        assertTrue(service.updateInstalledSoftware(UUID2, installedSoftware()).isEmpty());
    }

    @Test
    void deleteInstalledSoftwareLoadsOptional() {
        when(repo.findById(UUID2)).thenReturn(Optional.of(installedSoftware()));
        service.deleteInstalledSoftware(UUID2);
        verify(repo).findById(UUID2);
    }
}
