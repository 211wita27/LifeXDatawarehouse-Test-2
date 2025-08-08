package at.htlle.freq.infrastructure.search;

import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Einfache Autovervollständigung über Lucene-Terms.
 * Geht segmentweise über die Index-Reader (ohne MultiFields),
 * sammelt passende Terms und dedupliziert sie.
 */
@Service
public class SuggestService {

    // Felder, aus denen Vorschläge kommen sollen
    private static final List<String> FIELDS = List.of(
            "txt", "brand", "country", "variant", "fireZone", "os", "vplat", "sap", "email"
    );

    /**
     * Liefert bis zu {@code max} Vorschläge für das Präfix {@code prefix}.
     * Rückgabe ist kleingeschrieben (entspricht StandardAnalyzer).
     */
    public List<String> suggest(String prefix, int max) {
        if (prefix == null) return List.of();
        String pfx = prefix.toLowerCase();

        // sehr kurze Präfixe ausbremsen
        if (pfx.length() < 2) return List.of();

        Set<String> out = new LinkedHashSet<>();

        try (var dir = FSDirectory.open(LuceneIndexService.INDEX_PATH);
             var rd  = DirectoryReader.open(dir)) {

            // Für jedes gewünschte Feld …
            outer:
            for (String field : FIELDS) {

                // … über alle Segmente (Leaves) iterieren
                for (LeafReaderContext leaf : rd.leaves()) {
                    Terms terms = leaf.reader().terms(field);
                    if (terms == null) continue;

                    TermsEnum te = terms.iterator();
                    BytesRef br;
                    while ((br = te.next()) != null) {
                        String term = br.utf8ToString(); // bereits lowercase bei StandardAnalyzer
                        if (term.startsWith(pfx)) {
                            out.add(term);
                            if (out.size() >= max) break outer;
                        }
                    }
                }
            }
        } catch (IOException ignored) {
            // Im Fehlerfall lieber leer zurückgeben
        }

        return out.stream().limit(max).collect(Collectors.toList());
    }
}
