package fr.supinfo.istore.db;

import fr.supinfo.istore.db.DbConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class MainTestDb {
    public static void main(String[] args) throws Exception {
        try (Connection c = DbConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT 1")) {
            rs.next();
            System.out.println("DB OK -> " + rs.getInt(1));
        }
    }
}
