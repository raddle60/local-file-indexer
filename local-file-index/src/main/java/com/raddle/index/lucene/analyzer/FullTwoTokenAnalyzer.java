package com.raddle.index.lucene.analyzer;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.util.Version;

import com.raddle.index.lucene.tokenizer.FullTwoTokenizer;

/**
 * 类StandardNoSymbolAnalyzer.java的实现描述：任意符号分割
 * @author raddle60 2013-5-6 下午10:59:23
 */
public class FullTwoTokenAnalyzer extends StopwordAnalyzerBase {

    private boolean forSearch = false;
    /** An unmodifiable set containing some common English words that are usually not
    useful for searching. */
    public static final CharArraySet STOP_WORDS_SET = StopAnalyzer.ENGLISH_STOP_WORDS_SET;

    /** Builds an analyzer with the given stop words.
     * @param matchVersion Lucene version to match See {@link
     * <a href="#version">above</a>}
     * @param stopWords stop words */
    public FullTwoTokenAnalyzer(Version matchVersion, CharArraySet stopWords){
        super(matchVersion, stopWords);
    }

    /** Builds an analyzer with the default stop words ({@link
     * #STOP_WORDS_SET}).
     * @param matchVersion Lucene version to match See {@link
     * <a href="#version">above</a>}
     */
    public FullTwoTokenAnalyzer(Version matchVersion){
        this(matchVersion, STOP_WORDS_SET);
    }

    /** Builds an analyzer with the stop words from the given reader.
     * @see WordlistLoader#getWordSet(Reader, Version)
     * @param matchVersion Lucene version to match See {@link
     * <a href="#version">above</a>}
     * @param stopwords Reader to read stop words from */
    public FullTwoTokenAnalyzer(Version matchVersion, Reader stopwords) throws IOException{
        this(matchVersion, loadStopwordSet(stopwords, matchVersion));
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
        FullTwoTokenizer src = new FullTwoTokenizer(reader);
        src.setForSearch(forSearch);
        TokenStream tok = new StandardFilter(matchVersion, src);
        tok = new LowerCaseFilter(matchVersion, tok);
        tok = new StopFilter(matchVersion, tok, stopwords);
        return new TokenStreamComponents(src, tok) {

            @Override
            protected void setReader(final Reader reader) throws IOException {
                super.setReader(reader);
            }
        };
    }

    public boolean isForSearch() {
        return forSearch;
    }

    public void setForSearch(boolean forSearch) {
        this.forSearch = forSearch;
    }
}
