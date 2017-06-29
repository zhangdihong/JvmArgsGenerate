package wang.yu66.ideaplugin.jvmargs;

import clojure.lang.Obj;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.table.JBTable;
import wang.yu66.ideaplugin.jvmargs.config.JvmArgsContent;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.*;

import static javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS;

/**
 *
 */
public class JvmArgsWindowFactory implements ToolWindowFactory {

    private JPanel tabbedPanel;

    private JTabbedPane memoryTabbedPane;
    private JTextArea output;

    public JvmArgsWindowFactory() {}
    private Map<String, ArrayList<String>> selected = new HashMap<>();

    // Create the tool window content.
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(tabbedPanel, "", false);
        toolWindow.getContentManager().addContent(content);

        for (Map.Entry<String, Object[][]> entry : JvmArgsContent.ALL_ARGS.entrySet()) {
            String type = entry.getKey();
            Object[][] value = entry.getValue();

            JTable table = new JBTable();
            table.setAutoResizeMode(AUTO_RESIZE_ALL_COLUMNS);

            // 设置自动换行
//            table.setDefaultRenderer(Object.class,  new TableCellTextAreaRenderer());

            JScrollPane jScrollPane = new JBScrollPane(table);
            memoryTabbedPane.addTab(type, jScrollPane);

            ArrayList<String> list = selected.get(type);
            if (list == null) {
                list = new ArrayList<>();
                selected.put(type, list);
            }
            this.init(list, table, value);

        }

    }

    public void init(ArrayList<String> list, JTable table, Object[][] args) {

        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);
        setModel(table, args);

        table.getModel().addTableModelListener(e -> {
            if (e.getFirstRow() != e.getLastRow()) {
                System.out.println(" 为啥不一样呢? : " + e.getFirstRow() + " : " + e.getLastRow());
                return;
            }
            String row = e.getFirstRow() + "";
            if (list.contains(row)) {
                list.remove(row);
            } else {
                list.add(row);
            }
            update();
        });
        setBorder(table);

        setColumnWidthAndRowHeight(table);
    }

    private void setModel(JTable table, Object[][] args) {

        table.setModel(new JvmArgTableModel(args));
    }

    private void setBorder(JTable table) {
        Border border = BorderFactory.createEtchedBorder();
        table.setBorder(border);
    }

    private void setColumnWidthAndRowHeight(JTable table) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        table.setRowHeight(20);
        int[] maxWidths = {250, 100, 700, 50};
        for (int i = 0; i < 4; i++) {
            table.getColumnModel().getColumn(i).setMaxWidth(maxWidths[i]);
            table.getColumnModel().getColumn(i).setMinWidth(maxWidths[i]);
        }
    }

    private void update() {
        StringBuffer stringBuffer = new StringBuffer();
        for (Map.Entry<String, ArrayList<String>> entry : selected.entrySet()) {
            ArrayList<String> list = entry.getValue();
            Object[][] config = JvmArgsContent.ALL_ARGS.get(entry.getKey());
            update(stringBuffer, list, config);
        }

        output.setText(stringBuffer.toString());
    }

    private void update(StringBuffer stringBuffer, ArrayList<String> list, Object[][] args) {

        for (String rowStr : list) {
            int rowIdx = Integer.parseInt(rowStr);
            Object[] arg = args[rowIdx];
            String arg1 = (String) arg[0];
            String value = (String) arg[1];
            if (value != null && !(value.equals(""))) {
                stringBuffer.append(arg1);
                stringBuffer.append("=");
                stringBuffer.append(value);
            } else {
                stringBuffer.append(arg1);
            }
            stringBuffer.append(" ");
        }

    }

    class TableCellTextAreaRenderer extends JTextArea implements TableCellRenderer {
        public TableCellTextAreaRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            // 计算当下行的最佳高度
            int maxPreferredHeight = 0;
            for (int i = 0; i < table.getColumnCount(); i++) {
                setText("" + table.getValueAt(row, i));
                setSize(table.getColumnModel().getColumn(column).getWidth(), 0);
                maxPreferredHeight = Math.max(maxPreferredHeight, getPreferredSize().height);
            }

            if (table.getRowHeight(row) != maxPreferredHeight)  // 少了这行则处理器瞎忙
                table.setRowHeight(row, maxPreferredHeight);

            setText(value == null ? "" : value.toString());
            return this;
        }
    }

    class JvmArgTableModel extends AbstractTableModel {
        private Object[][] data;

        JvmArgTableModel(Object[][] data) {
            this.data = data;
        }

        public int getColumnCount() {
            return JvmArgsContent.COMLUMN_HEADER.length;
        }

        public int getRowCount() {
            return data.length;
        }

        public String getColumnName(int col) {
            return JvmArgsContent.COMLUMN_HEADER[col];
        }

        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        /*
         * JTable uses this method to determine the default renderer editor for each cell.
         * If we didn't implement this method, then the last column would contain text ("true"/"false"), rather than a check box.
         */
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        public boolean isCellEditable(int row, int col) {
            return col == 1 || col == 3;
        }

        public void setValueAt(Object value, int row, int col) {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }

    }
}
