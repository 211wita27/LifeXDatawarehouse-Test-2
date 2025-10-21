package at.htlle.freq.infrastructure.search;

import at.htlle.freq.infrastructure.lucene.LuceneIndexServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SuggestServiceTest {

    private final Path indexPath = Paths.get("target", "lifex-index");
    private final LuceneIndexServiceImpl lucene = new LuceneIndexServiceImpl();
    private final SuggestService service = new SuggestService();

    @BeforeEach
    void cleanIndex() throws IOException {
        if (Files.exists(indexPath)) {
            Files.walk(indexPath)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
        Files.createDirectories(indexPath);
    }

    @Test
    void suggestReturnsTermsFromIndex() {
        lucene.indexAccount("acc-5", "Acme", "Austria", "contact@acme.test");
        lucene.indexServer("srv-1", null, null, "Dell", null, null, null, null, null, false);

        List<String> suggestions = service.suggest("ac", 5);
        assertTrue(suggestions.stream().anyMatch(s -> s.equalsIgnoreCase("acme")));
    }

    @Test
    void suggestHonoursPrefixLengthAndMax() {
        lucene.indexAccount("acc-6", "FooBar", null, null);
        assertTrue(service.suggest("f", 5).isEmpty(), "prefix shorter than 2 characters should yield no results");
        assertTrue(service.suggest(null, 5).isEmpty());
        assertTrue(service.suggest("fo", 0).isEmpty());

        lucene.indexAccount("acc-7", "FooBaz", null, null);
        List<String> suggestions = service.suggest("fo", 1);
        assertEquals(1, suggestions.size());
    }
}
