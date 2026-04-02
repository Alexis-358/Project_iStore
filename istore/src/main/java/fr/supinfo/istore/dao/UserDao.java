package fr.supinfo.istore.dao;

import fr.supinfo.istore.db.DbConnection;
import fr.supinfo.istore.model.Role;
import fr.supinfo.istore.model.User;

import java.sql.*;

public class UserDao {

    private String normEmail(String email) {
        return (email == null) ? null : email.trim().toLowerCase();
    }

    public long countUsers() throws Exception {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    public User findByEmail(String email) throws Exception {
        email = normEmail(email);

        String sql = "SELECT id, email, pseudo, password_hash, role FROM users WHERE email = ?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new User(
                        rs.getLong("id"),
                        rs.getString("email"),
                        rs.getString("pseudo"),
                        rs.getString("password_hash"),
                        Role.valueOf(rs.getString("role"))
                );
            }
        }
    }

    public long insert(User user) throws Exception {
        String sql = "INSERT INTO users(email, pseudo, password_hash, role) VALUES (?, ?, ?, ?)";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, normEmail(user.getEmail()));
            ps.setString(2, user.getPseudo());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getRole().name());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    public User findById(long id) throws Exception {
        String sql = "SELECT id, email, pseudo, password_hash, role FROM users WHERE id = ?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new User(
                        rs.getLong("id"),
                        rs.getString("email"),
                        rs.getString("pseudo"),
                        rs.getString("password_hash"),
                        Role.valueOf(rs.getString("role"))
                );
            }
        }
    }

    public int updateEmployee(long id, String email, String pseudo) throws SQLException {
        String sql = "UPDATE users SET email=?, pseudo=? WHERE id=? AND role='EMPLOYEE'";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, normEmail(email));
            ps.setString(2, pseudo);
            ps.setLong(3, id);
            return ps.executeUpdate();
        }
    }

    public int updateUser(long id, String email, String pseudo) throws SQLException {
        String sql = "UPDATE users SET email=?, pseudo=? WHERE id=?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, normEmail(email));
            ps.setString(2, pseudo);
            ps.setLong(3, id);
            return ps.executeUpdate();
        }
    }

    public int deleteUser(long id) throws SQLException {
        try (Connection c = DbConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(
                        "DELETE FROM store_employees WHERE user_id = ?")) {
                    ps.setLong(1, id);
                    ps.executeUpdate();
                }

                int rows;
                try (PreparedStatement ps = c.prepareStatement(
                        "DELETE FROM users WHERE id = ?")) {
                    ps.setLong(1, id);
                    rows = ps.executeUpdate();
                }

                c.commit();
                return rows;
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public int deleteEmployee(long id) throws SQLException {
        try (Connection c = DbConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(
                        "DELETE FROM store_employees WHERE user_id = ?")) {
                    ps.setLong(1, id);
                    ps.executeUpdate();
                }

                int rows;
                try (PreparedStatement ps = c.prepareStatement(
                        "DELETE FROM users WHERE id = ? AND role='EMPLOYEE'")) {
                    ps.setLong(1, id);
                    rows = ps.executeUpdate();
                }

                c.commit();
                return rows;
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }
    public User findPublicByEmail(String email) throws Exception {
        email = (email == null) ? null : email.trim().toLowerCase();

        String sql = "SELECT id, email, pseudo, role FROM users WHERE email = ?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                User u = new User();
                u.setId(rs.getLong("id"));
                u.setEmail(rs.getString("email"));
                u.setPseudo(rs.getString("pseudo"));
                u.setRole(Role.valueOf(rs.getString("role")));
                return u;
            }
        }
    }
}
