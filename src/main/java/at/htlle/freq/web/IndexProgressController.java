package at.htlle.freq.web;

import at.htlle.freq.infrastructure.lucene.IndexProgress;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST für Reindex-Fortschritt (UI pollt das). */
@RestController
@RequestMapping("/api/index-progress")
public class IndexProgressController {

    @GetMapping
    public IndexProgress.Status getStatus() {
        return IndexProgress.get().status();
    }
}
