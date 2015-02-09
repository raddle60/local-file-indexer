package com.raddle.file.gui.index;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import com.raddle.config.ConfigManager;
import com.raddle.index.config.GlobalConfig;
import com.raddle.index.config.IndexConfig;

public class IndexConfigEdit extends JDialog {

    private static final long serialVersionUID = 1L;
    private JTextField nameTxt;
    private JTextField fileDirTxt;
    private JTextField indexDirTxt;
    private IndexConfig indexConfig;
    private ConfigManager configManager;
    private JTextField nameExtTxt;
    private JTextField contentExtTxt;
    private JTextField ignoreFileTxt;
    private JTextField ignoreDirTxt;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            IndexConfigEdit dialog = new IndexConfigEdit();
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the dialog.
     */
    public IndexConfigEdit(){
        setBounds(100, 100, 715, 285);
        getContentPane().setLayout(null);

        JLabel label = new JLabel("名称");
        label.setBounds(20, 10, 54, 15);
        getContentPane().add(label);

        JLabel label_1 = new JLabel("文件目录");
        label_1.setBounds(20, 35, 68, 15);
        getContentPane().add(label_1);

        JLabel label_2 = new JLabel("索引存储目录");
        label_2.setBounds(20, 60, 90, 15);
        getContentPane().add(label_2);

        nameTxt = new JTextField();
        nameTxt.setBounds(115, 7, 236, 21);
        getContentPane().add(nameTxt);
        nameTxt.setColumns(10);

        fileDirTxt = new JTextField();
        fileDirTxt.setBounds(115, 32, 445, 21);
        getContentPane().add(fileDirTxt);
        fileDirTxt.setColumns(10);

        indexDirTxt = new JTextField();
        indexDirTxt.setBounds(115, 57, 445, 21);
        getContentPane().add(indexDirTxt);
        indexDirTxt.setColumns(10);

        JButton fileDirBtn = new JButton("浏览");
        fileDirBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(fileDirTxt.getText());
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.setName("选择文件目录");
                GlobalConfig globalConfig = configManager.getGlobalConfig();
                if (StringUtils.isNotBlank(globalConfig.getLastContenParentDir())) {
                    fileChooser.setCurrentDirectory(new File(globalConfig.getLastContenParentDir()));
                }
                int result = fileChooser.showOpenDialog(IndexConfigEdit.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    fileDirTxt.setText(fileChooser.getSelectedFile().getAbsolutePath());
                    String baseName = FilenameUtils.getBaseName(fileChooser.getSelectedFile().getAbsolutePath());
                    if (StringUtils.isEmpty(nameTxt.getText())) {
                        nameTxt.setText(baseName);
                    }
                    if (StringUtils.isNotEmpty(indexDirTxt.getText())) {
                        indexDirTxt.setText(indexDirTxt.getText() + "/" + baseName);
                    }
                }
            }
        });
        fileDirBtn.setBounds(570, 31, 93, 23);
        getContentPane().add(fileDirBtn);

        JButton indexDirBtn = new JButton("浏览");
        indexDirBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(indexDirTxt.getText());
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.setName("选择索引存储目录");
                int result = fileChooser.showOpenDialog(IndexConfigEdit.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    indexDirTxt.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });
        indexDirBtn.setBounds(570, 56, 93, 23);
        getContentPane().add(indexDirBtn);

        JButton saveBtn = new JButton("保存");
        saveBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (StringUtils.isBlank(nameTxt.getText())) {
                    JOptionPane.showMessageDialog(IndexConfigEdit.this, "索引名称为空");
                    return;
                }
                if (StringUtils.isBlank(fileDirTxt.getText())) {
                    JOptionPane.showMessageDialog(IndexConfigEdit.this, "文件目录为空");
                    return;
                }
                if (!new File(fileDirTxt.getText()).exists()) {
                    JOptionPane.showMessageDialog(IndexConfigEdit.this, "文件目录" + new File(fileDirTxt.getText()).getAbsolutePath() + "不存在");
                    return;
                }
                if (StringUtils.isBlank(indexDirTxt.getText())) {
                    JOptionPane.showMessageDialog(IndexConfigEdit.this, "索引存储目录为空");
                    return;
                }
                if (indexConfig == null) {
                    indexConfig = new IndexConfig();
                }
                indexConfig.setName(nameTxt.getText());
                indexConfig.setFileDir(fileDirTxt.getText());
                indexConfig.setIndexSaveDir(indexDirTxt.getText());
                indexConfig.setIndexNameExts(Arrays.asList(StringUtils.split(nameExtTxt.getText(), ",")));
                indexConfig.setIndexContentExts(Arrays.asList(StringUtils.split(contentExtTxt.getText(), ",")));
                indexConfig.setIgnoreFiles(Arrays.asList(StringUtils.split(ignoreFileTxt.getText(), ",")));
                indexConfig.setIgnoreDirs(Arrays.asList(StringUtils.split(ignoreDirTxt.getText(), ",")));
                configManager.saveOrUpdateIndexConfig(indexConfig);
                GlobalConfig globalConfig = configManager.getGlobalConfig();
                globalConfig.setLastContenParentDir(new File(fileDirTxt.getText()).getParent());
                globalConfig.setLastIndexParentDir(new File(indexDirTxt.getText()).getParent());
                JOptionPane.showMessageDialog(IndexConfigEdit.this, "保存成功");
                IndexConfigEdit.this.setVisible(false);
            }
        });
        saveBtn.setBounds(115, 202, 93, 23);
        getContentPane().add(saveBtn);

        JButton cancelBtn = new JButton("取消");
        cancelBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                IndexConfigEdit.this.setVisible(false);
            }
        });
        cancelBtn.setBounds(233, 202, 93, 23);
        getContentPane().add(cancelBtn);

        JLabel label_3 = new JLabel("索引扩展名");
        label_3.setBounds(20, 85, 90, 15);
        getContentPane().add(label_3);

        JLabel label_4 = new JLabel("索引内容扩展名");
        label_4.setBounds(20, 110, 90, 15);
        getContentPane().add(label_4);

        nameExtTxt = new JTextField();
        nameExtTxt.setBounds(115, 82, 548, 21);
        getContentPane().add(nameExtTxt);
        nameExtTxt.setColumns(10);

        contentExtTxt = new JTextField();
        contentExtTxt.setBounds(115, 107, 548, 21);
        getContentPane().add(contentExtTxt);
        contentExtTxt.setColumns(10);

        JLabel label_5 = new JLabel("忽略的文件名");
        label_5.setBounds(20, 135, 90, 15);
        getContentPane().add(label_5);

        JLabel label_6 = new JLabel("忽略的目录名");
        label_6.setBounds(20, 160, 90, 15);
        getContentPane().add(label_6);

        ignoreFileTxt = new JTextField();
        ignoreFileTxt.setBounds(115, 132, 548, 21);
        getContentPane().add(ignoreFileTxt);
        ignoreFileTxt.setColumns(10);

        ignoreDirTxt = new JTextField();
        ignoreDirTxt.setBounds(115, 157, 548, 21);
        getContentPane().add(ignoreDirTxt);
        ignoreDirTxt.setColumns(10);
    }

    public void initData() {
        if (indexConfig != null) {
            nameTxt.setText(indexConfig.getName());
            fileDirTxt.setText(indexConfig.getFileDir());
            indexDirTxt.setText(indexConfig.getIndexSaveDir());
            if (indexConfig.getIndexNameExts() != null) {
                nameExtTxt.setText(StringUtils.join(indexConfig.getIndexNameExts(), ","));
            }
            if (indexConfig.getIndexContentExts() != null) {
                contentExtTxt.setText(StringUtils.join(indexConfig.getIndexContentExts(), ","));
            }
            if (indexConfig.getIgnoreFiles() != null) {
                ignoreFileTxt.setText(StringUtils.join(indexConfig.getIgnoreFiles(), ","));
            }
            if (indexConfig.getIgnoreDirs() != null) {
                ignoreDirTxt.setText(StringUtils.join(indexConfig.getIgnoreDirs(), ","));
            }
        } else {
            GlobalConfig globalConfig = configManager.getGlobalConfig();
            if (StringUtils.isNotBlank(globalConfig.getLastIndexParentDir())) {
                indexDirTxt.setText(globalConfig.getLastIndexParentDir());
            }
        }
    }

    public IndexConfig getIndexConfig() {
        return indexConfig;
    }

    public void setIndexConfig(IndexConfig indexConfig) {
        this.indexConfig = indexConfig;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }
}
