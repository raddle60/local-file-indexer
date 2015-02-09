package com.raddle.file.gui.index;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import com.raddle.config.ConfigManager;
import com.raddle.index.config.GlobalConfig;
import com.raddle.index.config.IndexConfig;

public class IndexSelectDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private JTable indexTable;
    private ConfigManager configManager;
    private JCheckBox checkBox;
    private List<String> selectedIndexs;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    IndexSelectDialog dialog = new IndexSelectDialog();
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
    public IndexSelectDialog(){
        setBounds(100, 100, 563, 351);
        getContentPane().setLayout(null);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(10, 10, 535, 223);
        getContentPane().add(scrollPane);

        indexTable = new JTable();
        indexTable.setModel(new DefaultTableModel() {

            private static final long serialVersionUID = 1L;

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class;
                }
                return super.getColumnClass(columnIndex);
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }

        });
        scrollPane.setViewportView(indexTable);

        checkBox = new JCheckBox("所有索引（选择中将忽略上面的选择）");
        checkBox.setBounds(6, 239, 234, 23);
        getContentPane().add(checkBox);

        JButton okBtn = new JButton("确定");
        okBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                GlobalConfig globalConfig = configManager.getGlobalConfig();
                if (checkBox.isSelected()) {
                    selectedIndexs.clear();
                    globalConfig.setSelectedIndexs(null);
                    configManager.saveGlobalConfig(globalConfig);
                } else {
                    List<String> selectIndexs = new ArrayList<String>();
                    for (int i = 0; i < indexTable.getRowCount(); i++) {
                        boolean selected = (Boolean) indexTable.getValueAt(i, 0);
                        IndexConfig config = (IndexConfig) indexTable.getValueAt(i, 1);
                        if (selected) {
                            selectIndexs.add(config.getId());
                        }
                    }
                    selectedIndexs.clear();
                    selectedIndexs.addAll(selectIndexs);
                    globalConfig.setSelectedIndexs(selectIndexs);
                    configManager.saveGlobalConfig(globalConfig);
                }
                IndexSelectDialog.this.setVisible(false);
            }
        });
        okBtn.setBounds(163, 268, 93, 23);
        getContentPane().add(okBtn);

        JButton cancelBtn = new JButton("取消");
        cancelBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                IndexSelectDialog.this.setVisible(false);
            }
        });
        cancelBtn.setBounds(285, 268, 93, 23);
        getContentPane().add(cancelBtn);
    }

    public void initData() {
        // 查询列表
        DefaultTableModel model = (DefaultTableModel) indexTable.getModel();
        model.addColumn("选择");
        model.addColumn("索引名称");
        model.addColumn("文件目录");
        model.addColumn("索引文件个数");
        indexTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // 清除以前的
        model.getDataVector().removeAllElements();
        // 加入新的
        List<IndexConfig> allIndexConfig = configManager.getAllIndexConfig();
        if (selectedIndexs == null) {
            selectedIndexs = new ArrayList<String>();
            selectedIndexs.addAll(configManager.getGlobalConfig().getSelectedIndexs());
        }
        for (IndexConfig config : allIndexConfig) {
            model.addRow(new Object[] { selectedIndexs != null ? selectedIndexs.contains(config.getId()) : false, config, config.getFileDir(), config.getIndexedCount() });
        }
        indexTable.repaint();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public List<String> getSelectedIndexs() {
        return selectedIndexs;
    }

    public void setSelectedIndexs(List<String> selectedIndexs) {
        this.selectedIndexs = selectedIndexs;
    }

}
