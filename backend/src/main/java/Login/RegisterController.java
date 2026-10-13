package Login;

import io.javalin.http.Context;

/** Tar imot registreringer fra frontend (JSON) og sender dem videre til RegisterHandler. */
public class RegisterController {

    private final RegisterHandler handler;

    public RegisterController(RegisterHandler handler) {
        this.handler = handler;
    }

    /** POST /api/register */
    public void register(Context ctx) {
        RegisterRequest request;
        try {
            request = ctx.bodyAsClass(RegisterRequest.class);
        } catch (Exception e) {
            ctx.status(400).json(new LoginResult(false, "Invalid request."));
            return;
        }
        if (request == null) {
            ctx.status(400).json(new LoginResult(false, "Invalid request."));
            return;
        }

        try {
            handler.register(request.getFornavn(), request.getEtternavn(), request.getEmail(),
                    request.getPassword(), request.getLokallag());
            ctx.status(201).json(new LoginResult(true, "Registration successful."));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(new LoginResult(false, e.getMessage()));
        } catch (RuntimeException e) {
            System.err.println("Registration error: " + e.getMessage());
            ctx.status(500).json(new LoginResult(false, "Registration is unavailable right now. Please try again later."));
        }
    }
}
