package Login;

import java.sql.*;
import java.time.Instant;

public class DatabaseRegistry implements DataRegistry {

    private final String url;
    private final String dbUser;
    private final String dbPassword;

    public DatabaseRegistry(String url, String dbUser, String dbPassword) {
        this.url = url;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    @Override
    public UserCredentials findUserByEmail(String email) {
        String sql = "SELECT id, username, email, fornavn, etternavn, lokallag, password_hash FROM users WHERE email = ?";

        try (Connection con = DriverManager.getConnection(url, dbUser, dbPassword);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("fornavn"),
                        rs.getString("etternavn"),
                        rs.getString("lokallag")
                );

                return new UserCredentials(
                        user,
                        rs.getString("password_hash")
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error during login lookup: " + e.getMessage(),
                    e
            );
        }

        return null;
    }

    @Override
    public void addUser(String fornavn, String etternavn, String email, String passwordHash, String lokallag) {
        String sql =
                "INSERT INTO users (fornavn, etternavn, email, password_hash, lokallag) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DriverManager.getConnection(url, dbUser, dbPassword);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, fornavn);
            ps.setString(2, etternavn);
            ps.setString(3, email);
            ps.setString(4, passwordHash);
            ps.setString(5, lokallag);

            ps.executeUpdate();

        } catch (SQLIntegrityConstraintViolationException e) {
            // MySQL svarer f.eks. "Duplicate entry '...' for key 'users.username'"
            if (e.getMessage().contains("users.username")) {
                throw new IllegalArgumentException("Username is already taken.");
            }
            throw new IllegalArgumentException("Email is already registered.");
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error during registration: " + e.getMessage(),
                    e
            );
        }
    }

    @Override
    public void saveResetCode(int userId, String codeHash, Instant expiresAt) {
        String sql =
                "UPDATE users SET reset_code_hash = ?, reset_code_expires_at = ?, reset_attempts = 0 WHERE id = ?";

        try (Connection con = DriverManager.getConnection(url, dbUser, dbPassword);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codeHash);
            ps.setTimestamp(2, Timestamp.from(expiresAt));
            ps.setInt(3, userId);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while saving reset code: " + e.getMessage(),
                    e
            );
        }
    }

    @Override
    public PasswordReset findPasswordReset(String email) {
        String sql =
                "SELECT id, reset_code_hash, reset_code_expires_at, reset_attempts FROM users WHERE email = ?";

        try (Connection con = DriverManager.getConnection(url, dbUser, dbPassword);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Timestamp expiresAt = rs.getTimestamp("reset_code_expires_at");

                return new PasswordReset(
                        rs.getInt("id"),
                        rs.getString("reset_code_hash"),
                        expiresAt == null ? null : expiresAt.toInstant(),
                        rs.getInt("reset_attempts")
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error during reset code lookup: " + e.getMessage(),
                    e
            );
        }

        return null;
    }

    @Override
    public void incrementResetAttempts(int userId) {
        String sql = "UPDATE users SET reset_attempts = reset_attempts + 1 WHERE id = ?";

        try (Connection con = DriverManager.getConnection(url, dbUser, dbPassword);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while counting reset attempt: " + e.getMessage(),
                    e
            );
        }
    }

    @Override
    public void updatePasswordAndClearReset(int userId, String newPasswordHash) {
        String sql =
                "UPDATE users SET password_hash = ?, reset_code_hash = NULL, reset_code_expires_at = NULL, "
                        + "reset_attempts = 0 WHERE id = ?";

        try (Connection con = DriverManager.getConnection(url, dbUser, dbPassword);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newPasswordHash);
            ps.setInt(2, userId);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while resetting password: " + e.getMessage(),
                    e
            );
        }
    }
}