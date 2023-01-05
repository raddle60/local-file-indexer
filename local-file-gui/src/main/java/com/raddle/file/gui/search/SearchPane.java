package com.raddle.file.gui.search;

import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

import com.raddle.config.file.JsonFileConfigManager;
import com.raddle.file.gui.index.IndexSelectDialog;
import com.raddle.file.gui.table.SimpleTableModel;
import com.raddle.file.gui.util.DesktopHelper;
import com.raddle.file.gui.util.TableUtils;
import com.raddle.index.config.GlobalConfig;
import com.raddle.index.config.IndexConfig;
import com.raddle.index.enums.IndexedField;
import com.raddle.index.util.CharSetUtils;
import com.raddle.search.lucene.LuceneSearchManager;
import com.raddle.search.model.SearchResult;
import com.raddle.swing.layout.LayoutUtils;
import com.raddle.swing.progress.Progress;
import com.raddle.swing.progress.ProgressUtils;
import com.raddle.swing.progress.single.ProgressCallback;
import com.raddle.util.ObjectHolder;

public class SearchPane extends JPanel {

    private static final long serialVersionUID = 1L;
    private JFrame outFrame;
    private JTextField keywordTxt;
    private JTable resultTable;
    private LuceneSearchManager luceneSearchManager;
    private JsonFileConfigManager configManager;
    private RSyntaxTextArea textArea;
    private RTextScrollPane sp;
    private String currentPath;
    private JTextField fileKeyworkTxt;
    private JCheckBox regexChk;
    private JCheckBox highLightChk;
    private JLabel markLeb;
    private JLabel countLeb;
    private TabTitlePanel tabTitlePanel;
    private List<String> selectedIndexs = new ArrayList<String>();
    private JTextField resultKeyword;
    private SearchResult searchResult;
    private JButton searchInResultBtn;
    private static final long MAX_TEXT_CONTENT_SIZE = 10 * 1024 * 1024;
    private List<Map<IndexedField, Object>> currentResults = null;
    private SearchContext currentSearchContext = new SearchContext();
    private JButton searchInResultResetBtn;
    private ObjectHolder<Boolean> isSearchingInResult = new ObjectHolder<Boolean>(Boolean.FALSE);
    private int inResultSearchTimes = 0;
    private JLabel charsetLeb;
    private final JPopupMenu resultTableMenu = new JPopupMenu();
    private Point resultTablePopPoint = null;
    private JMenuItem copyFileNameNoExt;
    private JMenuItem copyFileName;
    private JMenuItem copyFilePath;
    private JLabel label_1;
    private JTextField resultExcludeTxt;
    private JTextField extensionTxt;

    /**
     * Create the panel.
     */
    public SearchPane() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        setLayout(null);
        keywordTxt = new JTextField();
        keywordTxt.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    search();
                }
            }

        });
        keywordTxt.setBounds(10, 6, 392, 23);
        add(keywordTxt);
        keywordTxt.setColumns(10);

        JButton searchBtn = new JButton("搜索");
        searchBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                search();
            }
        });
        searchBtn.setBounds(546, 6, 93, 23);
        add(searchBtn);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane.setBounds(10, 60, 860, 367);
        splitPane.setDividerLocation(180);
        add(splitPane);

        JScrollPane resultPane = new JScrollPane();
        splitPane.setTopComponent(resultPane);

        textArea = new RSyntaxTextArea();
        textArea.setEditable(false);
        textArea.setMarkOccurrences(true);
        sp = new RTextScrollPane(textArea);

        resultTable = new JTable();
        resultTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
                    // 右键弹出菜单
                    resultTableMenu.show(resultTable, e.getX(), e.getY());
                    resultTablePopPoint = new Point(e.getX(), e.getY());
                    int row = resultTable.rowAtPoint(resultTablePopPoint);
                    if (ArrayUtils.contains(resultTable.getSelectedRows(), row)) {
                        copyFileNameNoExt.setEnabled(true);
                        copyFileName.setEnabled(true);
                        copyFilePath.setEnabled(true);
                    } else {
                        copyFileNameNoExt.setEnabled(false);
                        copyFileName.setEnabled(false);
                        copyFilePath.setEnabled(false);
                    }
                }
            }
        });
        resultTable.setModel(new SimpleTableModel());
        DefaultTableModel model = (DefaultTableModel) resultTable.getModel();
        model.addColumn("文件名");
        model.addColumn("文件类型");
        model.addColumn("文件大小");
        model.addColumn("文件编码");
        model.addColumn("文件更新时间");
        model.addColumn("文件路径");
        resultTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        resultTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                int row = resultTable.getSelectedRow();
                if (row != -1) {
                    Map<IndexedField, Object> map = currentResults.get(row);
                    String path = (String) map.get(IndexedField.PATH);
                    if (new File(path).exists() && !StringUtils.equals(currentPath, path)) {
                        currentPath = path;
                        ////
                        String fileSize = (String) map.get(IndexedField.LENGTH);
                        if (fileSize.indexOf("M") != -1 && Double.parseDouble(fileSize.substring(0, fileSize.length() - 1)) > 50) {
                            textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
                            try {
                                textArea.read(new StringReader("文件过大，不能预览"), null);
                            } catch (IOException e1) {
                                throw new RuntimeException(e1.getMessage(), e1);
                            }
                            return;
                        }
                        ////
                        List<String> txtFileExts = configManager.getGlobalConfig().getTxtFileExts();
                        if (txtFileExts != null && txtFileExts.contains(FilenameUtils.getExtension(path))) {
                            try {
                                markLeb.setText("");
                                textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
                                // 确定是否语法高亮
                                Map<String, List<String>> syntaxStyles = configManager.getGlobalConfig().getSyntaxStyles();
                                if (syntaxStyles != null) {
                                    for (Entry<String, List<String>> entry : syntaxStyles.entrySet()) {
                                        if (entry.getValue() != null && entry.getValue().contains(FilenameUtils.getExtension(path))) {
                                            textArea.setSyntaxEditingStyle(entry.getKey());
                                        }
                                    }
                                }
                                byte[] bytes = FileUtils.readFileToByteArray(new File(path));
                                String charset = StringUtils.defaultIfBlank(CharSetUtils.detectCharset(bytes), "ISO-8859-1");
                                charsetLeb.setText("文件编码：" + charset);
                                String content = new String(bytes, charset);
                                if (content.length() > 500 * 1024) {
                                    content = "文件过大，只截取前500k个字符\n" + content;
                                    content = content.substring(0, 500 * 1024);
                                }
                                Reader r = new StringReader(content);
                                textArea.read(r, null);
                                r.close();
                                // 定位到第一个
                                textArea.getCaret().setDot(0);
                                currentSearchContext.setSearchForward(true);
                                org.fife.ui.rtextarea.SearchResult find = SearchEngine.find(textArea, currentSearchContext);
                                markLeb.setText("匹配数：" + find.getMarkedCount());
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        } else {
                            try {
                                textArea.read(new StringReader("文件" + new File(path).getAbsolutePath() + "不是文本文件"), null);
                            } catch (IOException e1) {
                                throw new RuntimeException(e1.getMessage(), e1);
                            }
                            return;
                        }
                    }
                    if (!new File(path).exists()) {
                        try {
                            textArea.read(new StringReader("文件" + new File(path).getAbsolutePath() + "不存在"), null);
                        } catch (IOException e1) {
                            throw new RuntimeException(e1.getMessage(), e1);
                        }
                        return;
                    }
                }
            }
        });

        resultPane.setViewportView(resultTable);

        JMenuItem openFile = new JMenuItem("打开文件");
        openFile.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                int row = resultTable.rowAtPoint(resultTablePopPoint);
                openFile(row);
            }
        });
        resultTableMenu.add(openFile);
        JMenuItem openDir = new JMenuItem("打开目录");
        openDir.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                int row = resultTable.rowAtPoint(resultTablePopPoint);
                openDir(row);
            }
        });
        resultTableMenu.add(openDir);
        JMenuItem openCmd = new JMenuItem("打开命令行");
        openCmd.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                int row = resultTable.rowAtPoint(resultTablePopPoint);
                openCmd(row);
            }
        });
        resultTableMenu.add(openCmd);
        copyFileNameNoExt = new JMenuItem("复制文件名(无扩展名)");
        copyFileNameNoExt.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = resultTable.getSelectedRows();
                StringWriter stringWriter = new StringWriter();
                BufferedWriter writer = new BufferedWriter(stringWriter);
                for (int row : selectedRows) {
                    Map<IndexedField, Object> map = currentResults.get(row);
                    String name = (String) map.get(IndexedField.NAME);
                    try {
                        if (stringWriter.getBuffer().length() > 0) {
                            writer.newLine();
                        }
                        writer.write(name);
                        writer.flush();
                    } catch (IOException e1) {
                    }
                }
                try {
                    writer.close();
                } catch (IOException e1) {
                }
                System.out.println(stringWriter);
                // 放入剪切板
                Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable tText = new StringSelection(stringWriter.toString());
                sysClip.setContents(tText, null);
            }
        });
        resultTableMenu.add(copyFileNameNoExt);
        copyFileName = new JMenuItem("复制文件名");
        copyFileName.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = resultTable.getSelectedRows();
                StringWriter stringWriter = new StringWriter();
                BufferedWriter writer = new BufferedWriter(stringWriter);
                for (int row : selectedRows) {
                    Map<IndexedField, Object> map = currentResults.get(row);
                    String name = (String) map.get(IndexedField.NAME);
                    String ext = (String) map.get(IndexedField.EXTENSION);
                    String fileName = name + (StringUtils.isNotEmpty(ext) ? "." + ext : "");
                    try {
                        if (stringWriter.getBuffer().length() > 0) {
                            writer.newLine();
                        }
                        writer.write(fileName);
                        writer.flush();
                    } catch (IOException e1) {
                    }
                }
                try {
                    writer.close();
                } catch (IOException e1) {
                }
                System.out.println(stringWriter);
                // 放入剪切板
                Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable tText = new StringSelection(stringWriter.toString());
                sysClip.setContents(tText, null);
            }
        });
        resultTableMenu.add(copyFileName);
        copyFilePath = new JMenuItem("复制文件路径");
        copyFilePath.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = resultTable.getSelectedRows();
                StringWriter stringWriter = new StringWriter();
                BufferedWriter writer = new BufferedWriter(stringWriter);
                for (int row : selectedRows) {
                    Map<IndexedField, Object> map = currentResults.get(row);
                    String path = (String) map.get(IndexedField.PATH);
                    try {
                        if (stringWriter.getBuffer().length() > 0) {
                            writer.newLine();
                        }
                        writer.write(path);
                        writer.flush();
                    } catch (IOException e1) {
                    }
                }
                try {
                    writer.close();
                } catch (IOException e1) {
                }
                System.out.println(stringWriter);
                // 放入剪切板
                Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable tText = new StringSelection(stringWriter.toString());
                sysClip.setContents(tText, null);
            }
        });
        resultTableMenu.add(copyFilePath);

        LayoutUtils.anchorFixedBorder(this, splitPane).anchorRight().anchorBottom();

        JPanel panel = new JPanel();
        splitPane.setRightComponent(panel);
        panel.setLayout(null);

        fileKeyworkTxt = new JTextField();
        fileKeyworkTxt.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    searchInFile();
                }
            }
        });
        fileKeyworkTxt.setBounds(171, 10, 298, 21);
        panel.add(fileKeyworkTxt);
        fileKeyworkTxt.setColumns(10);

        regexChk = new JCheckBox("是否正则");
        regexChk.setToolTipText("是否正则");
        regexChk.setBounds(6, 9, 79, 23);
        panel.add(regexChk);

        highLightChk = new JCheckBox("是否高亮");
        highLightChk.setToolTipText("是否高亮");
        highLightChk.setSelected(true);
        highLightChk.setBounds(87, 9, 73, 23);
        panel.add(highLightChk);

        JButton filePreBtn = new JButton("上一个");
        filePreBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                currentSearchContext.setSearchForward(false);
                SearchEngine.find(textArea, currentSearchContext);
            }
        });
        filePreBtn.setBounds(553, 9, 73, 23);
        panel.add(filePreBtn);

        JButton fileNextBtn = new JButton("下一个");
        fileNextBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                currentSearchContext.setSearchForward(true);
                SearchEngine.find(textArea, currentSearchContext);
            }
        });
        fileNextBtn.setBounds(636, 9, 73, 23);
        panel.add(fileNextBtn);

        JButton fileSearchBtn = new JButton("搜索");
        fileSearchBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                searchInFile();
            }
        });
        fileSearchBtn.setBounds(479, 9, 57, 23);
        panel.add(fileSearchBtn);

        sp.setBounds(0, 38, 739, 148);
        panel.add(sp);
        LayoutUtils.anchorFixedBorder(panel, sp).anchorRight().anchorBottom();

        markLeb = new JLabel("匹配数：");
        markLeb.setBounds(719, 13, 100, 15);
        panel.add(markLeb);

        charsetLeb = new JLabel("文件编码：");
        charsetLeb.setBounds(829, 12, 150, 15);
        panel.add(charsetLeb);

        JButton refreshBtn = new JButton("刷新索引");
        refreshBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                refreshIndex();
            }
        });
        refreshBtn.setBounds(649, 6, 93, 23);
        add(refreshBtn);

        JButton button = new JButton("选择索引...");
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                IndexSelectDialog dialog = new IndexSelectDialog();
                dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                dialog.setTitle("选择索引");
                dialog.setConfigManager(configManager);
                dialog.setSelectedIndexs(selectedIndexs);
                dialog.initData();
                dialog.setLocationRelativeTo(SearchPane.this);
                dialog.setModal(true);
                dialog.setVisible(true);
                dialog.dispose();
                selectedIndexs = dialog.getSelectedIndexs();
                refreshIndex();
            }
        });
        button.setBounds(752, 6, 115, 23);
        add(button);

        countLeb = new JLabel("结果数：");
        countLeb.setBounds(877, 10, 250, 15);
        add(countLeb);

        resultKeyword = new JTextField();
        resultKeyword.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    searchInResult();
                }
            }
        });
        resultKeyword.setBounds(54, 35, 250, 21);
        add(resultKeyword);
        resultKeyword.setColumns(10);

        searchInResultBtn = new JButton("结果内搜索");
        searchInResultBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                searchInResult();
            }
        });
        searchInResultBtn.setToolTipText("不用索引，全文件逐字遍历");
        searchInResultBtn.setBounds(573, 34, 126, 23);
        searchInResultBtn.setEnabled(false);
        add(searchInResultBtn);

        searchInResultResetBtn = new JButton("重置");
        searchInResultResetBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (searchResult != null) {
                    currentResults = searchResult.getResults();
                    refreshResultTable(searchResult.getResults(), searchResult.getTotalCount(), 0);
                }
                searchInResultResetBtn.setEnabled(false);
                inResultSearchTimes = 0;
                searchInResultBtn.setText("结果内搜索");
            }
        });
        searchInResultResetBtn.setBounds(722, 34, 71, 23);
        searchInResultResetBtn.setEnabled(false);
        add(searchInResultResetBtn);
        
        JLabel label = new JLabel("包含");
        label.setBounds(10, 37, 41, 17);
        add(label);
        
        label_1 = new JLabel("排除");
        label_1.setBounds(307, 37, 55, 17);
        add(label_1);
        
        resultExcludeTxt = new JTextField();
        resultExcludeTxt.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    searchInResult();
                }
            }
        });
        resultExcludeTxt.setBounds(347, 35, 205, 21);
        add(resultExcludeTxt);
        resultExcludeTxt.setColumns(10);
        
        JLabel lblNewLabel = new JLabel("文件后缀");
        lblNewLabel.setBounds(415, 10, 58, 15);
        add(lblNewLabel);
        
        extensionTxt = new JTextField();
        extensionTxt.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    search();
                }
            }

        });
        extensionTxt.setBounds(467, 7, 69, 21);
        add(extensionTxt);
        extensionTxt.setColumns(10);

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                e.consume();
            }
        });
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

    public void afterPropertySet() {
        GlobalConfig globalConfig = configManager.getGlobalConfig();
        if (globalConfig.getSelectedIndexs() != null) {
            selectedIndexs.addAll(globalConfig.getSelectedIndexs());
        }
        refreshIndex();
    }

    private void search() {
        if (luceneSearchManager == null) {
            JOptionPane.showMessageDialog(SearchPane.this, "没有加载索引文件");
            return;
        }
        if (StringUtils.isBlank(keywordTxt.getText())) {
            JOptionPane.showMessageDialog(SearchPane.this, "关键字为空");
            return;
        }
        long start = System.currentTimeMillis();
        searchResult = luceneSearchManager.search(keywordTxt.getText(),extensionTxt.getText());
        currentResults = new ArrayList<Map<IndexedField, Object>>(searchResult.getResults());
        long spentTime = System.currentTimeMillis() - start;
        List<Map<IndexedField, Object>> results = searchResult.getResults();
        int totalCount = searchResult.getTotalCount();
        refreshResultTable(results, totalCount, spentTime);
        searchInResultBtn.setEnabled(true);
        //
        currentSearchContext.setSearchFor(keywordTxt.getText());
        currentSearchContext.setMatchCase(false);
        currentSearchContext.setRegularExpression(false);
        currentSearchContext.setSearchForward(true);
        currentSearchContext.setWholeWord(false);
        //
        resultKeyword.setText(keywordTxt.getText());
        resultExcludeTxt.setText("");
        fileKeyworkTxt.setText(keywordTxt.getText());
        inResultSearchTimes = 0;
        searchInResultBtn.setText("结果内搜索");
        // 默认选中第一个
        if(results.size() > 0) {
            resultTable.setRowSelectionInterval(0, 0);
        }
    }

    private void refreshResultTable(List<Map<IndexedField, Object>> results, int totalCount, long spentTime) {
        countLeb.setText("结果数：" + totalCount + "，耗时：" + spentTime + "毫秒");
        currentPath = null;
        resultTable.clearSelection();
        DefaultTableModel model = (DefaultTableModel) resultTable.getModel();
        model.getDataVector().removeAllElements();
        for (Map<IndexedField, Object> result : results) {
            model.addRow(new Object[] { result.get(IndexedField.NAME), result.get(IndexedField.EXTENSION),
                    readAbleSize(Long.parseLong((String) result.get(IndexedField.LENGTH))), result.get(IndexedField.ENCODING),
                    formatDate(Long.parseLong((String) result.get(IndexedField.MODIFIED))), result.get(IndexedField.PATH) });
        }
        TableUtils.FitTableColumns(resultTable);
        resultTable.repaint();
        // 清空编辑器
        try {
            textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
            textArea.read(new StringReader(""), null);
        } catch (IOException e) {
        }
    }

    private void updateTabTitle() {
        if (selectedIndexs.size() > 0) {
            IndexConfig indexConfig = configManager.getIndexConfig(selectedIndexs.get(0));
            if (indexConfig.getName().length() > 20) {
                tabTitlePanel.setTabTitle(indexConfig.getName().substring(0, 20));
            } else {
                tabTitlePanel.setTabTitle(indexConfig.getName());
            }
        } else {
            tabTitlePanel.setTabTitle("");
        }
    }

    private String formatDate(long time) {
        Date date = new Date(time);
        return DateFormatUtils.format(date, "yyyy-MM-dd HH:mm:ss");
    }

    private String readAbleSize(long length) {
        DecimalFormat f = new DecimalFormat("#.###");
        long k = 1024;
        long m = k * k;
        if (length > m) {
            return f.format(length / (double) m) + "M";
        } else if (length > k) {
            return f.format(length / (double) k) + "K";
        } else {
            return length + "";
        }
    }

    private void refreshIndex() {
        List<IndexConfig> allIndexConfig = configManager.getAllIndexConfig();
        Set<String> dirs = new HashSet<String>();
        List<String> selectedIndexNames = new ArrayList<String>();
        for (IndexConfig indexConfig : allIndexConfig) {
            if (new File(indexConfig.getIndexSaveDir()).exists()) {
                // 是否选择了索引
                if (CollectionUtils.isNotEmpty(selectedIndexs)) {
                    if (selectedIndexs.contains(indexConfig.getId())) {
                        dirs.add(indexConfig.getIndexSaveDir());
                        selectedIndexNames.add(indexConfig.getName());
                    }
                } else {
                    dirs.add(indexConfig.getIndexSaveDir());
                    selectedIndexNames.add(indexConfig.getName());
                }
            } else {
                JOptionPane.showMessageDialog(SearchPane.this, "索引目录，" + new File(indexConfig.getIndexSaveDir()).getAbsolutePath() + ", 不存在");
            }
        }
        if (dirs.size() == 0) {
            return;
        }
        outFrame.setTitle("本地文件索引 - " + StringUtils.join(selectedIndexNames, ", "));
        updateTabTitle();
        if (luceneSearchManager == null) {
            luceneSearchManager = new LuceneSearchManager();
        } else {
            luceneSearchManager.closeIndex();
        }
        luceneSearchManager.openIndex(dirs.toArray(new String[0]));
    }

    private void searchInFile() {
        if (StringUtils.isEmpty(fileKeyworkTxt.getText())) {
            JOptionPane.showMessageDialog(SearchPane.this, "关键字为空");
            return;
        }
        textArea.getCaret().setDot(0);
        currentSearchContext.setSearchFor(fileKeyworkTxt.getText());
        currentSearchContext.setMatchCase(false);
        currentSearchContext.setRegularExpression(regexChk.isSelected());
        currentSearchContext.setSearchForward(true);
        currentSearchContext.setWholeWord(false);
        try {
            org.fife.ui.rtextarea.SearchResult find = SearchEngine.find(textArea, currentSearchContext);
            markLeb.setText("匹配数：" + find.getMarkedCount());
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(SearchPane.this, "搜索失败，" + e.getMessage());
        }
    }

    public void updateOutFrameTitle() {
        List<String> selectedIndexNames = new ArrayList<String>();
        List<IndexConfig> allIndexConfig = configManager.getAllIndexConfig();
        for (IndexConfig indexConfig : allIndexConfig) {
            if (new File(indexConfig.getIndexSaveDir()).exists()) {
                // 是否选择了索引
                if (CollectionUtils.isNotEmpty(selectedIndexs)) {
                    if (selectedIndexs.contains(indexConfig.getId())) {
                        selectedIndexNames.add(indexConfig.getName());
                    }
                } else {
                    selectedIndexNames.add(indexConfig.getName());
                }
            }
        }
        if (selectedIndexNames.size() == 0) {
            return;
        }
        outFrame.setTitle("本地文件索引 - " + StringUtils.join(selectedIndexNames, ", "));
    }

    public JsonFileConfigManager getConfigManager() {
        return configManager;
    }

    public void setConfigManager(JsonFileConfigManager configManager) {
        this.configManager = configManager;
    }

    public JFrame getOutFrame() {
        return outFrame;
    }

    public void setOutFrame(JFrame outFrame) {
        this.outFrame = outFrame;
    }

    public TabTitlePanel getTabTitlePanel() {
        return tabTitlePanel;
    }

    public void setTabTitlePanel(TabTitlePanel tabTitlePanel) {
        this.tabTitlePanel = tabTitlePanel;
    }

    public List<String> getSelectedIndex() {
        return selectedIndexs;
    }

    public void setSelectedIndex(List<String> selectedIndex) {
        this.selectedIndexs = selectedIndex;
    }

    @SuppressWarnings("unchecked")
    private void searchInResult() {
        if (StringUtils.isEmpty(resultKeyword.getText()) && StringUtils.isEmpty(resultExcludeTxt.getText())) {
            JOptionPane.showMessageDialog(SearchPane.this, "关键字为空");
            return;
        }
        if (isSearchingInResult.value) {
            JOptionPane.showMessageDialog(SearchPane.this, "正在搜索中，请稍候");
            return;
        }
        isSearchingInResult.value = true;
        ProgressUtils.doInProgress(outFrame, "结果内搜索", 500, new ProgressCallback() {

            @Override
            public void doWithProgress(JDialog dialog, Progress progress) {
                if (currentResults != null) {
                    progress.setMinimum(1);
                    progress.setMaximum(currentResults.size());
                    List<Map<IndexedField, Object>> matchedResults = new ArrayList<Map<IndexedField, Object>>();
                    final Set<String> textFileExts = unionCollection(configManager.getGlobalConfig().getTxtFileExts());
                    long start = System.currentTimeMillis();
                    int i = 0;
                    for (Map<IndexedField, Object> config : currentResults) {
                        i++;
                        progress.setValue(i);
                        progress.setDescription("结果内搜索 " + i + "/" + currentResults.size());
                        String path = (String) config.get(IndexedField.PATH);
                        String name = (String) config.get(IndexedField.NAME);
                        File file = new File(path);
                        if (file.exists()) {
                            if (textFileExts.contains(FilenameUtils.getExtension(file.getName()))) {
                                if (file.length() > MAX_TEXT_CONTENT_SIZE) {
                                    if (JOptionPane.showConfirmDialog(SearchPane.this, file.getAbsolutePath() + "," + readAbleSize(file.length())
                                            + ",文件过大,是否跳过") != JOptionPane.OK_OPTION) {
                                        continue;
                                    }
                                }
                                try {
                                    byte[] bytes = FileUtils.readFileToByteArray(file);
                                    String content = new String(bytes, StringUtils.defaultIfBlank(CharSetUtils.detectCharset(bytes), "ISO-8859-1"));
                                    if (StringUtils.isNotEmpty(resultExcludeTxt.getText())) {
                                        if (StringUtils.containsIgnoreCase(content, resultExcludeTxt.getText()) || StringUtils.containsIgnoreCase(name, resultExcludeTxt.getText())
                                                || StringUtils.containsIgnoreCase(path, resultExcludeTxt.getText())) {
                                            continue;
                                        }
                                    }
                                    if (StringUtils.isNotEmpty(resultKeyword.getText())) {
                                        if (StringUtils.containsIgnoreCase(content, resultKeyword.getText()) || StringUtils.containsIgnoreCase(name, resultKeyword.getText())
                                                || StringUtils.containsIgnoreCase(path, resultKeyword.getText())) {
                                            matchedResults.add(config);
                                        }
                                    } else {
                                        matchedResults.add(config);
                                    }
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                    JOptionPane.showMessageDialog(SearchPane.this, "搜索失败，" + e1.getMessage());
                                }
                            }
                        }
                    }
                    currentResults = matchedResults;
                    long spentTime = System.currentTimeMillis() - start;
                    searchInResultResetBtn.setEnabled(true);
                    refreshResultTable(matchedResults, matchedResults.size(), spentTime);
                    //
                    currentSearchContext.setSearchFor(resultKeyword.getText());
                    currentSearchContext.setMatchCase(false);
                    currentSearchContext.setRegularExpression(false);
                    currentSearchContext.setSearchForward(true);
                    currentSearchContext.setWholeWord(false);
                    //
                    isSearchingInResult.value = false;
                    inResultSearchTimes++;
                    searchInResultBtn.setText("结果内搜索(" + inResultSearchTimes + "次)");
                    fileKeyworkTxt.setText(resultKeyword.getText());
                }
            }
        });

    }

    private void openFile(int row) {
        if (row != -1) {
            Map<IndexedField, Object> map = currentResults.get(row);
            String path = (String) map.get(IndexedField.PATH);
            if (new File(path).exists()) {
                DesktopHelper desktopHelper = new DesktopHelper(configManager.getGlobalConfig());
                try {
                    desktopHelper.open(new File(path));
                } catch (Exception e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(SearchPane.this, "打开文件失败，" + e1.getMessage());
                }
            }
        }
    }

    private void openDir(int row) {
        if (row != -1) {
            Map<IndexedField, Object> map = currentResults.get(row);
            String path = (String) map.get(IndexedField.PATH);
            if (new File(path).exists()) {
                DesktopHelper desktopHelper = new DesktopHelper(configManager.getGlobalConfig());
                try {
                    desktopHelper.openDir(new File(path));
                } catch (Exception e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(SearchPane.this, "打开目录失败，" + e1.getMessage());
                }
            }
        }
    }

    private void openCmd(int row) {
        if (row != -1) {
            Map<IndexedField, Object> map = currentResults.get(row);
            String path = (String) map.get(IndexedField.PATH);
            if (new File(path).exists()) {
                DesktopHelper desktopHelper = new DesktopHelper(configManager.getGlobalConfig());
                try {
                    desktopHelper.openCmd(new File(path));
                } catch (Exception e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(SearchPane.this, "打开命令行失败，" + e1.getMessage());
                }
            }
        }
    }
}
