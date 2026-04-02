package fr.supinfo.istore.ui.table;

import fr.supinfo.istore.model.InventoryLine;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class InventoryTableModel extends AbstractTableModel {

    private static final String[] COLS = {"ID", "Article", "Prix", "Quantité"};
    private List<InventoryLine> rows = new ArrayList<>();

    public void setRows(List<InventoryLine> rows) {
        this.rows = (rows == null) ? new ArrayList<>() : new ArrayList<>(rows);
        fireTableDataChanged();
    }

    public InventoryLine getRow(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= rows.size()) return null;
        return rows.get(rowIndex);
    }

    @Override public int getRowCount() { return rows.size(); }
    @Override public int getColumnCount() { return COLS.length; }
    @Override public String getColumnName(int column) { return COLS[column]; }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        InventoryLine l = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> l.getItemId();
            case 1 -> l.getItemName();
            case 2 -> l.getPrice();
            case 3 -> l.getQuantity();
            default -> null;
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> Long.class;
            case 2 -> Double.class;
            case 3 -> Integer.class;
            default -> String.class;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
