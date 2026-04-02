package fr.supinfo.istore.ui.table;

import fr.supinfo.istore.model.Item;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class ItemTableModel extends AbstractTableModel {

    private static final String[] COLS = {"ID", "Nom", "Prix"};
    private List<Item> rows = new ArrayList<>();

    public void setRows(List<Item> rows) {
        this.rows = (rows == null) ? new ArrayList<>() : new ArrayList<>(rows);
        fireTableDataChanged();
    }

    public Item getRow(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= rows.size()) return null;
        return rows.get(rowIndex);
    }

    @Override public int getRowCount() { return rows.size(); }
    @Override public int getColumnCount() { return COLS.length; }
    @Override public String getColumnName(int column) { return COLS[column]; }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Item it = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> it.getId();
            case 1 -> it.getName();
            case 2 -> it.getPrice();
            default -> null;
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> Long.class;
            case 2 -> Double.class;
            default -> String.class;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
