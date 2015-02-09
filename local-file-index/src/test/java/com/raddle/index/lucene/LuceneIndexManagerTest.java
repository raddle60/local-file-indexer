package com.raddle.index.lucene;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.BeforeClass;
import org.junit.Test;

import com.raddle.config.file.JsonFileConfigManager;
import com.raddle.index.config.IndexConfig;
import com.raddle.index.lucene.analyzer.FullTwoTokenAnalyzer;

public class LuceneIndexManagerTest {

    private LuceneIndexManager indexManager = new LuceneIndexManager();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Test
    public void testRebuildIndex() throws Exception {
        JsonFileConfigManager configManager = new JsonFileConfigManager();
        configManager.setConfigFile("target/config.json");
        configManager.init();
        indexManager.setConfigManager(configManager);
        IndexConfig config = new IndexConfig();
        config.setIndexSaveDir("target/index");
        indexManager.rebuildIndex("src/main", null, config);
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(config.getIndexSaveDir())));
        System.out.println(reader.numDocs());
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new FullTwoTokenAnalyzer(Version.LUCENE_42);
        QueryParser parser = new QueryParser(Version.LUCENE_42, "content", analyzer);
        Query query = parser.parse("文本文件");
        System.out.println(query.toString("content"));
        TopDocs results = searcher.search(query, 100);
        ScoreDoc[] scoreDocs = results.scoreDocs;
        System.out.println(results.totalHits);
        for (ScoreDoc scoreDoc : scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            System.out.println(doc.get("path"));
        }
        reader.close();
    }

    @Test
    public void testToken() throws Exception {
        Analyzer analyzer = new FullTwoTokenAnalyzer(Version.LUCENE_42);
        List<String> token = new ArrayList<String>();
        String keywords = "_new中文字符Term(IndexedField.PATH_FOR_SEARCH.getCode(), s";
        TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(keywords));
        while (tokenStream.incrementToken()) {
            CharTermAttribute attribute = tokenStream.getAttribute(CharTermAttribute.class);
            token.add(attribute.toString().trim());
        }
        System.out.println(token);
        FullTwoTokenAnalyzer analyzer2 = new FullTwoTokenAnalyzer(Version.LUCENE_42);
        analyzer2.setForSearch(true);
        List<String> token2 = new ArrayList<String>();
        String keywords2 = "_new中文字符Term(IndexedField.PATH_FOR_SEARCH.getCode(), s";
        TokenStream tokenStream2 = analyzer2.tokenStream(null, new StringReader(keywords2));
        while (tokenStream2.incrementToken()) {
            CharTermAttribute attribute = tokenStream2.getAttribute(CharTermAttribute.class);
            token2.add(attribute.toString().trim());
        }
        System.out.println(token2);
    }

}
