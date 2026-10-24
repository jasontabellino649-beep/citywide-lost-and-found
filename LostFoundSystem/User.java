import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int id;
    private final String username;
    private final String passwordHash;   // ENCAPSULATION: raw password is never stored
    private final String fullName;
    private final String contact;

    protected User(int id, String username, String password, String fullName, String contact) {
        this.id = id;
        this.username = username;
        this.passwordHash = hash(password);
        this.fullName = fullName;
        this.contact = contact;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getContact() { return contact; }

    public boolean checkPassword(String password) {
        return password != null && passwordHash.equals(hash(password));
    }

    // Each subclass defines its own role and permissions
    public abstract String getRole();
    public abstract boolean canModerate();

    private static String hash(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override public String toString() { return fullName + " (" + username + ")"; }
}
