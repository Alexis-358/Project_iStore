package fr.supinfo.istore.ui;

import fr.supinfo.istore.model.User;
import fr.supinfo.istore.security.Session;
import fr.supinfo.istore.service.AuthService;
import fr.supinfo.istore.service.UserService;

import javax.swing.*;
import java.awt.*;

public class RegisterFrame extends JFrame {

    private final JTextField emailField = new JTextField(24);
    private final JTextField pseudoField = new JTextField(24);
    private final JPasswordField passwordField = new JPasswordField(24);

    private final AuthService authService;
    private final UserService userService;
    private final Session session;

    public RegisterFrame(AuthService authService, UserService userService, Session session) {
        this.authService = authService;
        this.userService = userService;
        this.session = session;

        setTitle("iStore - Inscription");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(720, 460));
        setLocationRelativeTo(null);

        getContentPane().setBackground(UI.BG);

        JLabel title = new JLabel("Inscription");
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
        form.add(new JLabel("Pseudo"), gbc);
        gbc.gridx = 1;
        form.add(pseudoField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("Mot de passe"), gbc);
        gbc.gridx = 1;
        form.add(passwordField, gbc);
        row++;

        JButton registerBtn = new JButton("Créer le compte");
        UI.stylePrimary(registerBtn);
        registerBtn.addActionListener(e -> doRegister());

        JButton backBtn = new JButton("Retour connexion");
        UI.stylePrimary(backBtn);
        backBtn.addActionListener(e -> backToLogin());

        JPanel actions = UI.flowRight(backBtn, registerBtn);

        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        form.add(actions, gbc);

        setLayout(new BorderLayout());
        add(title, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);

        pack();
    }

    private void doRegister() {
        try {
            String email = LoginFrame.normEmail(emailField.getText());
            String pseudo = pseudoField.getText().trim();
            String password = new String(passwordField.getPassword());

            User created = authService.register(email, pseudo, password);

            UI.showSuccess(this, "Compte créé: " + created.getEmail() + " (role: " + created.getRole() + ")");
            backToLogin();
        } catch (Exception ex) {
            UI.showError(this, ex.getMessage());
        }
    }

    private void backToLogin() {
        new LoginFrame(authService, userService, session).setVisible(true);
        dispose();
    }
}
