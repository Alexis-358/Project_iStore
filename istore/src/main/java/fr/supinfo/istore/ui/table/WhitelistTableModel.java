package fr.supinfo.istore.ui.table;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class WhitelistTableModel extends AbstractTableModel {

    private static final String[] COLS = {"Email"};
    private List<String> rows = new ArrayList<>();

    public void setRows(List<String> emails) {
        this.rows = (emails == null) ? new ArrayList<>() : new ArrayList<>(emails);
        fireTableDataChanged();
    }

    public String getRow(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= rows.size()) return null;
        return rows.get(rowIndex);
    }

    @Override public int getRowCount() { return rows.size(); }
    @Override public int getColumnCount() { return 1; }
    @Override public String getColumnName(int column) { return COLS[0]; }
    @Override public Class<?> getColumnClass(int columnIndex) { return String.class; }
    @Override public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        String email = getRow(rowIndex);
        return email == null ? "" : email;
    }
}
