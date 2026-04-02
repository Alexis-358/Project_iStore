package fr.supinfo.istore.dao;

import fr.supinfo.istore.db.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WhitelistDao {

    private String normEmail(String email) {
        return (email == null) ? null : email.trim().toLowerCase();
    }

    public boolean isWhitelisted(String email) throws Exception {
        email = normEmail(email);
        if (email == null || email.isBlank()) return false;

        String sql = "SELECT 1 FROM whitelist_emails WHERE email = ?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void addEmail(String email) throws Exception {
        email = normEmail(email);
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email obligatoire.");

        // Optionnel: éviter doublons (si tu n'as pas UNIQUE en DB)
        if (isWhitelisted(email)) {
            throw new IllegalArgumentException("Email déjà présent dans la whitelist.");
        }

        String sql = "INSERT INTO whitelist_emails(email) VALUES (?)";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.executeUpdate();
        }
    }

    public int deleteEmail(String email) throws Exception {
        email = normEmail(email);
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email obligatoire.");

        String sql = "DELETE FROM whitelist_emails WHERE email = ?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            return ps.executeUpdate();
        }
    }

    public List<String> findAllEmails() throws Exception {
        String sql = "SELECT email FROM whitelist_emails ORDER BY email";
        List<String> emails = new ArrayList<>();
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                emails.add(rs.getString("email"));
            }
        }
        return emails;
    }
}
