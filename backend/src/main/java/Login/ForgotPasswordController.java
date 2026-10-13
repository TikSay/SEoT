package Login;

import io.javalin.http.Context;

/** Tar imot glemt passord-forespørsler fra frontend (JSON) og sender dem videre til ForgotPasswordHandler. */
public class ForgotPasswordController {

    private final ForgotPasswordHandler handler;

    public ForgotPasswordController(ForgotPasswordHandler handler) {
        this.handler = handler;
    }

    /** POST /api/forgot-password. Svarer det samme uansett om e-posten finnes eller ikke. */
    public void forgotPassword(Context ctx) {
        ForgotPasswordRequest request;
        try {
            request = ctx.bodyAsClass(ForgotPasswordRequest.class);
        } catch (Exception e) {
            ctx.status(400).json(new LoginResult(false, "Invalid request."));
            return;
        }
        if (request == null) {
            ctx.status(400).json(new LoginResult(false, "Invalid request."));
            return;
        }

        try {
            handler.requestReset(request.getEmail());
            ctx.status(200).json(new LoginResult(true, "If the email is registered, a reset code has been sent."));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(new LoginResult(false, e.getMessage()));
        } catch (RuntimeException e) {
            System.err.println("Forgot password error: " + e.getMessage());
            ctx.status(500).json(new LoginResult(false, "Password reset is unavailable right now. Please try again later."));
        }
    }

    /** POST /api/reset-password */
    public void resetPassword(Context ctx) {
        ResetPasswordRequest request;
        try {
            request = ctx.bodyAsClass(ResetPasswordRequest.class);
        } catch (Exception e) {
            ctx.status(400).json(new LoginResult(false, "Invalid request."));
            return;
        }
        if (request == null) {
            ctx.status(400).json(new LoginResult(false, "Invalid request."));
            return;
        }

        try {
            handler.resetPassword(request.getEmail(), request.getCode(), request.getNewPassword());
            ctx.status(200).json(new LoginResult(true, "Password has been reset."));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(new LoginResult(false, e.getMessage()));
        } catch (RuntimeException e) {
            System.err.println("Reset password error: " + e.getMessage());
            ctx.status(500).json(new LoginResult(false, "Password reset is unavailable right now. Please try again later."));
        }
    }
}
