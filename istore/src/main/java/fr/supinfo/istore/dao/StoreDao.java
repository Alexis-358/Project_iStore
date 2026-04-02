package fr.supinfo.istore.dao;

import fr.supinfo.istore.db.DbConnection;
import fr.supinfo.istore.model.Store;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StoreDao {

    public List<Store> findAll() throws Exception {
        String sql = "SELECT id, name FROM stores ORDER BY name";
        List<Store> stores = new ArrayList<>();
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                stores.add(new Store(rs.getLong("id"), rs.getString("name")));
            }
        }
        return stores;
    }

    public long insert(String name) throws Exception {
        String sql = "INSERT INTO stores(name) VALUES (?)";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    public void delete(long storeId) throws Exception {
        String sql = "DELETE FROM stores WHERE id = ?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, storeId);
            ps.executeUpdate();
        }
    }

    public List<Store> findStoresForEmployee(long userId) throws Exception {
        String sql = """
        SELECT s.id, s.name
        FROM stores s
        JOIN store_employees se ON se.store_id = s.id
        WHERE se.user_id = ?
        ORDER BY s.name
    """;

        List<Store> stores = new ArrayList<>();
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    stores.add(new Store(rs.getLong("id"), rs.getString("name")));
                }
            }
        }
        return stores;
    }

}
