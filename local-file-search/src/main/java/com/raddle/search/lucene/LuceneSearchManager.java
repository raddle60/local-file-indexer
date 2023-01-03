package com.raddle.search.lucene;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.raddle.index.enums.IndexedField;
import com.raddle.index.lucene.analyzer.FullTwoTokenAnalyzer;
import com.raddle.search.SearchManager;
import com.raddle.search.model.SearchResult;

/**
 * 类LuceneSearchManager.java的实现描述：搜索引擎
 * @author raddle60 2013-5-7 下午10:39:59
 */
public class LuceneSearchManager implements SearchManager {

    private boolean opened = false;
    private IndexReader reader;

    public synchronized void openIndex(String[] dirs) {
        if (!opened) {
            if (dirs == null || dirs.length == 0) {
                throw new RuntimeException("索引目录为空");
            }
            for (String dir : dirs) {
                if (!new File(dir).exists()) {
                    throw new RuntimeException("索引目录" + new File(dir).getAbsolutePath() + "不存在");
                }
            }
            try {
                IndexReader[] subReaders = new IndexReader[dirs.length];
                for (int i = 0; i < subReaders.length; i++) {
                    subReaders[i] = DirectoryReader.open(FSDirectory.open(new File(dirs[i])));
                }
                reader = new MultiReader(subReaders, true);
            } catch (IOException e) {
                throw new RuntimeException("打开索引失败," + e.getMessage(), e);
            }
        } else {
            throw new RuntimeException("索引已打开");
        }
    }

    @Override
    public SearchResult search(String keywords, String extension) {
        IndexSearcher searcher = new IndexSearcher(reader);
        FullTwoTokenAnalyzer analyzer = new FullTwoTokenAnalyzer(Version.LUCENE_42);
        analyzer.setForSearch(true);
        try {
            Set<String> tokens = new HashSet<String>();
            TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(keywords));
            while (tokenStream.incrementToken()) {
                CharTermAttribute attribute = tokenStream.getAttribute(CharTermAttribute.class);
                String token = attribute.toString().trim();
                if (StringUtils.isNotEmpty(token)) {
                    tokens.add(token);
                }
            }
            BooleanQuery booleanQuery = new BooleanQuery();
            BooleanQuery booleanNameQuery = new BooleanQuery();
            BooleanQuery booleanContentQuery = new BooleanQuery();
            BooleanQuery booleanPathQuery = new BooleanQuery();
            for (String s : tokens) {
                booleanNameQuery.add(new TermQuery(new Term(IndexedField.NAME.getCode(), s)), BooleanClause.Occur.MUST);
                booleanContentQuery.add(new TermQuery(new Term(IndexedField.CONTENT.getCode(), s)), BooleanClause.Occur.MUST);
                booleanPathQuery.add(new TermQuery(new Term(IndexedField.PATH_FOR_SEARCH.getCode(), s)), BooleanClause.Occur.MUST);
            }
            booleanQuery.add(booleanNameQuery, BooleanClause.Occur.SHOULD);
            booleanQuery.add(booleanContentQuery, BooleanClause.Occur.SHOULD);
            booleanQuery.add(booleanPathQuery, BooleanClause.Occur.SHOULD);
            if (StringUtils.isNotEmpty(extension)) {
                BooleanQuery booleanQueryPre = booleanQuery;
                BooleanQuery booleanExtensionQuery = new BooleanQuery();
                booleanExtensionQuery.add(new TermQuery(new Term(IndexedField.EXTENSION.getCode(), extension)), BooleanClause.Occur.MUST);
                
                // 重新构建2个条件的查询
                booleanQuery = new BooleanQuery();
                booleanQuery.add(booleanQueryPre, BooleanClause.Occur.MUST);
                booleanQuery.add(booleanExtensionQuery, BooleanClause.Occur.MUST);
            }
            //System.out.println(tokens);
            //System.out.println(booleanQuery);
            TopDocs results = searcher.search(booleanQuery, 500);
            SearchResult result = new SearchResult();
            result.setTotalCount(results.totalHits);
            ScoreDoc[] scoreDocs = results.scoreDocs;
            for (ScoreDoc scoreDoc : scoreDocs) {
                Document doc = searcher.doc(scoreDoc.doc);
                Map<IndexedField, Object> record = new HashMap<IndexedField, Object>();
                record.put(IndexedField.NAME, doc.get(IndexedField.NAME.getCode()));
                record.put(IndexedField.PATH, doc.get(IndexedField.PATH.getCode()));
                record.put(IndexedField.EXTENSION, doc.get(IndexedField.EXTENSION.getCode()));
                record.put(IndexedField.LENGTH, doc.get(IndexedField.LENGTH.getCode()));
                record.put(IndexedField.MODIFIED, doc.get(IndexedField.MODIFIED.getCode()));
                record.put(IndexedField.ENCODING, doc.get(IndexedField.ENCODING.getCode()));
                result.getResults().add(record);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("搜索失败," + e.getMessage(), e);
        } finally {
            analyzer.close();
        }
    }

    @Override
    public String[] parseForInFileSearch(String keywords) {
        return StringUtils.split(keywords, " ");
    }

    public synchronized void closeIndex() {
        if (opened) {
            try {
                reader.close();
            } catch (IOException e) {
                throw new RuntimeException("关闭索引失败," + e.getMessage(), e);
            }
        }
    }
}
