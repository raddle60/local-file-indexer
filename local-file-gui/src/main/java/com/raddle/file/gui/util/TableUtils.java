package com.raddle.file.gui.util;

import java.util.Enumeration;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

public class TableUtils {

    public static void FitTableColumns(JTable jTable) {
        JTableHeader header = jTable.getTableHeader();
        int rowCount = jTable.getRowCount();

        Enumeration<TableColumn> columns = jTable.getColumnModel().getColumns();
        while (columns.hasMoreElements()) {
            TableColumn column = columns.nextElement();
            int col = header.getColumnModel().getColumnIndex(column.getIdentifier());
            int width = (int) jTable.getTableHeader().getDefaultRenderer().getTableCellRendererComponent(jTable, column.getIdentifier(), false, false, -1, col).getPreferredSize().getWidth();
            // 只取前100行计算列宽，防止表格过大，占用大量时间
            for (int row = 0; row < Math.min(100, rowCount); row++) {
                int preferedWidth = (int) jTable.getCellRenderer(row, col).getTableCellRendererComponent(jTable, jTable.getValueAt(row, col), false, false, row, col).getPreferredSize().getWidth();
                width = Math.max(width, preferedWidth);
            }
            header.setResizingColumn(column); // 此行很重要
            column.setWidth(width + jTable.getIntercellSpacing().width);
        }
    }
}
