package at.htlle.freq.web;

import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/index")
public class IndexAdminController {

    private final LuceneIndexService lucene;

    public IndexAdminController(LuceneIndexService lucene) {
        this.lucene = lucene;
    }

    @PostMapping("/reindex")
    public void reindex() {
        new Thread(lucene::reindexAll, "manual-reindex").start();
    }
}
