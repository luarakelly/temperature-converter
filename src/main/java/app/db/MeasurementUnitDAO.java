package app.db;

import app.model.MeasurementUnit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MeasurementUnitDAO {

    public static final String DEFAULT_LANGUAGE = "en";

    private static final String FIND_ALL_SQL = """
            SELECT u.id,
                   u.unit_key,
                   u.category_code,
                   u.factor_to_base,
                   COALESCE(t.name, d.name, u.unit_key) AS name,
                   COALESCE(t.description, d.description, '') AS description
            FROM measurement_unit u
            LEFT JOIN unit_translation t
                   ON t.unit_id = u.id AND t.language_code = ?
            LEFT JOIN unit_translation d
                   ON d.unit_id = u.id AND d.language_code = ?
            ORDER BY u.id
            """;

    private final ConnectionProvider connectionProvider;

    public MeasurementUnitDAO() {
        this(DatabaseConnection::getConnection);
    }

    public MeasurementUnitDAO(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public List<MeasurementUnit> findAll(String languageCode) throws SQLException {
        List<MeasurementUnit> units = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL)) {
            statement.setString(1, languageCode);
            statement.setString(2, DEFAULT_LANGUAGE);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    units.add(new MeasurementUnit(
                            resultSet.getInt("id"),
                            resultSet.getString("unit_key"),
                            resultSet.getString("category_code"),
                            resultSet.getDouble("factor_to_base"),
                            resultSet.getString("name"),
                            resultSet.getString("description")));
                }
            }
        }
        return units;
    }
}
