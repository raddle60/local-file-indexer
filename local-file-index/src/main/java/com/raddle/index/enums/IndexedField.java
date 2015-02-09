package com.raddle.index.enums;

public enum IndexedField {
    /**
     * "name","文件名"
     */
    NAME("name", "文件名"),
    /**
     * "path","路径"
     */
    PATH("path", "路径"),
    /**
     * "path_for_search","搜索路径"
     */
    PATH_FOR_SEARCH("path_for_search", "搜索路径"),
    /**
     * "modified","修改时间"
     */
    MODIFIED("modified", "修改时间"),
    /**
     * "length","文件大小"
     */
    LENGTH("length", "文件大小"),
    /**
     * "content", "文件内容"
     */
    CONTENT("content", "文件内容"),
    /**
     * "extension","文件后缀"
     */
    EXTENSION("extension", "文件后缀"),
    /**
     * "encoding","文件编码"
     */
    ENCODING("encoding", "文件编码"), ;

    private final String code;
    private final String desc;

    private IndexedField(String code, String desc){
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
