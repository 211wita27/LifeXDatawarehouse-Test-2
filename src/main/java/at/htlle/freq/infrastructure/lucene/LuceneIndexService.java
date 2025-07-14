package at.htlle.freq.infrastructure.lucene;

import at.htlle.freq.domain.Account;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class LuceneIndexService {

    private IndexWriter writer;

    @PostConstruct
    public void init() throws IOException {
        FSDirectory dir = FSDirectory.open(Path.of("lucene-index"));
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        this.writer = new IndexWriter(dir, config);
    }

    public void index(Account account) {
        try {
            Document doc = new Document();
            doc.add(new StringField("id", account.getId().toString(), Field.Store.YES));
            doc.add(new TextField("name", account.getName(), Field.Store.YES));

            writer.updateDocument(new Term("id", account.getId().toString()), doc);
            writer.commit();
        } catch (IOException e) {
            throw new RuntimeException("Error indexing account", e);
        }
    }
    public List<Account> search(String queryString) {
        try {
            List<Account> results = new ArrayList<>();

            DirectoryReader reader = DirectoryReader.open(writer); // Reuse writer's directory
            IndexSearcher searcher = new IndexSearcher(reader);

            QueryParser parser = new QueryParser("name", new StandardAnalyzer());
            Query query = parser.parse(queryString);

            TopDocs topDocs = searcher.search(query, 10); // Top 10 Ergebnisse

            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document doc = searcher.doc(scoreDoc.doc);
                String id = doc.get("id");
                String name = doc.get("name");
                results.add(new Account(UUID.fromString(id), name));
            }

            reader.close();
            return results;

        } catch (Exception e) {
            throw new RuntimeException("Error searching index", e);
        }
    }

    @PreDestroy
    public void close() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException("Error closing writer", e);
            }
        }
    }
}
