package fr.supinfo.istore.ui;

import fr.supinfo.istore.dao.StockDao;
import fr.supinfo.istore.dao.StoreDao;
import fr.supinfo.istore.dao.StoreEmployeeDao;
import fr.supinfo.istore.dao.UserDao;
import fr.supinfo.istore.model.InventoryLine;
import fr.supinfo.istore.model.Store;
import fr.supinfo.istore.model.User;
import fr.supinfo.istore.security.Session;
import fr.supinfo.istore.service.AuthService;
import fr.supinfo.istore.service.UserService;
import fr.supinfo.istore.ui.table.InventoryTableModel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

public class EmployeeFrame extends JFrame {

    private final AuthService authService;
    private final UserService userService;
    private final Session session;
    private final User user;

    private final StoreDao storeDao = new StoreDao();
    private final StockDao stockDao = new StockDao();
    private final StoreEmployeeDao storeEmployeeDao = new StoreEmployeeDao();
    private final UserDao userDao = new UserDao();

    private final JComboBox<Store> storeCombo = new JComboBox<>();

    private final InventoryTableModel invTableModel = new InventoryTableModel();
    private final JTable invTable = new JTable(invTableModel);

    private final DefaultListModel<String> accessModel = new DefaultListModel<>();
    private final JList<String> accessList = new JList<>(accessModel);

    private final JTextField qtyField = new JTextField("1", 6);
    private final JLabel msgLabel = new JLabel(" ");

    // --- Renderers
    private final DefaultTableCellRenderer moneyRenderer = new DefaultTableCellRenderer() {
        private final DecimalFormat df = new DecimalFormat("0.00");
        @Override public void setValue(Object value) {
            if (value instanceof Number n) setText(df.format(n.doubleValue()) + " €");
            else setText("");
            setHorizontalAlignment(SwingConstants.RIGHT);
        }
    };

    public EmployeeFrame(User user, AuthService authService, UserService userService, Session session) {
        this.user = user;
        this.authService = authService;
        this.userService = userService;
        this.session = session;

        setTitle("iStore - Employé (" + user.getEmail() + ")");
        setSize(980, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setJMenuBar(buildMenuBar());
        getContentPane().setBackground(UI.BG);

        JButton refreshBtn = new JButton("Rafraîchir");
        UI.stylePrimary(refreshBtn);
        refreshBtn.addActionListener(e -> refreshInventory());

        JButton receiveBtn = new JButton("Recevoir (+)");
        UI.stylePrimary(receiveBtn);
        receiveBtn.addActionListener(e -> adjustSelected(+1));

        JButton sellBtn = new JButton("Vendre (-)");
        UI.stylePrimary(sellBtn);
        sellBtn.addActionListener(e -> adjustSelected(-1));

        JButton editMeBtn = new JButton("Modifier mon compte");
        UI.stylePrimary(editMeBtn);
        editMeBtn.addActionListener(e -> doUpdateMyAccount());

        JButton deleteMeBtn = new JButton("Supprimer mon compte");
        UI.styleDanger(deleteMeBtn);
        deleteMeBtn.addActionListener(e -> doDeleteMyAccount());

        storeCombo.addActionListener(e -> refreshInventory());

        JPanel top = UI.flowLeft(
                new JLabel("Magasin:"),
                storeCombo,
                refreshBtn,
                new JLabel("Quantité:"),
                qtyField,
                receiveBtn,
                sellBtn,
                editMeBtn,
                deleteMeBtn
        );

        msgLabel.setForeground(UI.DANGER);

        // --- Table look
        UI.styleTable(invTable);
        invTable.setAutoCreateRowSorter(true); // tri
        invTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Colonnes attendues: ID, Article, Prix, Quantité
        if (invTable.getColumnModel().getColumnCount() >= 4) {
            invTable.getColumnModel().getColumn(0).setMinWidth(60);
            invTable.getColumnModel().getColumn(0).setMaxWidth(90);
            UI.rightAlignColumn(invTable, 0);

            invTable.getColumnModel().getColumn(2).setMinWidth(110);
            invTable.getColumnModel().getColumn(2).setMaxWidth(140);
            invTable.getColumnModel().getColumn(2).setCellRenderer(moneyRenderer);

            UI.rightAlignColumn(invTable, 3);
        }

        JScrollPane invScroll = new JScrollPane(invTable);
        invScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Inventaire", TitledBorder.LEFT, TitledBorder.TOP));

        // --- Access list look (stylé)
        styleAccessList(accessList);

        JScrollPane accessScroll = new JScrollPane(accessList);
        accessScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Accès au magasin", TitledBorder.LEFT, TitledBorder.TOP));

        JPanel center = new JPanel(new GridLayout(1, 2, UI.GAP, UI.GAP));
        UI.stylePanel(center);
        center.add(invScroll);
        center.add(accessScroll);

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(msgLabel, BorderLayout.SOUTH);

        loadStores();
        refreshInventory();
    }

    private void styleAccessList(JList<String> list) {
        list.setFixedCellHeight(30);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus
            ) {
                JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                c.setOpaque(true);
                c.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

                if (!isSelected) {
                    c.setBackground(index % 2 == 0 ? Color.WHITE : UI.BG);
                    c.setForeground(new Color(30, 30, 30));
                }

                // Petit highlight pour l'admin
                String txt = (value == null) ? "" : value.toString();
                if (!isSelected && txt.startsWith("[ADMIN]")) {
                    c.setForeground(new Color(0, 0, 128));
                }

                return c;
            }
        });
    }

    private void loadStores() {
        try {
            storeCombo.removeAllItems();
            for (Store s : storeDao.findStoresForEmployee(user.getId())) storeCombo.addItem(s);

            if (storeCombo.getItemCount() == 0) {
                msgLabel.setForeground(UI.DANGER);
                msgLabel.setText("Aucun magasin accessible (demande à l’admin de t’ajouter).");
            } else {
                msgLabel.setForeground(UI.SUCCESS);
                msgLabel.setText("Magasins chargés (" + storeCombo.getItemCount() + ").");
            }

            refreshAccessList();
        } catch (Exception ex) {
            msgLabel.setForeground(UI.DANGER);
            msgLabel.setText(ex.getMessage());
        }
    }

    private void refreshInventory() {
        try {
            Store store = (Store) storeCombo.getSelectedItem();
            if (store == null) {
                msgLabel.setForeground(UI.DANGER);
                msgLabel.setText("Sélectionne un magasin.");
                accessModel.clear();
                invTableModel.setRows(List.of());
                return;
            }

            List<InventoryLine> lines = stockDao.findInventoryByStore(store.getId());
            invTableModel.setRows(lines);

            refreshAccessList();

            msgLabel.setForeground(UI.SUCCESS);
            msgLabel.setText("Inventaire chargé (" + lines.size() + " articles).");
        } catch (Exception ex) {
            msgLabel.setForeground(UI.DANGER);
            msgLabel.setText(ex.getMessage());
        }
    }

    private void refreshAccessList() {
        try {
            accessModel.clear();

            Store store = (Store) storeCombo.getSelectedItem();
            if (store == null) return;

            accessModel.addElement("[ADMIN] admin@istore.com");

            for (Long userId : storeEmployeeDao.findUserIdsByStore(store.getId())) {
                User u = userDao.findById(userId);
                if (u != null) accessModel.addElement(u.getEmail() + " (" + u.getRole() + ")");
            }
        } catch (Exception ex) {
            accessModel.clear();
            accessModel.addElement("Erreur: " + ex.getMessage());
        }
    }

    private void adjustSelected(int sign) {
        try {
            Store store = (Store) storeCombo.getSelectedItem();
            if (store == null) throw new IllegalArgumentException("Sélectionne un magasin.");

            int viewRow = invTable.getSelectedRow();
            if (viewRow < 0) throw new IllegalArgumentException("Sélectionne un article.");

            int modelRow = invTable.convertRowIndexToModel(viewRow); // nécessaire avec tri
            InventoryLine selected = invTableModel.getRow(modelRow);
            if (selected == null) throw new IllegalArgumentException("Sélection invalide.");

            String txt = qtyField.getText().trim();
            if (txt.isBlank()) throw new IllegalArgumentException("Quantité obligatoire.");

            int qty = Integer.parseInt(txt);
            if (qty <= 0) throw new IllegalArgumentException("Quantité doit être > 0.");

            int delta = sign * qty;
            stockDao.adjustQuantity(store.getId(), selected.getItemId(), delta);

            refreshInventory();

            UI.showSuccess(this, "Stock mis à jour: " + selected.getItemName() + " (" + (delta > 0 ? "+" : "") + delta + ")");
        } catch (NumberFormatException nfe) {
            UI.showError(this, "Quantité invalide (entier).");
        } catch (Exception ex) {
            UI.showError(this, ex.getMessage());
        }
    }

    private void doDeleteMyAccount() {
        if (!UI.confirm(this, "Supprimer définitivement ton compte ?")) return;

        try {
            userService.deleteSelf(user.getId());
            try { session.setCurrentUser(null); } catch (Exception ignored) {}

            UI.showSuccess(this, "Compte supprimé.");
            dispose();
            new LoginFrame(authService, userService, session).setVisible(true);
        } catch (Exception ex) {
            UI.showError(this, ex.getMessage());
        }
    }

    private void doUpdateMyAccount() {
        try {
            String newEmail = JOptionPane.showInputDialog(this, "Nouvel email:", user.getEmail());
            if (newEmail == null) return;
            newEmail = newEmail.trim().toLowerCase();
            if (newEmail.isBlank()) throw new IllegalArgumentException("Email obligatoire.");

            String newPseudo = JOptionPane.showInputDialog(this, "Nouveau pseudo:", user.getPseudo());
            if (newPseudo == null) return;
            newPseudo = newPseudo.trim();
            if (newPseudo.isBlank()) throw new IllegalArgumentException("Pseudo obligatoire.");

            userService.updateSelf(user.getId(), newEmail, newPseudo);

            user.setEmail(newEmail);
            user.setPseudo(newPseudo);
            setTitle("iStore - Employé (" + user.getEmail() + ")");

            UI.showSuccess(this, "Compte mis à jour.");
        } catch (Exception ex) {
            UI.showError(this, ex.getMessage());
        }
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu account = new JMenu("Compte");

        JMenuItem profile = new JMenuItem("Mon profil");
        profile.addActionListener(e -> doUpdateMyAccount());

        JMenuItem delete = new JMenuItem("Supprimer mon compte");
        delete.addActionListener(e -> doDeleteMyAccount());

        JMenuItem logout = new JMenuItem("Déconnexion");
        logout.addActionListener(e -> doLogout());

        account.add(profile);
        account.add(delete);
        account.addSeparator();
        account.add(logout);

        bar.add(account);
        return bar;
    }

    private void doLogout() {
        try { session.setCurrentUser(null); } catch (Exception ignored) {}
        UI.showInfo(this, "Déconnecté.");
        dispose();
        new LoginFrame(authService, userService, session).setVisible(true);
    }
}
