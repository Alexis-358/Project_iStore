package fr.supinfo.istore.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public final class UI {

    private UI() {}

    public static final Color BG = new Color(245, 246, 250);
    public static final Color PRIMARY = new Color(33, 150, 243);
    public static final Color SUCCESS = new Color(46, 125, 50);
    public static final Color DANGER = new Color(211, 47, 47);
    public static final Color TEXT_MUTED = new Color(90, 90, 90);

    public static final int GAP = 10;
    public static final Insets PAD_12 = new Insets(12, 12, 12, 12);

    public static void stylePanel(JComponent p) {
        p.setOpaque(true);
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(PAD_12));
    }

    public static void styleSubPanel(JComponent p) {
        p.setOpaque(true);
        p.setBackground(BG);
    }

    public static void stylePrimary(JButton b) { styleButton(b, PRIMARY); }
    public static void styleDanger(JButton b)  { styleButton(b, DANGER); }
    public static void styleSuccess(JButton b) { styleButton(b, SUCCESS); }

    private static void styleButton(JButton b, Color bg) {
        b.setForeground(Color.WHITE);
        b.setBackground(bg);

        b.setContentAreaFilled(true);
        b.setOpaque(true);

        b.setFocusPainted(false);
        b.setBorderPainted(false);

        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static void showSuccess(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Succès", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String msg) {
        int res = JOptionPane.showConfirmDialog(parent, msg, "Confirmation", JOptionPane.YES_NO_OPTION);
        return res == JOptionPane.YES_OPTION;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);

        // IMPORTANT: séparations visibles entre colonnes/lignes (Swing)
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
        table.setIntercellSpacing(new Dimension(10, 6)); // espace entre cellules

        table.setGridColor(new Color(220, 224, 234));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
    }

    public static void rightAlignColumn(JTable table, int colIndex) {
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT); // alignement colonne
        table.getColumnModel().getColumn(colIndex).setCellRenderer(right);
    }

    public static JPanel flowLeft(Component... comps) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, GAP, GAP));
        styleSubPanel(p);
        for (Component c : comps) p.add(c);
        return p;
    }

    public static JPanel flowRight(Component... comps) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, GAP, GAP));
        styleSubPanel(p);
        for (Component c : comps) p.add(c);
        return p;
    }
}
