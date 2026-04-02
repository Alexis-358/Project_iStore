package fr.supinfo.istore.ui.table;

import fr.supinfo.istore.model.Store;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class StoreTableModel extends AbstractTableModel {

    private static final int COL_ID = 0;
    private static final int COL_NAME = 1;

    private static final String[] COLS = {"ID", "Nom"};
    private final List<Store> rows = new ArrayList<>();

    public void setRows(List<Store> stores) {
        rows.clear();
        if (stores != null) rows.addAll(stores);
        fireTableDataChanged();
    }

    public Store getRow(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= rows.size()) return null;
        return rows.get(rowIndex);
    }

    @Override public int getRowCount() { return rows.size(); }
    @Override public int getColumnCount() { return COLS.length; }
    @Override public String getColumnName(int column) { return COLS[column]; }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case COL_ID -> Long.class;
            default -> String.class;
        };
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Store s = rows.get(rowIndex);
        return switch (columnIndex) {
            case COL_ID -> s.getId();
            case COL_NAME -> s.getName();
            default -> null;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
