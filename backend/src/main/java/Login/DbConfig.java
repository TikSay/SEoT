package Login;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Henter databaseinnstillingene. Først sjekkes miljøvariablene DB_URL, DB_USER og DB_PASSWORD,
 * finnes de ikke, leses db.properties i prosjektmappa.
 * db.properties ligger i .gitignore, så passordet havner ikke på GitHub.
 * Kopier db.properties.example for å komme i gang.
 */
public class DbConfig {

    private static final Path PROPERTIES_FILE = Path.of("db.properties");

    private final String url;
    private final String user;
    private final String password;

    private DbConfig(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public static DbConfig load() {
        Properties props = new Properties();
        if (Files.exists(PROPERTIES_FILE)) {
            try (InputStream in = Files.newInputStream(PROPERTIES_FILE)) {
                props.load(in);
            } catch (IOException e) {
                throw new RuntimeException("Could not read " + PROPERTIES_FILE + ": " + e.getMessage(), e);
            }
        }
        return new DbConfig(
                get("DB_URL", "db.url", props),
                get("DB_USER", "db.user", props),
                get("DB_PASSWORD", "db.password", props));
    }

    private static String get(String envName, String propName, Properties props) {
        String value = System.getenv(envName);
        if (value == null || value.isBlank()) {
            value = props.getProperty(propName);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing database setting: set environment variable "
                    + envName + " or " + propName + " in " + PROPERTIES_FILE);
        }
        return value;
    }

    public DatabaseRegistry createRegistry() {
        return new DatabaseRegistry(url, user, password);
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
