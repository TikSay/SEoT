package Login;

public class User {
    private final int id;
    private final String username;
    private final String email;
    private final String fornavn;
    private final String etternavn;
    private final String lokallag;

    public User(int id, String username, String email) {
        this(id, username, email, null, null, null);
    }

    public User(int id, String username, String email, String fornavn, String etternavn, String lokallag) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fornavn = fornavn;
        this.etternavn = etternavn;
        this.lokallag = lokallag;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFornavn() {
        return fornavn;
    }

    public String getEtternavn() {
        return etternavn;
    }

    public String getLokallag() {
        return lokallag;
    }
}
