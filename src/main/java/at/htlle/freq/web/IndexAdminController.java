package at.htlle.freq.web;

import at.htlle.freq.application.ReindexService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/index")
public class IndexAdminController {

    private final ReindexService reindexService;

    public IndexAdminController(ReindexService reindexService) {
        this.reindexService = reindexService;
    }

    @PostMapping("/reindex")
    public void reindex() {
        reindexService.triggerReindex();
    }
}
