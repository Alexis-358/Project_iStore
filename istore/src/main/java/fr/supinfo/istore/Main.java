package fr.supinfo.istore;

import fr.supinfo.istore.dao.UserDao;
import fr.supinfo.istore.dao.WhitelistDao;
import fr.supinfo.istore.security.Session;
import fr.supinfo.istore.service.AuthService;

public class Main {
    public static void main(String[] args) throws Exception {
        Session session = new Session();
        UserDao userDao = new UserDao();
        WhitelistDao whitelistDao = new WhitelistDao();

        AuthService auth = new AuthService(userDao, whitelistDao, session);

        System.out.println("admin@istore.com whitelisted = " + whitelistDao.isWhitelisted("admin@istore.com"));

    }
}
