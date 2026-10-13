package Login;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/** Tar imot innlogging fra frontend (JSON) og fra HTML-skjemaet, og sender det videre til LoginHandler. */
public class LoginController {

    private static final String SESSION_USER = "user";
    private static final String ERROR_PLACEHOLDER = "<!-- login-error -->";

    private final LoginHandler handler;
    private final String loginPage;

    public LoginController(LoginHandler handler) {
        this.handler = handler;
        this.loginPage = readLoginPage();
    }

    public void login(Context ctx) {
        LoginRequest request;
        try {
            request = ctx.bodyAsClass(LoginRequest.class);
        } catch (Exception e) {
            ctx.status(400).json(new LoginResult(false, "Invalid request."));
            return;
        }
        if (request == null) {
            ctx.status(400).json(new LoginResult(false, "Invalid request."));
            return;
        }

        try {
            LoginResult result = handler.login(request.getEmail(), request.getPassword());
            ctx.status(result.isSuccess() ? 200 : 401).json(result);
        } catch (RuntimeException e) {
            System.err.println("Login error: " + e.getMessage());
            ctx.status(500).json(new LoginResult(false, "Login is unavailable right now. Please try again later."));
        }
    }

    /** POST /login: HTML-skjemaet. Lager en sesjon og sender brukeren til /account hvis innloggingen gikk bra. */
    public void loginForm(Context ctx) {
        LoginResult result;
        try {
            result = handler.login(ctx.formParam("email"), ctx.formParam("password"));
        } catch (RuntimeException e) {
            System.err.println("Login error: " + e.getMessage());
            showLoginPage(ctx, 500, "Login is unavailable right now. Please try again later.");
            return;
        }
        if (!result.isSuccess()) {
            showLoginPage(ctx, 401, result.getMessage());
            return;
        }

        // Lag en ny sesjon, så en gammel sesjons-ID ikke kan gjenbrukes etter innlogging (session fixation)
        HttpSession oldSession = ctx.req().getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        HttpSession session = ctx.req().getSession(true);
        session.setAttribute(SESSION_USER, result.getUser());  // bare User, aldri passord-hashen
        ctx.redirect("/account", HttpStatus.SEE_OTHER);
    }

    /** GET /account: bare for innloggede. All info hentes fra sesjonen på serveren. */
    public void account(Context ctx) {
        HttpSession session = ctx.req().getSession(false);   // false: lager ikke sesjon for besøkende som ikke er logget inn
        User user = session == null ? null : (User) session.getAttribute(SESSION_USER);
        if (user == null) {
            ctx.redirect("/login.html");
            return;
        }
        ctx.header("Cache-Control", "no-store");
        ctx.html("""
                <!DOCTYPE html>
                <html lang="no">
                <head>
                    <meta charset="UTF-8">
                    <title>Min konto - Østfolds Husflidslag</title>
                    <link rel="stylesheet" href="/style.css">
                </head>
                <body>
                    <div class="container">
                        <main class="login-main">
                            <div class="login-box">
                                <h2>Min konto</h2>
                                <p>Brukernavn: %s</p>
                                <p>E-post: %s</p>
                                <form action="/logout" method="POST">
                                    <button type="submit" class="main-btn login-btn">LOGG UT</button>
                                </form>
                            </div>
                        </main>
                    </div>
                </body>
                </html>
                """.formatted(escapeHtml(user.getUsername()), escapeHtml(user.getEmail())));
    }

    /** POST /logout: avslutter sesjonen. */
    public void logout(Context ctx) {
        HttpSession session = ctx.req().getSession(false);
        if (session != null) {
            session.invalidate();
        }
        ctx.redirect("/login.html", HttpStatus.SEE_OTHER);
    }

    private void showLoginPage(Context ctx, int status, String message) {
        String error = "<p class=\"error\" role=\"alert\" style=\"color:#b00020\">" + escapeHtml(message) + "</p>";
        String page = loginPage.contains(ERROR_PLACEHOLDER)
                ? loginPage.replace(ERROR_PLACEHOLDER, error)
                : loginPage.replace("</form>", error + "</form>");
        ctx.status(status).header("Cache-Control", "no-store").html(page);
    }

    private static String readLoginPage() {
        try (InputStream in = LoginController.class.getResourceAsStream("/public/login.html")) {
            if (in == null) {
                throw new IllegalStateException("public/login.html not found on the classpath");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
