package Login;

/**
 * Sender reset-koden til brukeren.
 * I Sprint 1 bruker vi ConsoleResetCodeSender. Senere kan en ekte e-posttjeneste
 * implementere dette interfacet uten at ForgotPasswordHandler må endres.
 */
public interface ResetCodeSender {
    void send(String email, String code);
}
