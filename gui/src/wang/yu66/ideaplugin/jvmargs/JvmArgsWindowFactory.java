package wang.yu66.ideaplugin.jvmargs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.table.JBTable;
import wang.yu66.ideaplugin.jvmargs.config.JvmArgsTableModelData;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

import static javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS;

/**
 *
 */
public class JvmArgsWindowFactory implements ToolWindowFactory {

    private JPanel tabbedPanel;

    private JTabbedPane tabbedPane;
    private JTextArea selectedArgsArea;
    private JTextPane argDetailsPane;
    private JLabel currSelect;

    private JTextField query;
    private List<TableRowSorter<JvmArgTableModel>> sorters = new ArrayList<>();

    private Map<String, ArrayList<String>> selected = new HashMap<>();

    public JvmArgsWindowFactory() {
        selectedArgsArea.setLineWrap(true);        //激活自动换行功能
        selectedArgsArea.setWrapStyleWord(true);            // 激活断行不断字功能

        argDetailsPane.setContentType("text/html");
        argDetailsPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        query.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {newFilter(); }
            public void insertUpdate(DocumentEvent e) {
                newFilter();
            }
            public void removeUpdate(DocumentEvent e) {
                newFilter();
            }
        });
    }

    private void newFilter() {
        try {
            //If current expression doesn't parse, don't update.
            RowFilter<JvmArgTableModel, Object> rf = RowFilter.regexFilter(query.getText(), 0);
            for (TableRowSorter<JvmArgTableModel> sorter : sorters) {
                sorter.setRowFilter(rf);
            }
        } catch (java.util.regex.PatternSyntaxException e) {
            e.printStackTrace();
        }
    }


    // Create the tool window content.
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(tabbedPanel, "", false);
        toolWindow.getContentManager().addContent(content);

        for (Map.Entry<String, Object[][]> entry : JvmArgsTableModelData.ALL_ARGS.entrySet()) {
            String type = entry.getKey();
            Object[][] value = entry.getValue();

            JTable table = new JBTable();
            table.setAutoResizeMode(AUTO_RESIZE_ALL_COLUMNS);

            JScrollPane jScrollPane = new JBScrollPane(table);
            tabbedPane.addTab(type, jScrollPane);

            ArrayList<String> someTypeSelected = selected.get(type);
            if (someTypeSelected == null) {
                someTypeSelected = new ArrayList<>();
                selected.put(type, someTypeSelected);
            }
            this.init(someTypeSelected, table, value, type);
        }
    }

    public void init(ArrayList<String> someTypeSelected, JTable table, Object[][] args, String type) {

        table.setPreferredScrollableViewportSize(new Dimension(
                JvmArgsTableModelData.TABBED_PANEL_WIDTH,
                JvmArgsTableModelData.TABBED_PANEL_HEIGHT));

        table.setFillsViewportHeight(true);

        JvmArgTableModel model = new JvmArgTableModel(args, type);
        table.setModel(model);

        TableRowSorter<JvmArgTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorters.add(sorter);

        table.getModel().addTableModelListener(e -> {
            if (e.getFirstRow() != e.getLastRow()) {
                System.out.println(" 为啥不一样呢? : " + e.getFirstRow() + " : " + e.getLastRow());
                return;
            }
            if (e.getColumn() == 2) {
                updateSelectedArgsShowArea(someTypeSelected, e);
            }
        });

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if(table.isCellSelected(table.getSelectedRow(), 0)){
                    updateArgDetailArea(table.getSelectedRow(), type);
                }
            }
        });

        setBorder(table);
        setColumnWidthAndRowHeight(table);
    }

    private void updateSelectedArgsShowArea(ArrayList<String> someTypeSelected, TableModelEvent e) {
        String row = e.getFirstRow() + "";
        if (someTypeSelected.contains(row)) {
            someTypeSelected.remove(row);
        } else {
            someTypeSelected.add(row);
        }
        StringBuffer stringBuffer = new StringBuffer();
        for (Map.Entry<String, ArrayList<String>> entry : selected.entrySet()) {
            ArrayList<String> list = entry.getValue();
            Object[][] config = JvmArgsTableModelData.ALL_ARGS.get(entry.getKey());
            updateSelectedArgsShowArea(stringBuffer, list, config);
        }

        selectedArgsArea.setText(stringBuffer.toString());
    }

    private void updateSelectedArgsShowArea(StringBuffer outCache, ArrayList<String> selectedArgs, Object[][] configArgs) {

        for (String rowStr : selectedArgs) {
            int rowIdx = Integer.parseInt(rowStr);
            Object[] configArg = configArgs[rowIdx];
            String argName = (String) configArg[0];
            String argValue = (String) configArg[1];
            if (argValue != null && !(argValue.equals(""))) {
                outCache.append(regetArgName(argName));
                outCache.append("=");
                outCache.append(argValue);
            } else {
                outCache.append(argName);
            }
            outCache.append(" ");
        }
    }

    private String regetArgName(String argName) {
        if (argName.contains("[")) {
            if (argName.contains("[=")) {
                return argName.split("\\[=")[0];
            } else if (argName.contains("[:")) {
                return argName.split("\\[:")[0];
            }
        } else if (argName.contains("=")) {
            return argName.split("=")[0];
        }
        return "";
    }

    private void updateArgDetailArea(int row, String type) {
        List<JvmArgsTableModelData.JvmArg> datas = JvmArgsTableModelData.allArgs.get(type);
        JvmArgsTableModelData.JvmArg data = datas.get(row);
        argDetailsPane.setText(data.comment);
        currSelect.setText(data.arg);
    }

    private void setBorder(JTable table) {
        Border border = BorderFactory.createEtchedBorder();
        table.setBorder(border);
    }

    private void setColumnWidthAndRowHeight(JTable table) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        table.setRowHeight(JvmArgsTableModelData.EACH_ROW_HEIGHT);

        for (int i = 0; i < JvmArgsTableModelData.COMLUMN_MAX_WIDTH.length; i++) {
            table.getColumnModel().getColumn(i).setMaxWidth(JvmArgsTableModelData.COMLUMN_MAX_WIDTH[i]);
            table.getColumnModel().getColumn(i).setMinWidth(JvmArgsTableModelData.COMLUMN_MAX_WIDTH[i]);
        }
    }

    class JvmArgTableModel extends AbstractTableModel {
        private Object[][] data;
        private String type;

        JvmArgTableModel(Object[][] data, String type) {
            this.data = data;
            this.type = type;
        }

        public int getColumnCount() {
            return JvmArgsTableModelData.COMLUMN_HEADER.length;
        }

        public int getRowCount() {
            return data.length;
        }

        public String getColumnName(int col) {
            return JvmArgsTableModelData.COMLUMN_HEADER[col];
        }

        public Object getValueAt(int row, int col) {
            Object[] rowData = data[row];
            if (rowData == null) {
                System.err.println("getValueAt Row[" + type + "] : " + row + " is null");
            }
            if (rowData.length == 0) {
                System.err.println("getValueAt Row[" + type + "] : " + row + " length is 0");
            }
            Object colData = rowData[col];
            if (colData == null) {
                System.err.println("getValueAt Col[" + type + "] : " + col + " is null, length: " + rowData.length);
            }
            return colData;
        }

        /*
         * JTable uses this method to determine the default renderer editor for each cell.
         * If we didn't implement this method, then the last column would contain text ("true"/"false"), rather than a check box.
         */
        public Class getColumnClass(int c) {
            Object value = getValueAt(0, c);
            return value.getClass();
        }

        public boolean isCellEditable(int row, int col) {
            return JvmArgsTableModelData.COMLUMN_MODIFY[col];
        }

        public void setValueAt(Object value, int row, int col) {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }

    }
}
