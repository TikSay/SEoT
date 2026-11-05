package Login;

import io.javalin.http.Context;

/** PUT /api/user/update: den innloggede brukeren endrer navn, e-post og lokallag (S-017). */
public class UserController {

    private static final int MAX_NAME_LENGTH = 50;   // samme som users.fornavn / users.etternavn VARCHAR(50)

    private final DataRegistry registry;
    private final SessionUser sessionUser;

    public UserController(DataRegistry registry, SessionUser sessionUser) {
        this.registry = registry;
        this.sessionUser = sessionUser;
    }

    public void updateProfile(Context ctx) {
        // S-007: tekniske detaljer logges, brukeren får en generell melding
        try {
            User current = sessionUser.current(ctx);
            if (current == null) {
                ctx.status(401).json(new LoginResult(false, "Not logged in."));
                return;
            }

            UserUpdateRequest req;
            try {
                req = ctx.bodyAsClass(UserUpdateRequest.class);
            } catch (Exception e) {
                ctx.status(400).json(new LoginResult(false, "Invalid request."));
                return;
            }
            if (req == null) {
                ctx.status(400).json(new LoginResult(false, "Invalid request."));
                return;
            }

            String feil = validate(req);
            if (feil != null) {
                ctx.status(400).json(new LoginResult(false, feil));
                return;
            }

            String email = req.getEmail().trim().toLowerCase();

            // E-posten kan ikke tas fra en annen bruker (users.email er UNIQUE)
            UserCredentials eier = registry.findUserByEmail(email);
            if (eier != null && eier.getUser().getId() != current.getId()) {
                ctx.status(409).json(new LoginResult(false, "Email is already registered."));
                return;
            }

            registry.updateUser(current.getId(), req.getFornavn().trim(), req.getEtternavn().trim(),
                    email, req.getLokallag());

            // Hent oppdatert bruker fra databasen og oppdater sesjonen, så /api/me gir de nye opplysningene
            User updatedUser = registry.findUserById(current.getId());
            sessionUser.logIn(ctx, updatedUser);

            ctx.json(new LoginResult(true, "Profile updated.", updatedUser));
        } catch (RuntimeException e) {
            System.err.println("Profile update error: " + e.getMessage());
            ctx.status(500).json(new LoginResult(false, "Profile update is unavailable right now. Please try again later."));
        }
    }

    /** Samme regler som ved registrering (S-006). Returnerer feilmeldingen, eller null hvis alt er i orden. */
    private static String validate(UserUpdateRequest req) {
        if (isBlank(req.getFornavn()) || isBlank(req.getEtternavn()) || isBlank(req.getEmail())) {
            return "First name, last name and email are required.";
        }
        if (req.getFornavn().trim().length() > MAX_NAME_LENGTH || req.getEtternavn().trim().length() > MAX_NAME_LENGTH) {
            return "First and last name cannot be longer than 50 characters.";
        }
        try {
            InputValidator.requireValidEmail(req.getEmail());
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
        if (req.getLokallag() == null || !RegisterHandler.LOKALLAG.contains(req.getLokallag())) {
            return "Please choose a valid local chapter.";
        }
        return null;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
