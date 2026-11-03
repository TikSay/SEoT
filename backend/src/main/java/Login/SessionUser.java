package Login;

import io.javalin.http.Context;
import jakarta.servlet.http.HttpSession;

/**
 * Holder styr på hvem som er logget inn.
 * Sesjonen på serveren lagrer bare bruker-ID-en. Selve brukeren hentes fra databasen hver gang,
 * så endringer i profilen vises med en gang, og slettede brukere blir logget ut.
 * Brukes av innloggingen nå, og kan brukes av f.eks. kurspåmelding og profil senere.
 */
public class SessionUser {

    private static final String SESSION_USER_ID = "userId";

    private final DataRegistry registry;

    public SessionUser(DataRegistry registry) {
        this.registry = registry;
    }

    /** Lager en ny sesjon for brukeren. */
    public void logIn(Context ctx, User user) {
        // Lag en ny sesjon, så en gammel sesjons-ID ikke kan gjenbrukes etter innlogging (session fixation)
        HttpSession oldSession = ctx.req().getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        HttpSession session = ctx.req().getSession(true);
        session.setAttribute(SESSION_USER_ID, user.getId());  // bare ID-en, aldri passord-hashen
    }

    /** Innlogget bruker med oppdaterte opplysninger fra databasen, eller null hvis ingen er logget inn. */
    public User current(Context ctx) {
        HttpSession session = ctx.req().getSession(false);   // false: lager ikke sesjon for besøkende som ikke er logget inn
        if (session == null || !(session.getAttribute(SESSION_USER_ID) instanceof Integer userId)) {
            return null;
        }
        User user = registry.findUserById(userId);
        if (user == null) {
            session.invalidate();  // brukeren er slettet, så sesjonen skal ikke gjelde lenger
        }
        return user;
    }

    /** Avslutter sesjonen hvis det finnes en. */
    public void logOut(Context ctx) {
        HttpSession session = ctx.req().getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
