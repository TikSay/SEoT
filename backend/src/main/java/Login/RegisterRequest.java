package Login;

/**
 * Det frontend sender til POST /api/register:
 * {"fornavn": "...", "etternavn": "...", "email": "...", "password": "...", "lokallag": "..."}
 */
public class RegisterRequest {
    private String fornavn;
    private String etternavn;
    private String email;
    private String password;
    private String lokallag;

    public RegisterRequest() {
    }

    public String getFornavn() {
        return fornavn;
    }

    public void setFornavn(String fornavn) {
        this.fornavn = fornavn;
    }

    public String getEtternavn() {
        return etternavn;
    }

    public void setEtternavn(String etternavn) {
        this.etternavn = etternavn;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLokallag() {
        return lokallag;
    }

    public void setLokallag(String lokallag) {
        this.lokallag = lokallag;
    }
}
