package Login;

public class Main {
    static void main() {
        LoginHandler handler;
        try {
            handler = new LoginHandler(DbConfig.load().createRegistry());
        } catch (IllegalStateException e) {
            IO.println("Configuration error: " + e.getMessage());
            return;
        }

        String email = IO.readln("Email: ");
        String password = IO.readln("Password: ");

        try {
            LoginResult result = handler.login(email, password);
            IO.println(result.getMessage());
            if (result.isSuccess()) {
                IO.println("Welcome, " + result.getUser().getUsername() + "!");
            }
        } catch (RuntimeException e) {
            IO.println("Login is unavailable right now. Please try again later.");
            System.err.println("Login error: " + e.getMessage());
        }
    }
}
