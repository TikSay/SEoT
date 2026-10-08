package Login;

/** Det frontend sender til POST /api/forgot-password: {"email": "..."} */
public class ForgotPasswordRequest {
    private String email;

    public ForgotPasswordRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
