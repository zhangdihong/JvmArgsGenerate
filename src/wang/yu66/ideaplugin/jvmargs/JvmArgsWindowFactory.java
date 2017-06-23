package wang.yu66.ideaplugin.jvmargs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import wang.yu66.ideaplugin.jvmargs.config.JvmArgsContent;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;

/**
 *
 */
public class JvmArgsWindowFactory implements ToolWindowFactory {

    private JPanel tabbedPanel;

    private JTabbedPane memoryTabbedPane;
    private JPanel memoryPanel;
    private JTable memoryTable;
    private JTextArea output;

    public JvmArgsWindowFactory() {}
    private ArrayList<String> memoryList = new ArrayList<>();

    // Create the tool window content.
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        this.init(memoryList, memoryTable, JvmArgsContent.MEMORY);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(tabbedPanel, "", false);
        toolWindow.getContentManager().addContent(content);
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
        }
    }

    private void update() {
        StringBuffer stringBuffer = new StringBuffer();

        update(stringBuffer, memoryList, JvmArgsContent.MEMORY);

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
            if (col == 1 || col == 3) {
                return true;
            } else {
                return false;
            }
        }

        public void setValueAt(Object value, int row, int col) {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }

    }
}
