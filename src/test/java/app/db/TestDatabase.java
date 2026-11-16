package app.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

final class TestDatabase {

    private TestDatabase() {
    }

    static ConnectionProvider create() {
        String url = "jdbc:h2:mem:" + UUID.randomUUID()
                + ";MODE=MariaDB;DB_CLOSE_DELAY=-1"
                + ";INIT=RUNSCRIPT FROM 'classpath:test-db.sql' CHARSET 'UTF-8'";
        try (Connection ignored = DriverManager.getConnection(url)) {
            String reuseUrl = url.substring(0, url.indexOf(";INIT"));
            return () -> DriverManager.getConnection(reuseUrl);
        } catch (SQLException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
