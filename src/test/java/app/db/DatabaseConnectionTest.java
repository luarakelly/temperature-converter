package app.db;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConnectionTest {

    @Test
    void environmentVariableTakesPriority() {
        assertEquals(
                "env-value",
                DatabaseConnection.resolveConfig(
                        "env-value", "dotenv-value", "default-value"));
    }

    @Test
    void blankEnvironmentVariableUsesDotenvValue() {
        assertEquals(
                "dotenv-value",
                DatabaseConnection.resolveConfig(
                        "   ", "dotenv-value", "default-value"));
    }

    @Test
    void nullEnvironmentVariableUsesDotenvValue() {
        assertEquals(
                "dotenv-value",
                DatabaseConnection.resolveConfig(
                        null, "dotenv-value", "default-value"));
    }

    @Test
    void blankDotenvValueUsesDefault() {
        assertEquals(
                "default-value",
                DatabaseConnection.resolveConfig(
                        null, "   ", "default-value"));
    }

    @Test
    void nullDotenvValueUsesDefault() {
        assertEquals(
                "default-value",
                DatabaseConnection.resolveConfig(
                        null, null, "default-value"));
    }

    @Test
    void blankEnvironmentAndDotenvValuesUseDefault() {
        assertEquals(
                "default-value",
                DatabaseConnection.resolveConfig(
                        "", "", "default-value"));
    }

    @Test
    void configuredUrlIsMariaDbUrl() {
        assertTrue(
                DatabaseConnection.getUrl().startsWith("jdbc:mariadb://"));
    }

    @Test
    void configuredUsernameIsNotBlank() {
        assertNotNull(DatabaseConnection.getUsername());
        assertFalse(DatabaseConnection.getUsername().isBlank());
    }

    @Test
    void databaseConnectionCanBeOpened() throws SQLException {
        // Requires a running MariaDB database and valid credentials.
        try (Connection connection = DatabaseConnection.getConnection()) {
            assertNotNull(connection);
            assertFalse(connection.isClosed());
        }
    }
}
