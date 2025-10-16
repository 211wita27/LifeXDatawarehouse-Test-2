package at.htlle.freq.web;

import at.htlle.freq.infrastructure.search.SearchHit;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import at.htlle.freq.infrastructure.search.SmartQueryBuilder;
import at.htlle.freq.infrastructure.search.SuggestService;
import org.apache.lucene.search.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SearchController {

    private final LuceneIndexService lucene;
    private final SmartQueryBuilder smart;
    private final SuggestService suggest;

    public SearchController(LuceneIndexService lucene,
                            SmartQueryBuilder smart,
                            SuggestService suggest) {
        this.lucene = lucene;
        this.smart = smart;
        this.suggest = suggest;
    }

    @GetMapping("/search")
    public ResponseEntity<List<SearchHit>> query(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "raw", defaultValue = "false") boolean raw
    ) {
        if (q == null || q.isBlank()) {
            return ResponseEntity.ok(List.of());
        }

        // Lucene-String direkt ausführen (wenn 'raw' oder klar Lucene-Syntax)
        if (raw || SmartQueryBuilder.looksLikeLucene(q)) {
            return ResponseEntity.ok(lucene.search(q));
        }

        // "Smarte" User-Eingabe -> Lucene-Query bauen und Overload aufrufen
        Query built = smart.build(q);
        return ResponseEntity.ok(lucene.search(built));
    }

    // Autocomplete/Suggest
    @GetMapping("/search/suggest")
    public List<String> suggest(
            @RequestParam("q") String q,
            @RequestParam(name = "max", defaultValue = "8") int max
    ) {
        return suggest.suggest(q, Math.max(1, Math.min(max, 25)));
    }
}
