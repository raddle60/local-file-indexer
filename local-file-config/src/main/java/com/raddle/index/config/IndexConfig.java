package com.raddle.index.config;

import java.util.List;

public class IndexConfig {

    private String id;

    private String name;

    private String fileDir;

    private String indexSaveDir;

    private long indexedCount;
    /**
     * 索引的扩展名
     */
    private List<String> indexNameExts;
    /**
     * 索引内容的扩展名
     */
    private List<String> indexContentExts;
    /**
     * 忽略的文件
     */
    private List<String> ignoreFiles;
    /**
     * 忽略的目录
     */
    private List<String> ignoreDirs;

    public String getIndexSaveDir() {
        return indexSaveDir;
    }

    public void setIndexSaveDir(String indexSaveDir) {
        this.indexSaveDir = indexSaveDir;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileDir() {
        return fileDir;
    }

    public void setFileDir(String fileDir) {
        this.fileDir = fileDir;
    }

    @Override
    public String toString() {
        return name;
    }

    public long getIndexedCount() {
        return indexedCount;
    }

    public void setIndexedCount(long indexedCount) {
        this.indexedCount = indexedCount;
    }

    public List<String> getIgnoreFiles() {
        return ignoreFiles;
    }

    public void setIgnoreFiles(List<String> ignoreFiles) {
        this.ignoreFiles = ignoreFiles;
    }

    public List<String> getIgnoreDirs() {
        return ignoreDirs;
    }

    public void setIgnoreDirs(List<String> ignoreDirs) {
        this.ignoreDirs = ignoreDirs;
    }

    public List<String> getIndexNameExts() {
        return indexNameExts;
    }

    public void setIndexNameExts(List<String> indexNameExts) {
        this.indexNameExts = indexNameExts;
    }

    public List<String> getIndexContentExts() {
        return indexContentExts;
    }

    public void setIndexContentExts(List<String> indexContentExts) {
        this.indexContentExts = indexContentExts;
    }
}
