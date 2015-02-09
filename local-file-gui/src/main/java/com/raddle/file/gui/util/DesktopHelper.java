package com.raddle.file.gui.util;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import com.raddle.index.config.GlobalConfig;

/**
 * 类DesktopHelper.java的实现描述：
 * @author raddle60 2013-5-19 下午1:48:12
 */
public class DesktopHelper {

    private GlobalConfig globalConfig;

    public DesktopHelper(GlobalConfig globalConfig){
        this.globalConfig = globalConfig;
    }

    public void open(File file) {
        try {
            if (globalConfig.getTxtFileExts() != null && globalConfig.getTxtFileExts().contains(FilenameUtils.getExtension(file.getName())) && StringUtils.isNotEmpty(globalConfig.getTxtEditorCommandLine())) {
                Runtime.getRuntime().exec(MessageFormat.format(globalConfig.getTxtEditorCommandLine(), file.getAbsolutePath()));
            } else {
                Desktop desktop = Desktop.getDesktop();
                desktop.open(file);
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void openDir(File dir) {
        try {
            File openDir = dir;
            if (!dir.isDirectory()) {
                openDir = dir.getParentFile();
            }
            if (StringUtils.isEmpty(globalConfig.getFileManagerCommandLine())) {
                Desktop desktop = Desktop.getDesktop();
                desktop.open(openDir);
            } else {
                Runtime.getRuntime().exec(MessageFormat.format(globalConfig.getFileManagerCommandLine(), openDir.getAbsolutePath()));
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void openCmd(File dir) {
        File cmdDir = dir;
        if (!dir.isDirectory()) {
            cmdDir = dir.getParentFile();
        }
        if (StringUtils.isEmpty(globalConfig.getCmdCommandLine())) {
            throw new RuntimeException("请在选项中配置命令行");
        }
        try {
            Runtime.getRuntime().exec(MessageFormat.format(globalConfig.getCmdCommandLine(), cmdDir.getAbsolutePath()));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
