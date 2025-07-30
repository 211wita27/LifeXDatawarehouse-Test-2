package at.htlle.freq.infrastructure.lucene;

import at.htlle.freq.domain.Account;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
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

@Service
public class LuceneIndexService {

    private IndexWriter writer;

    @PostConstruct
    public void init() throws IOException {
        FSDirectory dir = FSDirectory.open(Path.of("lucene-index"));
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        this.writer = new IndexWriter(dir, config);
        // Alten Index komplett löschen, damit nur int-IDs drin stehen
        writer.deleteAll();
        writer.commit();
    }

    public void index(Account account) {
        try {
            String idStr = String.valueOf(account.getAccountID());
            Document doc = new Document();
            doc.add(new StringField("id", idStr, Field.Store.YES));
            doc.add(new TextField("name", account.getAccountName(), Field.Store.YES));
            doc.add(new TextField("contactEmail",account.getContactEmail(), Field.Store.YES));
            doc.add(new TextField("contactPhone", account.getContactPhone(), Field.Store.YES));
            doc.add(new TextField("vatNumber",account.getVATNumber(),Field.Store.YES));
            doc.add(new TextField("country", account.getCountry(), Field.Store.YES));
            writer.updateDocument(new Term("id", idStr), doc);
            writer.commit();
        } catch (IOException e) {
            throw new RuntimeException("Error indexing account", e);
        }
    }

    public List<Account> search(String queryString) {
        List<Account> results = new ArrayList<>();
        String[] fields = { "name", "contactEmail", "contactPhone", "vatNumber", "country" };

        try (DirectoryReader reader = DirectoryReader.open(writer)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, new StandardAnalyzer());
            Query query = parser.parse(queryString);

            TopDocs topDocs = searcher.search(query, 10);
            for (ScoreDoc sd : topDocs.scoreDocs) {
                Document doc = searcher.doc(sd.doc);
                String idStr   = doc.get("id");
                String name    = doc.get("name");
                String email   = doc.get("contactEmail");
                String phone   = doc.get("contactPhone");
                String vat     = doc.get("vatNumber");
                String country = doc.get("country");

                try {
                    int id = Integer.parseInt(idStr);
                    results.add(new Account(id, name, email, phone, vat, country));
                } catch (NumberFormatException nf) {
                    // überspringen, falls id nicht int ist
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error searching index", e);
        }
        return results;
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
