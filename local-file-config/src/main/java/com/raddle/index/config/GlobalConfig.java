package com.raddle.index.config;

import java.util.List;
import java.util.Map;

/**
 * 类GlobalConfig.java的实现描述：全局配置
 * @author raddle60 2013-5-11 下午6:04:43
 */
public class GlobalConfig {

    /**
     * 文本文件扩展名
     */
    private List<String> txtFileExts;
    /**
     * 忽略的文件
     */
    private List<String> ignoreFiles;
    /**
     * 忽略的目录
     */
    private List<String> ignoreDirs;

    private Map<String, List<String>> syntaxStyles;

    private List<String> selectedIndexs;

    private String lastContenParentDir;

    private String lastIndexParentDir;

    private String cmdCommandLine;

    private String txtEditorCommandLine;

    private String fileManagerCommandLine;

    public List<String> getTxtFileExts() {
        return txtFileExts;
    }

    public void setTxtFileExts(List<String> txtFileExts) {
        this.txtFileExts = txtFileExts;
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

    public Map<String, List<String>> getSyntaxStyles() {
        return syntaxStyles;
    }

    public void setSyntaxStyles(Map<String, List<String>> syntaxStyles) {
        this.syntaxStyles = syntaxStyles;
    }

    public List<String> getSelectedIndexs() {
        return selectedIndexs;
    }

    public void setSelectedIndexs(List<String> selectedIndexs) {
        this.selectedIndexs = selectedIndexs;
    }

    public String getLastContenParentDir() {
        return lastContenParentDir;
    }

    public void setLastContenParentDir(String lastContenParentDir) {
        this.lastContenParentDir = lastContenParentDir;
    }

    public String getLastIndexParentDir() {
        return lastIndexParentDir;
    }

    public void setLastIndexParentDir(String lastIndexParentDir) {
        this.lastIndexParentDir = lastIndexParentDir;
    }

    public String getCmdCommandLine() {
        return cmdCommandLine;
    }

    public void setCmdCommandLine(String openCmdCommandLine) {
        this.cmdCommandLine = openCmdCommandLine;
    }

    public String getFileManagerCommandLine() {
        return fileManagerCommandLine;
    }

    public void setFileManagerCommandLine(String fileManagerCommandLine) {
        this.fileManagerCommandLine = fileManagerCommandLine;
    }

    public String getTxtEditorCommandLine() {
        return txtEditorCommandLine;
    }

    public void setTxtEditorCommandLine(String txtEditorCommandLine) {
        this.txtEditorCommandLine = txtEditorCommandLine;
    }

}
