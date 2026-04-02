package fr.supinfo.istore.dao;

import fr.supinfo.istore.db.DbConnection;
import fr.supinfo.istore.model.Item;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDao {

    public List<Item> findAll() throws Exception {
        String sql = "SELECT id, name, price FROM items ORDER BY name";
        List<Item> items = new ArrayList<>();
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(new Item(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getDouble("price")
                ));
            }
        }
        return items;
    }

    public long insert(String name, double price) throws Exception {
        String sql = "INSERT INTO items(name, price) VALUES (?, ?)";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    public void delete(long itemId) throws Exception {
        String sql = "DELETE FROM items WHERE id = ?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, itemId);
            ps.executeUpdate();
        }
    }
}
