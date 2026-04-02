package fr.supinfo.istore.dao;

import fr.supinfo.istore.db.DbConnection;
import fr.supinfo.istore.model.InventoryLine;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockDao {

    // Inventaire consultable (store -> liste articles + prix + quantité)
    public List<InventoryLine> findInventoryByStore(long storeId) throws Exception {
        String sql = """
            SELECT si.store_id, si.item_id, i.name, i.price, si.quantity
            FROM store_items si
            JOIN items i ON i.id = si.item_id
            WHERE si.store_id = ?
            ORDER BY i.name
        """;

        List<InventoryLine> lines = new ArrayList<>();
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, storeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lines.add(new InventoryLine(
                            rs.getLong("store_id"),
                            rs.getLong("item_id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getInt("quantity")
                    ));
                }
            }
        }
        return lines;
    }

    // Admin: ajouter un article au stock du magasin (création ligne si absente)
    public void addItemToStore(long storeId, long itemId, int initialQty) throws Exception {
        if (initialQty < 0) throw new IllegalArgumentException("Quantité initiale invalide.");

        String sql = "INSERT INTO store_items(store_id, item_id, quantity) VALUES (?, ?, ?)";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, storeId);
            ps.setLong(2, itemId);
            ps.setInt(3, initialQty);
            ps.executeUpdate();
        }
    }

    // Employé: ajuster quantité (+/-) en garantissant quantité >= 0
    public void adjustQuantity(long storeId, long itemId, int delta) throws Exception {
        // On fait le contrôle en SQL pour éviter les courses (stock qui passe négatif)
        String sql = """
            UPDATE store_items
            SET quantity = quantity + ?
            WHERE store_id = ? AND item_id = ? AND (quantity + ?) >= 0
        """;

        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setLong(2, storeId);
            ps.setLong(3, itemId);
            ps.setInt(4, delta);

            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new IllegalArgumentException("Stock insuffisant (ne peut pas passer sous 0).");
            }
        }
    }
}
