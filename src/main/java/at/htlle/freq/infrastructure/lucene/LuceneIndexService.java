package at.htlle.freq.infrastructure.lucene;

import at.htlle.freq.domain.SearchHit;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * EIN gemeinsamer Lucene-Index für alle Entitäten.
 */
@Service
public class LuceneIndexService {

    private IndexWriter       writer;
    private final JdbcTemplate jdbc;
    private final StandardAnalyzer analyzer = new StandardAnalyzer();

    public LuceneIndexService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /* ───────────────────────── Initialisierung ─────────────────────────── */

    @PostConstruct
    public void init() throws IOException {
        FSDirectory dir = FSDirectory.open(Path.of("lucene-index"));
        IndexWriterConfig cfg = new IndexWriterConfig(analyzer);
        writer = new IndexWriter(dir, cfg);
        writer.deleteAll();
        writer.commit();
    }

    /* ───────────────────────── Index-API je Entität ─────────────────────── */

    public void indexAccount(int id, String name, String country) {
        Document d = baseDoc("account", id);
        d.add(new TextField("txt",     name,                  Field.Store.NO));
        d.add(new TextField("country", country.toLowerCase(), Field.Store.YES));  // ⬅︎ lowercase
        write(d);
    }

    public void indexProject(int id, String name, String variant) {
        Document d = baseDoc("project", id);
        d.add(new TextField("txt",     name,                   Field.Store.NO));
        d.add(new TextField("variant", variant.toLowerCase(),  Field.Store.YES));
        write(d);
    }

    public void indexSite(int id, String name, String fireZone) {
        Document d = baseDoc("site", id);
        d.add(new TextField("txt",      name,                   Field.Store.NO));
        d.add(new TextField("fireZone", fireZone.toLowerCase(), Field.Store.YES));
        write(d);
    }

    public void indexServer(int id, String name, String os) {
        Document d = baseDoc("server", id);
        d.add(new TextField("txt", name, Field.Store.NO));
        d.add(new TextField("os",  os.toLowerCase(), Field.Store.YES));
        write(d);
    }

    /* ───────────────────────── Gemeinsame Hilfen ───────────────────────── */

    private Document baseDoc(String type, int id) {
        Document d = new Document();
        d.add(new StringField("type", type, Field.Store.YES));
        d.add(new IntPoint("entityId", id));
        d.add(new StoredField("entityId", id));
        return d;
    }

    private void write(Document doc) {
        try {
            Term t = new Term("typeId", doc.get("type") + "#" + doc.get("entityId"));
            writer.updateDocument(t, doc);
            writer.commit();
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    /* ───────────────────────── Komplett-Reindex ────────────────────────── */

    public void reindexAll() {
        try { writer.deleteAll(); } catch (IOException ignored) {}

        jdbc.query("SELECT AccountID, AccountName, Country FROM Account",
                rs -> { while (rs.next())
                    indexAccount(rs.getInt("AccountID"),
                            rs.getString("AccountName"),
                            rs.getString("Country")); });

        jdbc.query("SELECT ProjectID, ProjectName, DeploymentVariant FROM Project",
                rs -> { while (rs.next())
                    indexProject(rs.getInt("ProjectID"),
                            rs.getString("ProjectName"),
                            rs.getString("DeploymentVariant")); });

        jdbc.query("SELECT SiteID, SiteName, FireZone FROM Site",
                rs -> { while (rs.next())
                    indexSite(rs.getInt("SiteID"),
                            rs.getString("SiteName"),
                            rs.getString("FireZone")); });

        jdbc.query("SELECT ServerID, ServerName, ServerOS FROM Server",
                rs -> { while (rs.next())
                    indexServer(rs.getInt("ServerID"),
                            rs.getString("ServerName"),
                            rs.getString("ServerOS")); });
    }

    /* ───────────────────────── Volltextsuche ───────────────────────────── */

    private static final String[] SEARCH_FIELDS =
            { "txt", "type", "country", "variant", "fireZone", "os" };

    public List<SearchHit> search(String q) {
        List<SearchHit> hits = new ArrayList<>();
        try (DirectoryReader rd = DirectoryReader.open(writer)) {
            IndexSearcher s = new IndexSearcher(rd);
            Query query = new MultiFieldQueryParser(SEARCH_FIELDS, analyzer).parse(q);

            for (ScoreDoc sd : s.search(query, 50).scoreDocs) {
                Document d = s.doc(sd.doc);
                hits.add(new SearchHit(
                        d.get("type"),
                        d.getField("entityId").numericValue().intValue(),
                        d.get("txt")));
            }
        } catch (Exception e) { throw new RuntimeException(e); }
        return hits;
    }

    /* ───────────────────────── Cleanup ─────────────────────────────────── */

    @PreDestroy
    public void close() {
        try { if (writer != null) writer.close(); } catch (IOException ignored) {}
    }
}