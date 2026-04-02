package fr.supinfo.istore.service;

import fr.supinfo.istore.dao.UserDao;
import fr.supinfo.istore.dao.WhitelistDao;
import fr.supinfo.istore.model.Role;
import fr.supinfo.istore.model.User;
import fr.supinfo.istore.security.PasswordHasher;
import fr.supinfo.istore.security.Session;

public class AuthService {

    private final UserDao userDao;
    private final WhitelistDao whitelistDao;
    private final Session session;

    public AuthService(UserDao userDao, WhitelistDao whitelistDao, Session session) {
        this.userDao = userDao;
        this.whitelistDao = whitelistDao;
        this.session = session;
    }

    private String normEmail(String email) {
        return (email == null) ? null : email.trim().toLowerCase();
    }

    private void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message); // validation d’arguments
    }

    private boolean looksLikeEmail(String email) {
        // Simple, suffisant pour un projet Swing
        return email != null && email.contains("@") && email.contains(".");
    }

    public User register(String email, String pseudo, String password) throws Exception {
        email = normEmail(email);

        require(email != null && !email.isBlank(), "Email obligatoire.");
        require(looksLikeEmail(email), "Email invalide.");
        require(pseudo != null && !pseudo.isBlank(), "Pseudo obligatoire.");
        require(password != null && !password.isBlank(), "Mot de passe obligatoire.");
        require(password.length() >= 6, "Mot de passe trop court (min 6 caractères).");

        boolean firstRun = (userDao.countUsers() == 0);

        // Whitelist obligatoire seulement après initialisation
        if (!firstRun) {
            if (!whitelistDao.isWhitelisted(email)) {
                throw new IllegalArgumentException("Email non autorisé (pas dans la whitelist).");
            }
        }

        if (userDao.findByEmail(email) != null) {
            throw new IllegalArgumentException("Email déjà utilisé.");
        }

        Role role = firstRun ? Role.ADMIN : Role.EMPLOYEE;

        User u = new User();
        u.setEmail(email);
        u.setPseudo(pseudo.trim());
        u.setPasswordHash(PasswordHasher.hash(password));
        u.setRole(role);

        long id = userDao.insert(u);
        u.setId(id);
        return u;
    }


    public User login(String email, String password) throws Exception {
        email = normEmail(email);

        require(email != null && !email.isBlank(), "Email obligatoire.");
        require(password != null && !password.isBlank(), "Mot de passe obligatoire.");

        User u = userDao.findByEmail(email);
        if (u == null) throw new IllegalArgumentException("Email inconnu.");

        if (!PasswordHasher.verify(password, u.getPasswordHash())) {
            throw new IllegalArgumentException("Mot de passe incorrect.");
        }

        session.setCurrentUser(u);
        return u;
    }
}
