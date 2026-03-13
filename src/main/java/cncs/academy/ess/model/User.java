package cncs.academy.ess.model;

public class User {

    private int id;
    private String username;
    private String passwordHash;
    private String salt;
    private String role;

    public User(int id, String username, String passwordHash, String salt, String role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.role = role;
    }

    public User(String username, String passwordHash, String salt, String role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.role = role;
    }

    // getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getSalt() { return salt; }
    public String getRole() { return role; }

    public void setId(int id) { this.id = id; }
}


