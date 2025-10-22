package at.htlle.freq.infrastructure.search;

import org.apache.lucene.search.Query;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SmartQueryBuilderTest {

    private final SmartQueryBuilder builder = new SmartQueryBuilder();

    @Test
    void looksLikeLuceneDetectsSyntaxIndicators() {
        assertTrue(SmartQueryBuilder.looksLikeLucene("type:server"));
        assertTrue(SmartQueryBuilder.looksLikeLucene("\"quoted phrase\""));
        assertTrue(SmartQueryBuilder.looksLikeLucene("foo AND bar"));
        assertTrue(SmartQueryBuilder.looksLikeLucene("name*"));
    }

    @Test
    void looksLikeLuceneRejectsPlainTerms() {
        assertFalse(SmartQueryBuilder.looksLikeLucene(null));
        assertFalse(SmartQueryBuilder.looksLikeLucene("   "));
        assertFalse(SmartQueryBuilder.looksLikeLucene("just words"));
    }

    @Test
    void buildReturnsMatchAllForBlankInput() {
        Query query = builder.build("   ");
        assertEquals("*:*", query.toString());
    }

    @Test
    void buildParsesUserInputAcrossFields() {
        Query query = builder.build("Vienna Server");
        String lucene = query.toString();
        assertTrue(lucene.contains("txt:vienna"));
        assertTrue(lucene.contains("txt:server"));
    }

    @Test
    void buildWrapsParserExceptions() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> builder.build("\"unterminated"));
        assertTrue(ex.getMessage().contains("Ungültige Suchanfrage"));
    }
}
