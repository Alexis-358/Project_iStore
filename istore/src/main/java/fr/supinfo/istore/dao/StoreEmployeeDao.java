package fr.supinfo.istore.dao;

import fr.supinfo.istore.db.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StoreEmployeeDao {

    public void addAccess(long storeId, long userId) throws Exception {
        String sql = "INSERT INTO store_employees(store_id, user_id) VALUES (?, ?)";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, storeId);
            ps.setLong(2, userId);
            ps.executeUpdate();
        }
    }

    public List<Long> findUserIdsByStore(long storeId) throws Exception {
        String sql = "SELECT user_id FROM store_employees WHERE store_id = ? ORDER BY user_id";
        List<Long> ids = new ArrayList<>();
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, storeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getLong("user_id"));
            }
        }
        return ids;
    }
    public int removeAllAccessForUser(long userId) throws Exception {
        String sql = "DELETE FROM store_employees WHERE user_id = ?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            return ps.executeUpdate();
        }
    }

}
