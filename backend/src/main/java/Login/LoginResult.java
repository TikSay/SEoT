package Login;

public class LoginResult {
    private final boolean success;
    private final String message;
    private final User user;

    public LoginResult(boolean success, String message) {
        this(success, message, null);
    }

    public LoginResult(boolean success, String message, User user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    /** Brukeren som logget inn, eller null hvis innloggingen feilet. */
    public User getUser() {
        return user;
    }
}
