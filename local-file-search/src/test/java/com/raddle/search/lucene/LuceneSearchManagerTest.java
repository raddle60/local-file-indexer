package com.raddle.search.lucene;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.raddle.search.model.SearchResult;

public class LuceneSearchManagerTest {

    private static LuceneSearchManager searchManager = new LuceneSearchManager();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        searchManager.openIndex(new String[] { "../local-file-index/target/index" });
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        searchManager.closeIndex();
    }

    @Test
    public void testSearch() {
        SearchResult search = searchManager.search("StopwordAnalyzerBase NoSymbolTokenizer","");
        System.out.println(search.getResults());
    }
}
