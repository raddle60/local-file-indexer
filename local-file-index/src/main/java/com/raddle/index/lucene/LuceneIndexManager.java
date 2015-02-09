package com.raddle.index.lucene;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.omg.CORBA.IntHolder;

import com.raddle.config.ConfigManager;
import com.raddle.index.IndexManager;
import com.raddle.index.config.GlobalConfig;
import com.raddle.index.config.IndexConfig;
import com.raddle.index.enums.IndexedField;
import com.raddle.index.lucene.analyzer.FullTwoTokenAnalyzer;
import com.raddle.index.observer.ProgressObserver;
import com.raddle.index.util.CharSetUtils;

/**
 * 类LuceneIndexManager.java的实现描述：
 * @author raddle60 2013-5-6 下午9:26:44
 */
public class LuceneIndexManager implements IndexManager {

    private ConfigManager configManager;

    private static final long MAX_TEXT_CONTENT_SIZE = 10 * 1024 * 1024;

    @Override
    public void rebuildIndex(String docDir, final ProgressObserver progressObserver, IndexConfig indexConfig) {
        indexDir(docDir, progressObserver, indexConfig, true);
    }

    @Override
    public void updateIndex(String docDir, ProgressObserver progressObserver, IndexConfig indexConfig) {
        indexDir(docDir, progressObserver, indexConfig, false);
    }

    @SuppressWarnings("unchecked")
    private void indexDir(String docDir, final ProgressObserver progressObserver, IndexConfig indexConfig, boolean rebuild) {
        final File docDirFile = new File(docDir);
        if (!docDirFile.exists() || !docDirFile.canRead()) {
            throw new RuntimeException("文件目录不存在");
        }
        GlobalConfig globalConfig = configManager.getGlobalConfig();
        final Set<String> indexFileExts = unionCollection(indexConfig.getIndexNameExts());
        final Set<String> indexContentExts = unionCollection(indexConfig.getIndexContentExts());
        final Set<String> textFileExts = unionCollection(globalConfig.getTxtFileExts());
        final Set<String> ignoreFileNames = unionCollection(globalConfig.getIgnoreFiles(), indexConfig.getIgnoreFiles());
        final Set<String> ignoreDirNames = unionCollection(globalConfig.getIgnoreDirs(), indexConfig.getIgnoreDirs());
        final IntHolder total = new IntHolder();
        Collection<File> matchedFiles = null;
        try {
            matchedFiles = FileUtils.listFiles(docDirFile, new AbstractFileFilter() {

                @Override
                public boolean accept(File file) {
                    total.value++;
                    if (progressObserver != null) {
                        progressObserver.fileCollecting(total.value);
                        if (progressObserver.isTerminated()) {
                            throw new RuntimeException("Terminated");
                        }
                    }
                    if (indexFileExts.size() > 0) {
                        // 不索引的扩展名
                        if (!indexFileExts.contains(FilenameUtils.getExtension(file.getName()))) {
                            return false;
                        }
                    }
                    if (ignoreFileNames.size() > 0) {
                        // 忽略的文件名
                        for (String ignoreFileName : ignoreFileNames) {
                            if (FilenameUtils.wildcardMatch(file.getName(), ignoreFileName)) {
                                return false;
                            }
                        }
                    }
                    return true;
                }
            }, new AbstractFileFilter() {

                @Override
                public boolean accept(File file) {
                    if (ignoreDirNames.size() > 0) {
                        // 忽略的目录名
                        for (String ignoreDirName : ignoreDirNames) {
                            if (FilenameUtils.wildcardMatch(file.getName(), ignoreDirName)) {
                                return false;
                            }
                        }
                    }
                    return true;
                }
            });
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Terminated")) {
                if (progressObserver != null) {
                    progressObserver.fileIndexed(0, true);
                }
            } else {
                throw e;
            }
        }
        if (progressObserver != null) {
            progressObserver.fileCollected(matchedFiles.size(), total.value, total.value - matchedFiles.size());
        }
        Directory dir = null;
        try {
            if (!new File(indexConfig.getIndexSaveDir()).exists()) {
                new File(indexConfig.getIndexSaveDir()).mkdirs();
            }
            dir = FSDirectory.open(new File(indexConfig.getIndexSaveDir()));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        Analyzer analyzer = new FullTwoTokenAnalyzer(Version.LUCENE_42);
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_42, analyzer);
        IndexWriter writer = null;
        DirectoryReader reader = null;
        try {
            IndexSearcher searcher = null;
            if (rebuild) {
                iwc.setOpenMode(OpenMode.CREATE);
            } else {
                iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
                reader = DirectoryReader.open(FSDirectory.open(new File(indexConfig.getIndexSaveDir())));
                searcher = new IndexSearcher(reader);
            }
            writer = new IndexWriter(dir, iwc);
            int i = 0;
            boolean terminated = false;
            for (File file : matchedFiles) {
                i++;
                if (file.canRead()) {
                    if (!rebuild) {
                        // 更新，先获得已有的文件
                        Query query = new TermQuery(new Term(IndexedField.PATH.getCode(), FilenameUtils.normalize(file.getPath(), true)));
                        TopDocs results = searcher.search(query, 1);
                        if (results.totalHits > 0) {
                            Document doc = searcher.doc(results.scoreDocs[0].doc);
                            long length = Long.parseLong(doc.get(IndexedField.LENGTH.getCode()));
                            long modified = Long.parseLong(doc.get(IndexedField.MODIFIED.getCode()));
                            if (length == file.length() && modified == file.lastModified()) {
                                // 文件没改变
                                if (progressObserver != null) {
                                    progressObserver.fileIndexing(i);
                                    if (progressObserver.isTerminated()) {
                                        terminated = true;
                                        break;
                                    }
                                }
                                continue;
                            }
                        }
                    }
                    ///////
                    Document doc = new Document();
                    doc.add(new TextField(IndexedField.NAME.getCode(), FilenameUtils.getBaseName(file.getName()), Field.Store.YES));
                    doc.add(new StringField(IndexedField.EXTENSION.getCode(), FilenameUtils.getExtension(file.getName()), Field.Store.YES));
                    doc.add(new StringField(IndexedField.PATH.getCode(), FilenameUtils.normalize(file.getPath(), true), Field.Store.YES));
                    doc.add(new TextField(IndexedField.PATH_FOR_SEARCH.getCode(), FilenameUtils.normalize(file.getPath(), true), Field.Store.NO));
                    doc.add(new LongField(IndexedField.LENGTH.getCode(), file.length(), Field.Store.YES));
                    doc.add(new LongField(IndexedField.MODIFIED.getCode(), file.lastModified(), Field.Store.YES));
                    if (indexContentExts.size() == 0 || indexContentExts.contains(FilenameUtils.getExtension(file.getName()))) {
                        // 未指定或匹配指定扩展名
                        if (textFileExts.contains(FilenameUtils.getExtension(file.getName())) && file.length() < MAX_TEXT_CONTENT_SIZE) {
                            // 是文本文件，就读取内容,只索引，不存储
                            byte[] bytes = FileUtils.readFileToByteArray(file);
                            String encoding = StringUtils.defaultIfBlank(CharSetUtils.detectCharset(bytes), "ISO-8859-1");
                            doc.add(new StringField(IndexedField.ENCODING.getCode(), encoding, Field.Store.YES));
                            doc.add(new TextField(IndexedField.CONTENT.getCode(), new String(bytes, encoding), Field.Store.NO));
                        }
                    }
                    if (rebuild) {
                        writer.addDocument(doc);
                    } else {
                        writer.updateDocument(new Term(IndexedField.PATH.getCode(), FilenameUtils.normalize(file.getPath(), true)), doc);
                    }
                }
                if (progressObserver != null) {
                    progressObserver.fileIndexing(i);
                    if (progressObserver.isTerminated()) {
                        terminated = true;
                        break;
                    }
                }
            }
            if (progressObserver != null) {
                progressObserver.fileIndexed(i, terminated);
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
    }

    private Set<String> unionCollection(Collection<String>... collections) {
        Set<String> ret = new HashSet<String>();
        if (collections != null) {
            for (Collection<String> collection : collections) {
                if (collection != null) {
                    ret.addAll(collection);
                }
            }
        }
        return ret;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

}
