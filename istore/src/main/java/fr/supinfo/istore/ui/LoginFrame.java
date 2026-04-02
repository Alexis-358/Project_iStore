package fr.supinfo.istore.ui;

import fr.supinfo.istore.model.Role;
import fr.supinfo.istore.model.User;
import fr.supinfo.istore.security.Session;
import fr.supinfo.istore.service.AuthService;
import fr.supinfo.istore.service.UserService;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final JTextField emailField = new JTextField(24);
    private final JPasswordField passwordField = new JPasswordField(24);

    private final AuthService authService;
    private final UserService userService;
    private final Session session;

    public LoginFrame(AuthService authService, UserService userService, Session session) {
        this.authService = authService;
        this.userService = userService;
        this.session = session;

        setTitle("iStore - Connexion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(720, 420));
        setLocationRelativeTo(null);

        getContentPane().setBackground(UI.BG);

        JLabel title = new JLabel("Connexion");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));

        JPanel form = new JPanel(new GridBagLayout());
        UI.stylePanel(form);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("Email"), gbc);
        gbc.gridx = 1;
        form.add(emailField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("Mot de passe"), gbc);
        gbc.gridx = 1;
        form.add(passwordField, gbc);
        row++;

        JButton loginBtn = new JButton("Se connecter");
        UI.stylePrimary(loginBtn);
        loginBtn.addActionListener(e -> doLogin());

        JButton goRegisterBtn = new JButton("S'inscrire");
        UI.stylePrimary(goRegisterBtn);
        goRegisterBtn.addActionListener(e -> openRegister());

        JPanel actions = UI.flowRight(goRegisterBtn, loginBtn);

        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        form.add(actions, gbc);
        row++;

        JLabel hint = new JLabel("Pas de compte ? Clique sur \"S'inscrire\".");
        hint.setForeground(UI.TEXT_MUTED);
        hint.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));

        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        form.add(hint, gbc);

        setLayout(new BorderLayout());
        add(title, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);

        pack();
    }

    public static String normEmail(String email) {
        return (email == null) ? null : email.trim().toLowerCase();
    }

    private void doLogin() {
        try {
            String email = normEmail(emailField.getText());
            String password = new String(passwordField.getPassword());

            User user = authService.login(email, password);

            if (user.getRole() == Role.ADMIN) {
                new AdminFrame(user, authService, userService, session).setVisible(true);
            } else {
                new EmployeeFrame(user, authService, userService, session).setVisible(true);
            }
            dispose();
        } catch (Exception ex) {
            UI.showError(this, ex.getMessage());
        }
    }

    private void openRegister() {
        new RegisterFrame(authService, userService, session).setVisible(true);
        dispose();
    }
}
