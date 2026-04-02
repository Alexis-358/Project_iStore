package fr.supinfo.istore.model;

public class User {
    private long id;
    private String email;
    private String pseudo;
    private String passwordHash;
    private Role role;

    public User() {}

    public User(long id, String email, String pseudo, String passwordHash, Role role) {
        this.id = id;
        this.email = email;
        this.pseudo = pseudo;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPseudo() { return pseudo; }
    public void setPseudo(String pseudo) { this.pseudo = pseudo; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
