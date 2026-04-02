package fr.supinfo.istore.ui.table;

import fr.supinfo.istore.model.Role;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class AccessTableModel extends AbstractTableModel {

    public static class AccessRow {
        private final long userId;
        private final String email;
        private final Role role;

        public AccessRow(long userId, String email, Role role) {
            this.userId = userId;
            this.email = email;
            this.role = role;
        }

        public long getUserId() { return userId; }
        public String getEmail() { return email; }
        public Role getRole() { return role; }
    }

    private static final int COL_ID = 0;
    private static final int COL_EMAIL = 1;
    private static final int COL_ROLE = 2;

    private static final String[] COLS = {"ID", "Email", "Rôle"};
    private final List<AccessRow> rows = new ArrayList<>();

    public void setRows(List<AccessRow> newRows) {
        rows.clear();
        if (newRows != null) rows.addAll(newRows);
        fireTableDataChanged();
    }

    public AccessRow getRow(int rowIndex) {
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
            case COL_ROLE -> Role.class;
            default -> String.class;
        };
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        AccessRow r = rows.get(rowIndex);
        return switch (columnIndex) {
            case COL_ID -> r.getUserId();
            case COL_EMAIL -> r.getEmail();
            case COL_ROLE -> r.getRole();
            default -> null;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
