package com.raddle.file.gui.option;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import com.raddle.config.ConfigManager;
import com.raddle.index.config.GlobalConfig;

public class OptionDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private JTextField textSuffixTxt;
    private ConfigManager configManager;
    private JTextField ignoreFileTxt;
    private JTextField ignoreDirTxt;
    private JTextArea syntaxArea;
    private JTextField cmdTxt;
    private JTextField txtEditorTxt;
    private JTextField fileManagerTxt;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    OptionDialog dialog = new OptionDialog();
                    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    dialog.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the dialog.
     */
    public OptionDialog(){
        setBounds(100, 100, 579, 450);
        getContentPane().setLayout(null);

        JLabel label = new JLabel("文本文件扩展名");
        label.setBounds(10, 10, 94, 15);
        getContentPane().add(label);

        textSuffixTxt = new JTextField();
        textSuffixTxt.setBounds(114, 7, 447, 21);
        getContentPane().add(textSuffixTxt);
        textSuffixTxt.setColumns(10);

        JButton saveBtn = new JButton("保存");
        saveBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (StringUtils.isBlank(textSuffixTxt.getText())) {
                    JOptionPane.showMessageDialog(OptionDialog.this, "文本文件后缀名为空");
                    return;
                }
                GlobalConfig globalConfig = configManager.getGlobalConfig();
                globalConfig.setTxtFileExts(Arrays.asList(StringUtils.split(textSuffixTxt.getText(), ",")));
                globalConfig.setIgnoreFiles(Arrays.asList(StringUtils.split(ignoreFileTxt.getText(), ",")));
                globalConfig.setIgnoreDirs(Arrays.asList(StringUtils.split(ignoreDirTxt.getText(), ",")));
                BufferedReader reader = new BufferedReader(new StringReader(syntaxArea.getText()));
                Map<String, List<String>> syntaxStyles = new HashMap<String, List<String>>();
                try {
                    String readLine = reader.readLine();
                    while (readLine != null) {
                        String[] split = StringUtils.split(readLine, ":");
                        if (split.length > 1 && StringUtils.isNotBlank(split[0]) && StringUtils.isNotBlank(split[1])) {
                            syntaxStyles.put(split[0], new ArrayList<String>(Arrays.asList(StringUtils.split(split[1], ","))));
                        }
                        readLine = reader.readLine();
                    }
                } catch (IOException e1) {
                    throw new RuntimeException(e1.getMessage(), e1);
                }
                globalConfig.setSyntaxStyles(syntaxStyles);
                globalConfig.setCmdCommandLine(cmdTxt.getText());
                globalConfig.setTxtEditorCommandLine(txtEditorTxt.getText());
                globalConfig.setFileManagerCommandLine(fileManagerTxt.getText());
                configManager.saveGlobalConfig(globalConfig);
                JOptionPane.showMessageDialog(OptionDialog.this, "保存成功");
                OptionDialog.this.setVisible(false);
            }
        });
        saveBtn.setBounds(114, 385, 93, 23);
        getContentPane().add(saveBtn);

        JButton cancelBtn = new JButton("取消");
        cancelBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                OptionDialog.this.setVisible(false);
            }
        });
        cancelBtn.setBounds(225, 385, 93, 23);
        getContentPane().add(cancelBtn);

        JLabel label_1 = new JLabel("忽略的文件名");
        label_1.setBounds(10, 35, 90, 15);
        getContentPane().add(label_1);

        JLabel label_2 = new JLabel("忽略的目录名");
        label_2.setBounds(10, 60, 79, 15);
        getContentPane().add(label_2);

        ignoreFileTxt = new JTextField();
        ignoreFileTxt.setBounds(114, 32, 447, 21);
        getContentPane().add(ignoreFileTxt);
        ignoreFileTxt.setColumns(10);

        ignoreDirTxt = new JTextField();
        ignoreDirTxt.setBounds(114, 57, 447, 21);
        getContentPane().add(ignoreDirTxt);
        ignoreDirTxt.setColumns(10);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(114, 88, 447, 211);
        getContentPane().add(scrollPane);

        syntaxArea = new JTextArea();
        scrollPane.setViewportView(syntaxArea);

        JLabel label_3 = new JLabel("语法类型扩展名");
        label_3.setBounds(10, 85, 94, 15);
        getContentPane().add(label_3);

        JLabel label_4 = new JLabel("命令行{0}路径");
        label_4.setBounds(10, 306, 94, 15);
        getContentPane().add(label_4);

        cmdTxt = new JTextField();
        cmdTxt.setBounds(114, 303, 447, 21);
        getContentPane().add(cmdTxt);
        cmdTxt.setColumns(10);

        JLabel label_5 = new JLabel("文本编辑器");
        label_5.setBounds(10, 331, 94, 15);
        getContentPane().add(label_5);

        txtEditorTxt = new JTextField();
        txtEditorTxt.setBounds(114, 328, 447, 21);
        getContentPane().add(txtEditorTxt);
        txtEditorTxt.setColumns(10);

        JLabel label_6 = new JLabel("文件管理器");
        label_6.setBounds(10, 356, 94, 15);
        getContentPane().add(label_6);

        fileManagerTxt = new JTextField();
        fileManagerTxt.setBounds(114, 353, 447, 21);
        getContentPane().add(fileManagerTxt);
        fileManagerTxt.setColumns(10);

    }

    public void initData() {
        GlobalConfig globalConfig = configManager.getGlobalConfig();
        if (globalConfig != null) {
            if (globalConfig.getTxtFileExts() != null) {
                textSuffixTxt.setText(StringUtils.join(globalConfig.getTxtFileExts(), ","));
            }
            if (globalConfig.getIgnoreFiles() != null) {
                ignoreFileTxt.setText(StringUtils.join(globalConfig.getIgnoreFiles(), ","));
            }
            if (globalConfig.getIgnoreDirs() != null) {
                ignoreDirTxt.setText(StringUtils.join(globalConfig.getIgnoreDirs(), ","));
            }
        }
        Field[] fields = SyntaxConstants.class.getFields();
        StringBuilder sb = new StringBuilder();
        for (Field field : fields) {
            try {
                String syntaxType = field.get(SyntaxConstants.class) + "";
                sb.append(syntaxType).append(":");
                Map<String, List<String>> syntaxStyles = globalConfig.getSyntaxStyles();
                if (syntaxStyles != null) {
                    List<String> list = syntaxStyles.get(syntaxType);
                    if (list != null) {
                        sb.append(StringUtils.join(list, ","));
                    }
                }
                sb.append("\n");
            } catch (Exception e) {
            }
        }
        syntaxArea.setText(sb.toString());
        cmdTxt.setText(StringUtils.defaultString(globalConfig.getCmdCommandLine()));
        txtEditorTxt.setText(StringUtils.defaultString(globalConfig.getTxtEditorCommandLine()));
        fileManagerTxt.setText(StringUtils.defaultString(globalConfig.getFileManagerCommandLine()));
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }
}
