package fr.supinfo.istore;

import com.formdev.flatlaf.FlatIntelliJLaf;
import fr.supinfo.istore.dao.UserDao;
import fr.supinfo.istore.dao.WhitelistDao;
import fr.supinfo.istore.security.Session;
import fr.supinfo.istore.service.AuthService;
import fr.supinfo.istore.service.UserService;
import fr.supinfo.istore.ui.LoginFrame;

import javax.swing.*;
import java.awt.*;

public class App {
    public static void main(String[] args) {

        FlatIntelliJLaf.setup();

        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 12);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));

        // Zebra rows + grille (FlatLaf)
        UIManager.put("Table.alternateRowColor", new Color(235, 238, 245));
        UIManager.put("Table.gridColor", new Color(220, 224, 234));
        UIManager.put("Table.intercellSpacing", new Dimension(10, 6));

        SwingUtilities.invokeLater(() -> {
            Session session = new Session();

            UserDao userDao = new UserDao();
            WhitelistDao whitelistDao = new WhitelistDao();

            AuthService authService = new AuthService(userDao, whitelistDao, session);
            UserService userService = new UserService(userDao, session);

            new LoginFrame(authService, userService, session).setVisible(true);
        });
    }
}
