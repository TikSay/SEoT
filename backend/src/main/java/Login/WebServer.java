package Login;

import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.type.LogicalType;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinJackson;
import org.eclipse.jetty.ee10.servlet.SessionHandler;
import org.eclipse.jetty.http.HttpCookie;

import java.time.Clock;

/**
 * Starter backend på http://localhost:7070.
 * Den gamle innloggingssiden ligger på /login.html (fra src/main/resources/public).
 * Sett miljøvariabelen FRONTEND_ORIGIN (f.eks. http://127.0.0.1:5500) hvis frontend
 * kjører på en annen adresse og skal få kalle API-et fra nettleseren.
 */
public class WebServer {

    private static final int PORT = 7070;

    static void main() {
        LoginHandler handler;
        SessionUser sessionUser;
        RegisterHandler registerHandler;
        ForgotPasswordHandler forgotPasswordHandler;
        try {
            DataRegistry registry = DbConfig.load().createRegistry();
            handler = new LoginHandler(registry);
            sessionUser = new SessionUser(registry);
            registerHandler = new RegisterHandler(registry);
            // Sprint 1: reset-koden skrives i konsollen. Her bytter vi til ekte e-post senere.
            forgotPasswordHandler = new ForgotPasswordHandler(registry, new ConsoleResetCodeSender(), Clock.systemUTC());
        } catch (IllegalStateException e) {
            IO.println("Configuration error: " + e.getMessage());
            return;
        }
        LoginController controller = new LoginController(handler, sessionUser);
        RegisterController registerController = new RegisterController(registerHandler);
        ForgotPasswordController forgotPasswordController = new ForgotPasswordController(forgotPasswordHandler);
        String frontendOrigin = System.getenv("FRONTEND_ORIGIN");

        Javalin.create(config -> configure(config, controller, registerController,
                forgotPasswordController, frontendOrigin)).start();

        IO.println("Login backend running on http://localhost:" + PORT);
        if (frontendOrigin == null || frontendOrigin.isBlank()) {
            IO.println("CORS disabled: set FRONTEND_ORIGIN to allow a browser frontend.");
        }
    }

    /** Oppsett av Javalin: port, statiske filer, sesjoner, CORS og ruter. */
    static void configure(JavalinConfig config, LoginController controller,
                          RegisterController registerController,
                          ForgotPasswordController forgotPasswordController, String frontendOrigin) {
        config.jetty.port = PORT;
        config.jsonMapper(createJsonMapper());
        config.staticFiles.add(staticFiles -> {
            staticFiles.hostedPath = "/";
            staticFiles.directory = "/public";
            staticFiles.location = Location.CLASSPATH;
        });
        config.jetty.modifyServletContextHandler(handler -> handler.setSessionHandler(createSessionHandler()));
        if (frontendOrigin != null && !frontendOrigin.isBlank()) {
            config.bundledPlugins.enableCors(cors -> cors.addRule(rule -> rule.allowHost(frontendOrigin)));
        }
        config.routes.post("/api/login", controller::login);
        config.routes.get("/api/me", controller::me);
        config.routes.post("/api/logout", controller::apiLogout);
        config.routes.post("/api/register", registerController::register);
        config.routes.post("/api/forgot-password", forgotPasswordController::forgotPassword);
        config.routes.post("/api/reset-password", forgotPasswordController::resetPassword);
        config.routes.post("/login", controller::loginForm);
        config.routes.get("/account", controller::account);
        config.routes.post("/logout", controller::logout);

        // S-007: sikkerhetsnett for feil som ingen controller fanger. Detaljene logges, brukeren får en generell melding.
        config.routes.exception(Exception.class, (e, ctx) -> {
            System.err.println("Unexpected error on " + ctx.method() + " " + ctx.path() + ": " + e);
            ctx.status(500).json(new LoginResult(false, "Something went wrong. Please try again later."));
        });
        // S-007: ukjente /api-ruter svarer med JSON, så React alltid kan lese svaret med response.json()
        config.routes.error(404, "*", ctx -> {
            if (ctx.path().startsWith("/api/")) {
                ctx.json(new LoginResult(false, "Not found."));
            }
        });
    }

    /**
     * S-006: Javalins standard JSON-oppsett, men tall og true/false godtas ikke lenger i tekstfelt
     * (f.eks. {"password": 12345678}). Da kaster bodyAsClass, og controllerne svarer 400 "Invalid request.".
     * Gjelder bare felter av typen String. Felter som int og boolean oppfører seg som før.
     */
    private static JavalinJackson createJsonMapper() {
        return new JavalinJackson().updateMapper(mapper -> mapper.coercionConfigFor(LogicalType.Textual)
                .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail));
    }

    /**
     * Innstillinger for sesjonscookien i Sprint 1-demoen (vanlig HTTP på localhost).
     * SameSite=Lax beskytter bare delvis mot CSRF.
     */
    private static SessionHandler createSessionHandler() {
        SessionHandler sessionHandler = new SessionHandler();
        sessionHandler.setHttpOnly(true);                     // JavaScript får ikke lese sesjonscookien
        sessionHandler.setSameSite(HttpCookie.SameSite.LAX);  // stopper de fleste skjemaer sendt fra andre nettsider (delvis CSRF-beskyttelse)
        sessionHandler.setMaxInactiveInterval(30 * 60);       // sesjonen utløper etter 30 min uten aktivitet
        // setSecureRequestOnly(true) når siden kjører på HTTPS
        return sessionHandler;
    }
}
