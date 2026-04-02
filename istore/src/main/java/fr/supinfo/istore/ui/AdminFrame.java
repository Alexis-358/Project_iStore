package fr.supinfo.istore.ui;

import fr.supinfo.istore.dao.*;
import fr.supinfo.istore.model.InventoryLine;
import fr.supinfo.istore.model.Item;
import fr.supinfo.istore.model.Role;
import fr.supinfo.istore.model.Store;
import fr.supinfo.istore.model.User;
import fr.supinfo.istore.security.Session;
import fr.supinfo.istore.service.AuthService;
import fr.supinfo.istore.service.UserService;
import fr.supinfo.istore.ui.table.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class AdminFrame extends JFrame {

    // --- Users read/update (lecture stylée)
    private final JTextField userEmailSearchField = new JTextField(18);

    private final JPanel userCard = new JPanel(new GridBagLayout());
    private final JLabel userIdVal = new JLabel("-");
    private final JLabel userEmailVal = new JLabel("-");
    private final JLabel userPseudoVal = new JLabel("-");
    private final JLabel userRoleVal = new JLabel("-");
    private final JLabel userReadMsgLabel = new JLabel(" ");

    private final JTextField empIdField = new JTextField(6);
    private final JTextField empEmailField = new JTextField(18);
    private final JTextField empPseudoField = new JTextField(18);
    private final JLabel userAdminMsgLabel = new JLabel(" ");

    // --- Stock (ADMIN)
    private final StockDao stockDao = new StockDao();
    private final JComboBox<Store> stockStoreCombo = new JComboBox<>();
    private final JComboBox<Item> stockItemCombo = new JComboBox<>();
    private final JTextField stockQtyField = new JTextField(8);
    private final JLabel stockMessageLabel = new JLabel(" ");
    private final InventoryTableModel stockTableModel = new InventoryTableModel();
    private final JTable stockTable = new JTable(stockTableModel);

    // --- Items
    private final ItemDao itemDao = new ItemDao();
    private final JTextField itemNameField = new JTextField(22);
    private final JTextField itemPriceField = new JTextField(10);
    private final JLabel itemMessageLabel = new JLabel(" ");
    private final ItemTableModel itemTableModel = new ItemTableModel();
    private final JTable itemTable = new JTable(itemTableModel);

    // --- Whitelist
    private final WhitelistDao whitelistDao = new WhitelistDao();
    private final JTextField wlEmailField = new JTextField(28);
    private final JLabel wlMessageLabel = new JLabel(" ");
    private final WhitelistTableModel wlTableModel = new WhitelistTableModel();
    private final JTable wlTable = new JTable(wlTableModel);

    // --- Stores (JTable)
    private final StoreDao storeDao = new StoreDao();
    private final JTextField storeNameField = new JTextField(22);
    private final JLabel storeMessageLabel = new JLabel(" ");
    private final StoreTableModel storeTableModel = new StoreTableModel();
    private final JTable storeTable = new JTable(storeTableModel);

    // --- Store access (JTable)
    private final StoreEmployeeDao storeEmployeeDao = new StoreEmployeeDao();
    private final UserDao userDao = new UserDao();
    private final JComboBox<Store> storeCombo = new JComboBox<>();
    private final JTextField employeeEmailField = new JTextField(24);
    private final JLabel accessMessageLabel = new JLabel(" ");
    private final AccessTableModel accessTableModel = new AccessTableModel();
    private final JTable accessTable = new JTable(accessTableModel);

    // --- Services/session
    private final AuthService authService;
    private final UserService userService;
    private final Session session;
    private final User currentUser;

    // --- Renderers
    private final DefaultTableCellRenderer moneyRenderer = new DefaultTableCellRenderer() {
        private final DecimalFormat df = new DecimalFormat("0.00");
        @Override public void setValue(Object value) {
            if (value instanceof Number n) setText(df.format(n.doubleValue()) + " €");
            else setText("");
            setHorizontalAlignment(SwingConstants.RIGHT);
        }
    };

    public AdminFrame(User user, AuthService authService, UserService userService, Session session) {
        this.currentUser = user;
        this.authService = authService;
        this.userService = userService;
        this.session = session;

        setTitle("iStore - Admin (" + user.getEmail() + ")");
        setSize(1050, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setJMenuBar(buildMenuBar());
        getContentPane().setBackground(UI.BG);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Whitelist", buildWhitelistPanel());
        tabs.addTab("Magasins", buildStoresPanel());
        tabs.addTab("Accès magasin", buildAccessPanel());
        tabs.addTab("Articles", buildItemsPanel());
        tabs.addTab("Stock", buildStockPanel());
        tabs.addTab("Utilisateurs", buildUsersPanel());

        add(tabs);

        refreshWhitelist();
        refreshStores();
        refreshItems();
        refreshStockCombos();
        refreshStockList();
    }

    private String normEmail(String email) {
        return (email == null) ? null : email.trim().toLowerCase();
    }

    // ---------------- MENU
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

    private void doUpdateMyAccount() {
        try {
            String newEmail = JOptionPane.showInputDialog(this, "Nouvel email:", currentUser.getEmail());
            if (newEmail == null) return;
            newEmail = newEmail.trim().toLowerCase();
            if (newEmail.isBlank()) throw new IllegalArgumentException("Email obligatoire.");

            String newPseudo = JOptionPane.showInputDialog(this, "Nouveau pseudo:", currentUser.getPseudo());
            if (newPseudo == null) return;
            newPseudo = newPseudo.trim();
            if (newPseudo.isBlank()) throw new IllegalArgumentException("Pseudo obligatoire.");

            userService.updateSelf(currentUser.getId(), newEmail, newPseudo);

            currentUser.setEmail(newEmail);
            currentUser.setPseudo(newPseudo);
            setTitle("iStore - Admin (" + currentUser.getEmail() + ")");

            UI.showSuccess(this, "Compte mis à jour.");
        } catch (Exception ex) {
            UI.showError(this, ex.getMessage());
        }
    }

    private void doDeleteMyAccount() {
        if (!UI.confirm(this, "Supprimer définitivement ton compte ?")) return;

        try {
            userService.deleteSelf(currentUser.getId());
            try { session.setCurrentUser(null); } catch (Exception ignored) {}

            UI.showSuccess(this, "Compte supprimé.");
            dispose();
            new LoginFrame(authService, userService, session).setVisible(true);
        } catch (Exception ex) {
            UI.showError(this, ex.getMessage());
        }
    }

    // ---------------- WHITELIST
    private JPanel buildWhitelistPanel() {
        JPanel panel = new JPanel(new BorderLayout(UI.GAP, UI.GAP));
        UI.stylePanel(panel);

        JButton addBtn = new JButton("Ajouter");
        UI.stylePrimary(addBtn);
        addBtn.addActionListener(e -> addWhitelistEmail());

        JButton deleteBtn = new JButton("Supprimer");
        UI.styleDanger(deleteBtn);
        deleteBtn.addActionListener(e -> deleteSelectedWhitelistEmail());

        JPanel top = UI.flowLeft(
                new JLabel("Email à autoriser:"),
                wlEmailField,
                addBtn,
                deleteBtn
        );

        UI.styleTable(wlTable);
        wlTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(wlTable), BorderLayout.CENTER);
        panel.add(wlMessageLabel, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshWhitelist() {
        try {
            List<String> emails = whitelistDao.findAllEmails();
            wlTableModel.setRows(emails);

            wlMessageLabel.setForeground(UI.SUCCESS);
            wlMessageLabel.setText("Whitelist chargée (" + emails.size() + " emails).");
        } catch (Exception ex) {
            wlMessageLabel.setForeground(UI.DANGER);
            wlMessageLabel.setText(ex.getMessage());
        }
    }

    private void addWhitelistEmail() {
        try {
            String email = normEmail(wlEmailField.getText());
            if (email == null || email.isBlank()) throw new IllegalArgumentException("Email obligatoire.");

            whitelistDao.addEmail(email);
            wlEmailField.setText("");
            refreshWhitelist();
            UI.showSuccess(this, "Email ajouté: " + email);
        } catch (Exception ex) {
            String msg = ex.getMessage();
            String low = (msg == null) ? "" : msg.toLowerCase();
            if (low.contains("duplicate") || low.contains("duplicata") || low.contains("déjà présent"))
                UI.showError(this, "Cet email est déjà dans la whitelist.");
            else UI.showError(this, msg);
        }
    }

    private void deleteSelectedWhitelistEmail() {
        try {
            int viewRow = wlTable.getSelectedRow();
            if (viewRow < 0) throw new IllegalArgumentException("Sélectionne un email dans la table.");

            int modelRow = wlTable.convertRowIndexToModel(viewRow); // si tri/RowSorter
            String email = wlTableModel.getRow(modelRow);

            if (!UI.confirm(this, "Retirer de la whitelist: " + email + " ?")) return;

            int rows = whitelistDao.deleteEmail(email);
            if (rows == 0) throw new IllegalArgumentException("Email introuvable (déjà supprimé).");

            refreshWhitelist();
            UI.showSuccess(this, "Email retiré: " + email);
        } catch (Exception ex) {
            UI.showError(this, ex.getMessage());
        }
    }

    // ---------------- STORES (JTable)
    private JPanel buildStoresPanel() {
        JPanel panel = new JPanel(new BorderLayout(UI.GAP, UI.GAP));
        UI.stylePanel(panel);

        JButton createBtn = new JButton("Créer");
        UI.stylePrimary(createBtn);
        createBtn.addActionListener(e -> createStore());

        JButton deleteBtn = new JButton("Supprimer le magasin sélectionné");
        UI.styleDanger(deleteBtn);
        deleteBtn.addActionListener(e -> deleteSelectedStore());

        JPanel top = UI.flowLeft(new JLabel("Nom du magasin:"), storeNameField, createBtn);

        JPanel south = new JPanel(new BorderLayout(UI.GAP, UI.GAP));
        UI.styleSubPanel(south);
        south.add(storeMessageLabel, BorderLayout.CENTER);
        south.add(deleteBtn, BorderLayout.EAST);

        UI.styleTable(storeTable);
        storeTable.getColumnModel().getColumn(0).setMinWidth(60);
        storeTable.getColumnModel().getColumn(0).setMaxWidth(90);
        UI.rightAlignColumn(storeTable, 0);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(storeTable), BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshStores() {
        try {
            List<Store> stores = storeDao.findAll();
            storeTableModel.setRows(stores);

            storeMessageLabel.setForeground(UI.SUCCESS);
            storeMessageLabel.setText("Magasins chargés (" + stores.size() + ").");
        } catch (Exception ex) {
            storeMessageLabel.setForeground(UI.DANGER);
            storeMessageLabel.setText(ex.getMessage());
        }
        refreshStoreCombo();
        refreshAccessList();
    }

    private void createStore() {
        try {
            String name = storeNameField.getText().trim();
            if (name.isBlank()) throw new IllegalArgumentException("Nom du magasin obligatoire.");

            storeDao.insert(name);
            storeNameField.setText("");

            refreshStores();
            refreshStockCombos();
            refreshStockList();

            UI.showSuccess(this, "Magasin créé: " + name);
        } catch (Exception ex) {
            String msg = ex.getMessage();
            String low = (msg == null) ? "" : msg.toLowerCase();
            if (low.contains("duplicate") || low.contains("duplicata")) UI.showError(this, "Ce nom de magasin existe déjà.");
            else UI.showError(this, msg);
        }
    }

    private void deleteSelectedStore() {
        try {
            int viewRow = storeTable.getSelectedRow();
            if (viewRow < 0) throw new IllegalArgumentException("Sélectionne un magasin.");

            int modelRow = storeTable.convertRowIndexToModel(viewRow);
            Store selected = storeTableModel.getRow(modelRow);

            if (!UI.confirm(this, "Supprimer le magasin: " + selected.getName() + " ?")) return;

            storeDao.delete(selected.getId());
            refreshStores();
            refreshStockCombos();
            refreshStockList();

            UI.showSuccess(this, "Magasin supprimé: " + selected.getName());
        } catch (Exception ex) {
            String msg = ex.getMessage();
            String low = (msg == null) ? "" : msg.toLowerCase();
            if (low.contains("foreign key") || low.contains("constraint"))
                UI.showError(this, "Impossible de supprimer: ce magasin a encore du stock et/ou des employés associés.");
            else UI.showError(this, msg);
        }
    }

    // ---------------- ACCESS (JTable)
    private JPanel buildAccessPanel() {
        JPanel panel = new JPanel(new BorderLayout(UI.GAP, UI.GAP));
        UI.stylePanel(panel);

        JButton addBtn = new JButton("Ajouter accès");
        UI.stylePrimary(addBtn);
        addBtn.addActionListener(e -> addStoreAccess());

        JButton deleteEmployeeBtn = new JButton("Supprimer employé");
        UI.styleDanger(deleteEmployeeBtn);
        deleteEmployeeBtn.addActionListener(e -> deleteEmployeeByEmail());

        storeCombo.addActionListener(e -> refreshAccessList());

        JPanel top = UI.flowLeft(
                new JLabel("Magasin:"),
                storeCombo,
                new JLabel("Email employé:"),
                employeeEmailField,
                addBtn,
                deleteEmployeeBtn
        );

        UI.styleTable(accessTable);
        accessTable.getColumnModel().getColumn(0).setMinWidth(60);
        accessTable.getColumnModel().getColumn(0).setMaxWidth(90);
        UI.rightAlignColumn(accessTable, 0);

        accessTable.getColumnModel().getColumn(2).setMinWidth(120);
        accessTable.getColumnModel().getColumn(2).setMaxWidth(140);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(accessTable), BorderLayout.CENTER);
        panel.add(accessMessageLabel, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshStoreCombo() {
        try {
            storeCombo.removeAllItems();
            for (Store s : storeDao.findAll()) storeCombo.addItem(s);
        } catch (Exception ignored) {}
    }

    private void refreshAccessList() {
        try {
            Store store = (Store) storeCombo.getSelectedItem();
            if (store == null) {
                accessTableModel.setRows(List.of());
                return;
            }

            List<AccessTableModel.AccessRow> rows = new ArrayList<>();
            rows.add(new AccessTableModel.AccessRow(0L, "admin@istore.com", Role.ADMIN));

            for (Long userId : storeEmployeeDao.findUserIdsByStore(store.getId())) {
                User u = userDao.findById(userId);
                if (u != null) rows.add(new AccessTableModel.AccessRow(u.getId(), u.getEmail(), u.getRole()));
            }

            accessTableModel.setRows(rows);

            accessMessageLabel.setForeground(UI.SUCCESS);
            accessMessageLabel.setText("Accès chargés (" + rows.size() + ").");
        } catch (Exception ex) {
            accessMessageLabel.setForeground(UI.DANGER);
            accessMessageLabel.setText(ex.getMessage());
        }
    }

    private void addStoreAccess() {
        try {
            Store store = (Store) storeCombo.getSelectedItem();
            if (store == null) throw new IllegalArgumentException("Sélectionne un magasin.");

            String email = normEmail(employeeEmailField.getText());
            if (email == null || email.isBlank()) throw new IllegalArgumentException("Email employé obligatoire.");

            User u = userDao.findByEmail(email);
            if (u == null) throw new IllegalArgumentException("Utilisateur introuvable.");
            if (u.getRole() != Role.EMPLOYEE)
                throw new IllegalArgumentException("Seuls les EMPLOYEE peuvent être ajoutés au magasin.");

            storeEmployeeDao.addAccess(store.getId(), u.getId());

            employeeEmailField.setText("");
            refreshAccessList();

            UI.showSuccess(this, "Accès ajouté: " + email + " -> " + store.getName());
        } catch (Exception ex) {
            String msg = ex.getMessage();
            String low = (msg == null) ? "" : msg.toLowerCase();
            if (low.contains("duplicate") || low.contains("duplicata")) UI.showError(this, "Cet employé a déjà accès à ce magasin.");
            else UI.showError(this, msg);
        }
    }

    private void deleteEmployeeByEmail() {
        try {
            String email = normEmail(employeeEmailField.getText());
            if (email == null || email.isBlank()) throw new IllegalArgumentException("Email employé obligatoire.");

            User u = userDao.findByEmail(email);
            if (u == null) throw new IllegalArgumentException("Utilisateur introuvable.");
            if (u.getRole() != Role.EMPLOYEE)
                throw new IllegalArgumentException("Suppression autorisée uniquement pour EMPLOYEE.");

            if (!UI.confirm(this, "Supprimer l'employé: " + u.getEmail() + " ?")) return;

            userService.adminDeleteEmployee(u.getId());

            employeeEmailField.setText("");
            refreshAccessList();

            UI.showSuccess(this, "Employé supprimé: " + email);
        } catch (Exception ex) {
            UI.showError(this, ex.getMessage());
        }
    }

    // ---------------- ITEMS
    private JPanel buildItemsPanel() {
        JPanel panel = new JPanel(new BorderLayout(UI.GAP, UI.GAP));
        UI.stylePanel(panel);

        JButton createBtn = new JButton("Créer");
        UI.stylePrimary(createBtn);
        createBtn.addActionListener(e -> createItem());

        JButton deleteBtn = new JButton("Supprimer l'article sélectionné");
        UI.styleDanger(deleteBtn);
        deleteBtn.addActionListener(e -> deleteSelectedItem());

        JPanel top = UI.flowLeft(
                new JLabel("Nom:"),
                itemNameField,
                new JLabel("Prix:"),
                itemPriceField,
                createBtn
        );

        JPanel south = new JPanel(new BorderLayout(UI.GAP, UI.GAP));
        UI.styleSubPanel(south);
        south.add(itemMessageLabel, BorderLayout.CENTER);
        south.add(deleteBtn, BorderLayout.EAST);

        UI.styleTable(itemTable);

        itemTable.getColumnModel().getColumn(0).setMinWidth(60);
        itemTable.getColumnModel().getColumn(0).setMaxWidth(90);
        UI.rightAlignColumn(itemTable, 0);

        itemTable.getColumnModel().getColumn(2).setMinWidth(110);
        itemTable.getColumnModel().getColumn(2).setMaxWidth(140);
        itemTable.getColumnModel().getColumn(2).setCellRenderer(moneyRenderer);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(itemTable), BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshItems() {
        try {
            List<Item> items = itemDao.findAll();
            itemTableModel.setRows(items);

            itemMessageLabel.setForeground(UI.SUCCESS);
            itemMessageLabel.setText("Articles chargés (" + items.size() + ").");
        } catch (Exception ex) {
            itemMessageLabel.setForeground(UI.DANGER);
            itemMessageLabel.setText(ex.getMessage());
        }
    }

    private void createItem() {
        try {
            String name = itemNameField.getText().trim();
            String priceTxt = itemPriceField.getText().trim().replace(",", ".");

            if (name.isBlank()) throw new IllegalArgumentException("Nom d'article obligatoire.");
            if (priceTxt.isBlank()) throw new IllegalArgumentException("Prix obligatoire.");

            double price = Double.parseDouble(priceTxt);
            if (price < 0) throw new IllegalArgumentException("Le prix ne peut pas être négatif.");

            itemDao.insert(name, price);

            itemNameField.setText("");
            itemPriceField.setText("");

            refreshItems();
            refreshStockCombos();

            UI.showSuccess(this, "Article créé: " + name);
        } catch (NumberFormatException nfe) {
            UI.showError(this, "Prix invalide (ex: 12.50).");
        } catch (Exception ex) {
            String msg = ex.getMessage();
            String low = (msg == null) ? "" : msg.toLowerCase();
            if (low.contains("duplicate") || low.contains("duplicata")) UI.showError(this, "Ce nom d'article existe déjà.");
            else UI.showError(this, msg);
        }
    }

    private void deleteSelectedItem() {
        try {
            int viewRow = itemTable.getSelectedRow();
            if (viewRow < 0) throw new IllegalArgumentException("Sélectionne un article.");

            int modelRow = itemTable.convertRowIndexToModel(viewRow);
            Item selected = itemTableModel.getRow(modelRow);

            if (!UI.confirm(this, "Supprimer l'article: " + selected.getName() + " ?")) return;

            itemDao.delete(selected.getId());
            refreshItems();

            UI.showSuccess(this, "Article supprimé: " + selected.getName());
        } catch (Exception ex) {
            String msg = ex.getMessage();
            String low = (msg == null) ? "" : msg.toLowerCase();
            if (low.contains("foreign key") || low.contains("constraint"))
                UI.showError(this, "Impossible de supprimer: l'article est présent dans un stock de magasin.");
            else UI.showError(this, msg);
        }
    }

    // ---------------- STOCK
    private JPanel buildStockPanel() {
        JPanel panel = new JPanel(new BorderLayout(UI.GAP, UI.GAP));
        UI.stylePanel(panel);

        JButton addBtn = new JButton("Ajouter au stock");
        UI.stylePrimary(addBtn);
        addBtn.addActionListener(e -> addItemToStock());

        stockStoreCombo.addActionListener(e -> refreshStockList());

        JPanel top = UI.flowLeft(
                new JLabel("Magasin:"),
                stockStoreCombo,
                new JLabel("Article:"),
                stockItemCombo,
                new JLabel("Qté init:"),
                stockQtyField,
                addBtn
        );

        UI.styleTable(stockTable);

        stockTable.getColumnModel().getColumn(0).setMinWidth(60);
        stockTable.getColumnModel().getColumn(0).setMaxWidth(90);
        UI.rightAlignColumn(stockTable, 0);

        stockTable.getColumnModel().getColumn(2).setMinWidth(110);
        stockTable.getColumnModel().getColumn(2).setMaxWidth(140);
        stockTable.getColumnModel().getColumn(2).setCellRenderer(moneyRenderer);

        UI.rightAlignColumn(stockTable, 3);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(stockTable), BorderLayout.CENTER);
        panel.add(stockMessageLabel, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshStockCombos() {
        try {
            stockStoreCombo.removeAllItems();
            for (Store s : storeDao.findAll()) stockStoreCombo.addItem(s);

            stockItemCombo.removeAllItems();
            for (Item i : itemDao.findAll()) stockItemCombo.addItem(i);

            stockMessageLabel.setForeground(UI.SUCCESS);
            stockMessageLabel.setText("Listes magasins/articles chargées.");
        } catch (Exception ex) {
            stockMessageLabel.setForeground(UI.DANGER);
            stockMessageLabel.setText(ex.getMessage());
        }
    }

    private void refreshStockList() {
        try {
            Store store = (Store) stockStoreCombo.getSelectedItem();
            if (store == null) {
                stockTableModel.setRows(List.of());
                stockMessageLabel.setForeground(UI.DANGER);
                stockMessageLabel.setText("Sélectionne un magasin.");
                return;
            }

            List<InventoryLine> lines = stockDao.findInventoryByStore(store.getId());
            stockTableModel.setRows(lines);

            stockMessageLabel.setForeground(UI.SUCCESS);
            stockMessageLabel.setText("Inventaire chargé (" + lines.size() + " articles).");
        } catch (Exception ex) {
            stockMessageLabel.setForeground(UI.DANGER);
            stockMessageLabel.setText(ex.getMessage());
        }
    }

    private void addItemToStock() {
        try {
            Store store = (Store) stockStoreCombo.getSelectedItem();
            Item item = (Item) stockItemCombo.getSelectedItem();
            if (store == null) throw new IllegalArgumentException("Sélectionne un magasin.");
            if (item == null) throw new IllegalArgumentException("Sélectionne un article.");

            String qtyTxt = stockQtyField.getText().trim();
            if (qtyTxt.isBlank()) throw new IllegalArgumentException("Quantité initiale obligatoire.");

            int qty = Integer.parseInt(qtyTxt);
            if (qty < 0) throw new IllegalArgumentException("La quantité ne peut pas être négative.");

            stockDao.addItemToStore(store.getId(), item.getId(), qty);

            stockQtyField.setText("");
            refreshStockList();

            UI.showSuccess(this, "Article ajouté au stock: " + item.getName() + " (" + qty + ")");
        } catch (NumberFormatException nfe) {
            UI.showError(this, "Quantité invalide (nombre entier).");
        } catch (Exception ex) {
            String msg = ex.getMessage();
            String low = (msg == null) ? "" : msg.toLowerCase();
            if (low.contains("duplicate") || low.contains("duplicata")) UI.showError(this, "Cet article est déjà présent dans le stock de ce magasin.");
            else UI.showError(this, msg);
        }
    }

    // ---------------- USERS (stylé)
    private JPanel buildUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(UI.GAP, UI.GAP));
        UI.stylePanel(panel);

        JButton searchBtn = new JButton("Rechercher");
        UI.stylePrimary(searchBtn);
        searchBtn.addActionListener(e -> readUserByEmail());

        JPanel readTop = UI.flowLeft(new JLabel("Email:"), userEmailSearchField, searchBtn);

        JPanel readBox = new JPanel(new BorderLayout(UI.GAP, UI.GAP));
        UI.styleSubPanel(readBox);
        readBox.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Lire un utilisateur (sans mot de passe)",
                TitledBorder.LEFT, TitledBorder.TOP
        ));
        readBox.add(readTop, BorderLayout.NORTH);
        readBox.add(buildUserCard(), BorderLayout.CENTER);

        userReadMsgLabel.setForeground(UI.TEXT_MUTED);
        readBox.add(userReadMsgLabel, BorderLayout.SOUTH);

        JButton updateBtn = new JButton("Mettre à jour employé");
        UI.stylePrimary(updateBtn);
        updateBtn.addActionListener(e -> adminUpdateEmployeeUI());

        JPanel upd = UI.flowLeft(
                new JLabel("ID:"), empIdField,
                new JLabel("Email:"), empEmailField,
                new JLabel("Pseudo:"), empPseudoField,
                updateBtn
        );

        JPanel updBox = new JPanel(new BorderLayout(UI.GAP, UI.GAP));
        UI.styleSubPanel(updBox);
        updBox.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Admin: modifier un employé",
                TitledBorder.LEFT, TitledBorder.TOP
        ));
        updBox.add(upd, BorderLayout.NORTH);
        updBox.add(userAdminMsgLabel, BorderLayout.SOUTH);

        panel.add(readBox, BorderLayout.NORTH);
        panel.add(updBox, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildUserCard() {
        userCard.removeAll();

        userCard.setOpaque(true);
        userCard.setBackground(Color.WHITE);
        userCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 234)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        JLabel title = new JLabel("Utilisateur");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.weightx = 1;
        userCard.add(title, gbc);
        row++;

        gbc.gridwidth = 1;

        addUserCardRow(gbc, row++, "ID", userIdVal);
        addUserCardRow(gbc, row++, "Email", userEmailVal);
        addUserCardRow(gbc, row++, "Pseudo", userPseudoVal);
        addUserCardRow(gbc, row++, "Rôle", userRoleVal);

        return userCard;
    }

    private void addUserCardRow(GridBagConstraints gbc, int row, String label, JLabel value) {
        JLabel l = new JLabel(label + " :");
        l.setForeground(UI.TEXT_MUTED);
        l.setFont(l.getFont().deriveFont(Font.BOLD));

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        userCard.add(l, gbc);

        value.setForeground(new Color(30, 30, 30));
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1;
        userCard.add(value, gbc);
    }

    private void resetUserCard() {
        userIdVal.setText("-");
        userEmailVal.setText("-");
        userPseudoVal.setText("-");
        userRoleVal.setText("-");
        userRoleVal.setForeground(new Color(30, 30, 30));
    }

    private void readUserByEmail() {
        try {
            String email = userEmailSearchField.getText().trim().toLowerCase();
            if (email.isBlank()) throw new IllegalArgumentException("Email obligatoire.");

            User u = userDao.findPublicByEmail(email);
            if (u == null) throw new IllegalArgumentException("Utilisateur introuvable.");

            userIdVal.setText(String.valueOf(u.getId()));
            userEmailVal.setText(u.getEmail());
            userPseudoVal.setText(u.getPseudo());
            userRoleVal.setText(String.valueOf(u.getRole()));

            if (u.getRole() == Role.ADMIN) userRoleVal.setForeground(UI.DANGER);
            else if (u.getRole() == Role.EMPLOYEE) userRoleVal.setForeground(UI.SUCCESS);
            else userRoleVal.setForeground(new Color(30, 30, 30));

            userReadMsgLabel.setForeground(UI.SUCCESS);
            userReadMsgLabel.setText("Utilisateur chargé.");

            userCard.revalidate();
            userCard.repaint();
        } catch (Exception ex) {
            resetUserCard();
            userReadMsgLabel.setForeground(UI.DANGER);
            userReadMsgLabel.setText(ex.getMessage());
        }
    }

    private void adminUpdateEmployeeUI() {
        try {
            long id = Long.parseLong(empIdField.getText().trim());
            String email = empEmailField.getText().trim().toLowerCase();
            String pseudo = empPseudoField.getText().trim();

            if (email.isBlank()) throw new IllegalArgumentException("Email obligatoire.");
            if (pseudo.isBlank()) throw new IllegalArgumentException("Pseudo obligatoire.");

            userService.adminUpdateEmployee(id, email, pseudo);

            userAdminMsgLabel.setForeground(UI.SUCCESS);
            userAdminMsgLabel.setText("Employé mis à jour.");
        } catch (NumberFormatException nfe) {
            userAdminMsgLabel.setForeground(UI.DANGER);
            userAdminMsgLabel.setText("ID invalide.");
        } catch (Exception ex) {
            userAdminMsgLabel.setForeground(UI.DANGER);
            userAdminMsgLabel.setText(ex.getMessage());
        }
    }
}
