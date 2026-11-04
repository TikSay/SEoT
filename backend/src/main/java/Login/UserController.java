package Login;

import io.javalin.http.Context;
import java.util.Set;

public class UserController {

    private final DataRegistry registry;
    private final SessionUser sessionUser;
    private static final Set<String> LOKALLAG = Set.of(
            "fredrikstad husflidslag", "halden husflidslag", "hobøl husflidslag",
            "indre østfold husflidslag", "moss husflidslag", "rygge husflidslag",
            "råde husflidslag", "sarpsborg husflidslag");

    public UserController(DataRegistry registry, SessionUser sessionUser) {
        this.registry = registry;
        this.sessionUser = sessionUser;
    }

    public void updateProfile(Context ctx) {
        User sessionUserObj = sessionUser.current(ctx);
        if (sessionUserObj == null) {
            ctx.status(401).json(java.util.Map.of("success", false, "message", "Ikke logget inn"));
            return;
        }

        UserUpdateRequest req = ctx.bodyAsClass(UserUpdateRequest.class);

        if (req.getFornavn() == null || req.getFornavn().isBlank() ||
                req.getEtternavn() == null || req.getEtternavn().isBlank() ||
                req.getEmail() == null || req.getEmail().isBlank()) {
            ctx.status(400).json(java.util.Map.of("success", false, "message", "Felt kan ikke være tomme"));
            return;
        }

        if (!LOKALLAG.contains(req.getLokallag())) {
            ctx.status(400).json(java.util.Map.of("success", false, "message", "Ugyldig lokallag"));
            return;
        }

        try {
            // Oppdater i databasen ved hjelp av ID-en til den innloggede brukeren
            registry.updateUser(
                    sessionUserObj.getId(),
                    req.getFornavn().trim(),
                    req.getEtternavn().trim(),
                    req.getEmail().trim().toLowerCase(),
                    req.getLokallag()
            );

            // Hent oppdatert bruker fra databasen
            User updatedUser = registry.findUserById(sessionUserObj.getId());

            // Oppdater sesjonen slik at den nye informasjonen lagres der også
            sessionUser.logIn(ctx, updatedUser);

            ctx.json(java.util.Map.of("success", true, "user", updatedUser));
        } catch (Exception e) {
            ctx.status(500).json(java.util.Map.of("success", false, "message", "Feil: " + e.getMessage()));
        }
    }
}