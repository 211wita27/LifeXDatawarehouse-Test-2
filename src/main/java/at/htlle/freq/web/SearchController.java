package at.htlle.freq.web;

import at.htlle.freq.domain.SearchHit;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final LuceneIndexService lucene;

    public SearchController(LuceneIndexService lucene) {
        this.lucene = lucene;
    }

    /** GET /search?q=foo */
    @GetMapping
    public ResponseEntity<List<SearchHit>> query(@RequestParam String q){
        return ResponseEntity.ok(lucene.search(q.isBlank() ? "*:*" : q));
    }
}