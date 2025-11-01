package at.htlle.freq.web;

import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SearchControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LuceneIndexService luceneIndexService;

    @BeforeEach
    void setUp() {
        luceneIndexService.reindexAll();
        luceneIndexService.indexAccount("acc-integration", "Acme Integration", "Austria", "contact@acme.example");
    }

    @AfterEach
    void tearDown() {
        luceneIndexService.reindexAll();
    }

    @Test
    void plainTextQueryReturnsHits() throws Exception {
        mockMvc.perform(get("/search").param("q", "Acme Integration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("acc-integration"))
                .andExpect(jsonPath("$[0].type").value("account"));
    }
}
