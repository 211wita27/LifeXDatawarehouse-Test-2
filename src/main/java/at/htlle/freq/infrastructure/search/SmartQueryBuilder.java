package at.htlle.freq.infrastructure.search;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.springframework.stereotype.Component;

@Component
public class SmartQueryBuilder {

    private static final StandardAnalyzer ANALYZER = new StandardAnalyzer();

    // MUSS zu LuceneIndexService passen
    private static final String[] SEARCH_FIELDS = {
            "txt","type","country","variant","fireZone","os","brand","vplat",
            "sap","email","mode","digitalStandard","direction","phoneType",
            // NEU: Relations & Rollups
            "accountId","projectId","siteId","clientId",
            "serverBrand","serverOS","serverVplat","hasServer"
    };

    /** Heuristik: sieht der String nach Lucene-Syntax aus? */
    public static boolean looksLikeLucene(String q) {
        if (q == null) return false;
        String s = q.trim();
        return s.contains(":") || s.contains("\"") || s.contains(" AND ")
                || s.contains(" OR ") || s.endsWith("*");
    }

    /** Baut aus einer normalen Nutzereingabe eine Multi-Field-Lucene-Query. */
    public Query build(String userInput) {
        try {
            if (userInput == null || userInput.isBlank()) {
                return new QueryParser("txt", ANALYZER).parse("*:*");
            }
            MultiFieldQueryParser p = new MultiFieldQueryParser(SEARCH_FIELDS, ANALYZER);
            p.setDefaultOperator(QueryParser.Operator.AND);
            return p.parse(userInput.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Ungültige Suchanfrage: " + userInput, e);
        }
    }
}