package app;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final Dotenv DOTENV = Dotenv.configure()
            .ignoreIfMissing()
            .ignoreIfMalformed()
            .load();

    private static final String HOST = getConfig("DB_HOST", "localhost");

    private static final String PORT = getConfig("DB_PORT", "3306");

    private static final String DATABASE = getConfig("DB_NAME", "temperature_converter_db");

    private static final String USER = getConfig("DB_USER", "temperature_app");

    private static final String PASSWORD = getConfig("DB_PASSWORD", "temperature_app");

    private static final String URL = "jdbc:mariadb://" + HOST + ":" + PORT + "/" + DATABASE;

    private DatabaseConnection() {
    }

    private static String getConfig(String name, String defaultValue) {

        String value = DOTENV.get(name);

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                URL,
                USER,
                PASSWORD);
    }

    public static String getUrl() {
        return URL;
    }

    public static String getUsername() {
        return USER;
    }

}
