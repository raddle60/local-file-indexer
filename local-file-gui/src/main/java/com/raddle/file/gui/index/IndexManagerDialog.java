package com.raddle.file.gui.index;

import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang.StringUtils;

import com.raddle.config.ConfigManager;
import com.raddle.file.gui.table.SimpleTableModel;
import com.raddle.file.gui.util.DesktopHelper;
import com.raddle.file.gui.util.TableUtils;
import com.raddle.index.config.IndexConfig;
import com.raddle.index.lucene.LuceneIndexManager;
import com.raddle.index.observer.ProgressObserver;
import com.raddle.swing.layout.LayoutUtils;
import com.raddle.swing.layout.anchor.border.FixedBorderAnchor;
import com.raddle.swing.progress.Progress;
import com.raddle.swing.progress.ProgressContext;
import com.raddle.swing.progress.ProgressUtils;
import com.raddle.swing.progress.multi.MultiProgressCallback;

public class IndexManagerDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private JTable indexConfigListTable;
    private JScrollPane scrollPane;
    private ConfigManager configManager;
    private LuceneIndexManager indexManager;
    private final JPopupMenu resultTableMenu = new JPopupMenu();
    private Point resultTablePopPoint = null;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    IndexManagerDialog dialog = new IndexManagerDialog();
                    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    dialog.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     */
    public IndexManagerDialog(){
        setTitle("索引列表");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 698, 520);
        getContentPane().setLayout(null);

        JButton button = new JButton("新增");
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                IndexConfigEdit dialog = new IndexConfigEdit();
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                dialog.setTitle("新增索引");
                dialog.setConfigManager(configManager);
                dialog.setLocationRelativeTo(IndexManagerDialog.this);
                dialog.initData();
                dialog.setModal(true);
                dialog.setVisible(true);
                dialog.dispose();
                refreshIndexTable();
            }
        });
        button.setBounds(10, 10, 93, 23);

        JButton button_1 = new JButton("编辑");
        button_1.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                editIndex();
            }
        });
        button_1.setBounds(121, 10, 93, 23);

        JButton button_2 = new JButton("删除");
        button_2.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                deleteIndex();
            }
        });
        button_2.setBounds(232, 10, 93, 23);

        scrollPane = new JScrollPane();
        scrollPane.setBounds(10, 48, 670, 428);

        JButton button_3 = new JButton("重建索引");
        button_3.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                rebuildIndex();
            }
        });
        button_3.setBounds(438, 10, 95, 23);
        JMenuItem editMenuItem = new JMenuItem("编辑");
        editMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                editIndex();
            }
        });
        resultTableMenu.add(editMenuItem);
        JMenuItem deleteMenuItem = new JMenuItem("删除");
        deleteMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                deleteIndex();
            }
        });
        resultTableMenu.add(deleteMenuItem);
        JMenuItem refreshMenuItem = new JMenuItem("更新索引");
        refreshMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                refreshIndex();
            }
        });
        resultTableMenu.add(refreshMenuItem);
        JMenuItem reBuildMenuItem = new JMenuItem("重建索引");
        reBuildMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                rebuildIndex();
            }
        });
        resultTableMenu.add(reBuildMenuItem);
        JMenuItem openDir = new JMenuItem("打开文件目录");
        openDir.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                int row = indexConfigListTable.rowAtPoint(resultTablePopPoint);
                openDir(row, true);
            }
        });
        resultTableMenu.add(openDir);
        JMenuItem openCmd = new JMenuItem("打开文件命令行");
        openCmd.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                int row = indexConfigListTable.rowAtPoint(resultTablePopPoint);
                openCmd(row, true);
            }
        });
        resultTableMenu.add(openCmd);
        JMenuItem openIndexDir = new JMenuItem("打开索引目录");
        openIndexDir.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                int row = indexConfigListTable.rowAtPoint(resultTablePopPoint);
                openDir(row, false);
            }
        });
        resultTableMenu.add(openIndexDir);
        JMenuItem openIndexCmd = new JMenuItem("打开索引命令行");
        openIndexCmd.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                int row = indexConfigListTable.rowAtPoint(resultTablePopPoint);
                openCmd(row, false);
            }
        });
        resultTableMenu.add(openIndexCmd);
        indexConfigListTable = new JTable();
        indexConfigListTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editIndex();
                }
                if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
                    // 右键弹出菜单
                    resultTableMenu.show(indexConfigListTable, e.getX(), e.getY());
                    resultTablePopPoint = new Point(e.getX(), e.getY());
                }
            }
        });
        indexConfigListTable.setModel(new SimpleTableModel());
        scrollPane.setViewportView(indexConfigListTable);
        getContentPane().setLayout(null);
        getContentPane().add(button);
        getContentPane().add(button_1);
        getContentPane().add(button_2);
        getContentPane().add(button_3);
        getContentPane().add(scrollPane);

        JButton button_4 = new JButton("更新索引");
        button_4.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                refreshIndex();
            }
        });
        button_4.setToolTipText("只新增和更新索引，不删除索引");
        button_4.setBounds(335, 10, 93, 23);
        getContentPane().add(button_4);

    }

    public void initData() {
        //
        indexManager = new LuceneIndexManager();
        indexManager.setConfigManager(configManager);
        // 查询列表
        DefaultTableModel model = (DefaultTableModel) indexConfigListTable.getModel();
        model.addColumn("索引名称");
        model.addColumn("文件目录");
        model.addColumn("索引文件个数");
        model.addColumn("索引目录");
        indexConfigListTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        FixedBorderAnchor anchorFixedBorder = LayoutUtils.anchorFixedBorder(IndexManagerDialog.this, scrollPane);
        anchorFixedBorder.anchorRight().anchorBottom();
        refreshIndexTable();
    }

    public void refreshIndexTable() {
        indexConfigListTable.clearSelection();
        // 填入jtable
        DefaultTableModel model = (DefaultTableModel) indexConfigListTable.getModel();
        // 清除以前的
        model.getDataVector().removeAllElements();
        // 加入新的
        List<IndexConfig> allIndexConfig = configManager.getAllIndexConfig();
        for (IndexConfig config : allIndexConfig) {
            model.addRow(new Object[] { config, config.getFileDir(), config.getIndexedCount(), config.getIndexSaveDir() });
        }
        TableUtils.FitTableColumns(indexConfigListTable);
        indexConfigListTable.repaint();
    }

    private void openDir(int row, boolean isFileContent) {
        if (row != -1) {
            IndexConfig indexConfig = (IndexConfig) indexConfigListTable.getValueAt(row, 0);
            String path = indexConfig.getFileDir();
            if (!isFileContent) {
                path = indexConfig.getIndexSaveDir();
            }
            if (new File(path).exists()) {
                DesktopHelper desktopHelper = new DesktopHelper(configManager.getGlobalConfig());
                try {
                    desktopHelper.openDir(new File(path));
                } catch (Exception e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(IndexManagerDialog.this, "打开目录失败，" + e1.getMessage());
                }
            }
        }
    }

    private void openCmd(int row, boolean isFileContent) {
        if (row != -1) {
            IndexConfig indexConfig = (IndexConfig) indexConfigListTable.getValueAt(row, 0);
            String path = indexConfig.getFileDir();
            if (!isFileContent) {
                path = indexConfig.getIndexSaveDir();
            }
            if (new File(path).exists()) {
                DesktopHelper desktopHelper = new DesktopHelper(configManager.getGlobalConfig());
                try {
                    desktopHelper.openCmd(new File(path));
                } catch (Exception e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(IndexManagerDialog.this, "打开命令行失败，" + e1.getMessage());
                }
            }
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    private void editIndex() {
        int selectedRow = indexConfigListTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(IndexManagerDialog.this, "未选择索引");
            return;
        }
        IndexConfig indexConfig = (IndexConfig) indexConfigListTable.getValueAt(selectedRow, 0);
        IndexConfigEdit dialog = new IndexConfigEdit();
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setTitle("编辑索引");
        dialog.setConfigManager(configManager);
        dialog.setIndexConfig(indexConfig);
        dialog.initData();
        dialog.setLocationRelativeTo(IndexManagerDialog.this);
        dialog.setModal(true);
        dialog.setVisible(true);
        dialog.dispose();
        refreshIndexTable();
    }

    private void deleteIndex() {
        int selectedRow = indexConfigListTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(IndexManagerDialog.this, "未选择索引");
            return;
        }
        IndexConfig indexConfig = (IndexConfig) indexConfigListTable.getValueAt(selectedRow, 0);
        if (JOptionPane.showConfirmDialog(IndexManagerDialog.this, "确定要删除索引[" + indexConfig.getName() + "]吗？索引文件不会被删除") == JOptionPane.OK_OPTION) {
            //                    if (new File(indexConfig.getIndexSaveDir()).exists()) {
            //                        // 有时索引目录，会使设置错误，将整个目录删除，将会造成数据损失，所以去掉删除索引目录
            //                        // 比如设置了个D盘，整个盘的文件可能被删除掉
            //                        if (JOptionPane.showConfirmDialog(IndexManagerFrame.this, "是否删除索引目录", "删除索引目录", JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
            //                            try {
            //                                FileUtils.deleteDirectory(new File(indexConfig.getIndexSaveDir()));
            //                            } catch (IOException e1) {
            //                                throw new RuntimeException(e1.getMessage(), e1);
            //                            }
            //                        }
            //                    }
            configManager.deleteIndexConfig(indexConfig.getId());
        }
        refreshIndexTable();
    }

    private void rebuildIndex() {
        int selectedRow = indexConfigListTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(IndexManagerDialog.this, "未选择索引");
            return;
        }
        if (JOptionPane.showConfirmDialog(IndexManagerDialog.this, "确定要重建选中的索引吗？原有索引文件将被清除") == JOptionPane.OK_OPTION) {
            ProgressUtils.doInMultiProgress(null, "重建索引", 2, new MultiProgressCallback() {

                @Override
                public void doWithMultiProgress(ProgressContext context) {
                    int[] selectedRows = indexConfigListTable.getSelectedRows();
                    final Progress progress = context.getProgress(0);
                    final Progress totalProgress = context.getProgress(1);
                    totalProgress.setMaximum(selectedRows.length);
                    for (int i = 0; i < selectedRows.length; i++) {
                        final IndexConfig indexConfig = (IndexConfig) indexConfigListTable.getValueAt(selectedRows[i], 0);
                        context.getDialog().setTitle("重建索引 - " + indexConfig.getName());
                        totalProgress.setDescription("正在重建索引 : " + (i + 1) + "/" + selectedRows.length + " - " + indexConfig.getName());
                        if (StringUtils.isBlank(indexConfig.getFileDir())) {
                            JOptionPane.showMessageDialog(IndexManagerDialog.this, "文件目录为空");
                            continue;
                        }
                        if (!new File(indexConfig.getFileDir()).exists()) {
                            JOptionPane.showMessageDialog(IndexManagerDialog.this, "文件目录" + new File(indexConfig.getFileDir()).getAbsolutePath() + "不存在");
                            continue;
                        }
                        indexManager.rebuildIndex(indexConfig.getFileDir(), new ProgressObserver() {

                            @Override
                            public void fileCollecting(int count) {
                                progress.setDescription("已统计文件数：" + count);
                            }

                            @Override
                            public void fileIndexing(int count) {
                                progress.setValue(count);
                                progress.setDescription("已建立索引 , " + count + "/" + progress.getMaximum());
                            }

                            @Override
                            public void fileCollected(int count, int total, int ignored) {
                                progress.setDescription("开始建立索引");
                                progress.setMaximum(count);
                                progress.setValue(0);
                                indexConfig.setIndexedCount(count);
                                configManager.saveOrUpdateIndexConfig(indexConfig);
                            }

                            @Override
                            public void fileIndexed(int count, boolean terminated) {
                                refreshIndexTable();
                            }

                            @Override
                            public boolean isPaused() {
                                return false;
                            }

                            @Override
                            public boolean isTerminated() {
                                return false;
                            }
                        }, indexConfig);
                        totalProgress.setValue(i + 1);
                    }
                }
            });
        }
    }

    private void refreshIndex() {
        int selectedRow = indexConfigListTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(IndexManagerDialog.this, "未选择索引");
            return;
        }
        if (JOptionPane.showConfirmDialog(IndexManagerDialog.this, "确定要更新选中的索引吗？已删除文件的索引不会被更新") == JOptionPane.OK_OPTION) {
            ProgressUtils.doInMultiProgress(null, "更新索引", 2, new MultiProgressCallback() {

                @Override
                public void doWithMultiProgress(ProgressContext context) {
                    int[] selectedRows = indexConfigListTable.getSelectedRows();
                    final Progress progress = context.getProgress(0);
                    final Progress totalProgress = context.getProgress(1);
                    totalProgress.setMaximum(selectedRows.length);
                    for (int i = 0; i < selectedRows.length; i++) {
                        final IndexConfig indexConfig = (IndexConfig) indexConfigListTable.getValueAt(selectedRows[i], 0);
                        totalProgress.setDescription("正在更新索引 : " + (i + 1) + "/" + selectedRows.length + " - " + indexConfig.getName());
                        context.getDialog().setTitle("更新索引 - " + indexConfig.getName());
                        if (StringUtils.isBlank(indexConfig.getFileDir())) {
                            JOptionPane.showMessageDialog(IndexManagerDialog.this, "文件目录为空");
                            continue;
                        }
                        if (!new File(indexConfig.getFileDir()).exists()) {
                            JOptionPane.showMessageDialog(IndexManagerDialog.this, "文件目录" + new File(indexConfig.getFileDir()).getAbsolutePath() + "不存在");
                            continue;
                        }
                        indexManager.updateIndex(indexConfig.getFileDir(), new ProgressObserver() {

                            @Override
                            public void fileCollecting(int count) {
                                progress.setDescription("已统计文件数：" + count);
                            }

                            @Override
                            public void fileIndexing(int count) {
                                progress.setValue(count);
                                progress.setDescription("已更新索引 , " + count + "/" + progress.getMaximum());
                            }

                            @Override
                            public void fileCollected(int count, int total, int ignored) {
                                progress.setDescription("开始更新索引");
                                progress.setMaximum(count);
                                progress.setValue(0);
                                indexConfig.setIndexedCount(count);
                                configManager.saveOrUpdateIndexConfig(indexConfig);
                            }

                            @Override
                            public void fileIndexed(int count, boolean terminated) {
                                refreshIndexTable();
                            }

                            @Override
                            public boolean isPaused() {
                                return false;
                            }

                            @Override
                            public boolean isTerminated() {
                                return false;
                            }
                        }, indexConfig);
                        totalProgress.setValue(i + 1);
                    }
                }
            });
        }
    }
}
