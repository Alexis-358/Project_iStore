package fr.supinfo.istore.service;

import fr.supinfo.istore.dao.UserDao;
import fr.supinfo.istore.model.Role;
import fr.supinfo.istore.security.Session;

import java.sql.SQLException;

public class UserService {
    private final UserDao userDao;
    private final Session session;

    public UserService(UserDao userDao, Session session) {
        this.userDao = userDao;
        this.session = session;
    }

    public void adminUpdateEmployee(long targetId, String email, String pseudo) throws SQLException {
        if (session.getCurrentUser().getRole() != Role.ADMIN) {
            throw new SecurityException("Admin only");
        }
        int rows = userDao.updateEmployee(targetId, email, pseudo);
        if (rows == 0) {
            throw new IllegalArgumentException("Employee not found or not EMPLOYEE");
        }
    }

    public void updateSelf(long targetId, String email, String pseudo) throws SQLException {
        boolean isAdmin = session.getCurrentUser().getRole() == Role.ADMIN;
        boolean isSelf = session.getCurrentUser().getId() == targetId;
        if (!isAdmin && !isSelf) {
            throw new SecurityException("Forbidden");
        }
        userDao.updateUser(targetId, email, pseudo);
    }

    public void deleteSelf(long targetId) throws SQLException {
        boolean isAdmin = session.getCurrentUser().getRole() == Role.ADMIN;
        boolean isSelf  = session.getCurrentUser().getId() == targetId;
        if (!isAdmin && !isSelf) throw new SecurityException("Forbidden");

        int rows = userDao.deleteUser(targetId);
        if (rows == 0) throw new IllegalArgumentException("Suppression impossible (id introuvable).");
    }
    public void adminDeleteEmployee(long employeeId) throws SQLException {
        if (session.getCurrentUser().getRole() != Role.ADMIN) {
            throw new SecurityException("Admin only");
        }

        int rows = userDao.deleteEmployee(employeeId); // delete + FK cleanup + WHERE role='EMPLOYEE'
        if (rows == 0) {
            throw new IllegalArgumentException("Employé introuvable.");
        }
    }


}
