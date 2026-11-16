package app.db;

import app.model.ConversionRecord;
import app.model.MeasurementUnit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConversionRecordDAO {

    private static final String INSERT_SQL = """
            INSERT INTO conversion_record
                (input_value, from_unit_id, to_unit_id, result_value, language_code)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String FIND_LATEST_SQL = """
            SELECT r.id,
                   r.input_value,
                   r.result_value,
                   r.language_code,
                   r.created_at,
                   fu.id AS from_id,
                   fu.unit_key AS from_key,
                   fu.category_code AS from_category,
                   fu.factor_to_base AS from_factor,
                   COALESCE(ft.name, fd.name, fu.unit_key) AS from_name,
                   COALESCE(ft.description, fd.description, '') AS from_description,
                   tu.id AS to_id,
                   tu.unit_key AS to_key,
                   tu.category_code AS to_category,
                   tu.factor_to_base AS to_factor,
                   COALESCE(tt.name, td.name, tu.unit_key) AS to_name,
                   COALESCE(tt.description, td.description, '') AS to_description
            FROM conversion_record r
            JOIN measurement_unit fu ON fu.id = r.from_unit_id
            JOIN measurement_unit tu ON tu.id = r.to_unit_id
            LEFT JOIN unit_translation ft
                   ON ft.unit_id = fu.id AND ft.language_code = ?
            LEFT JOIN unit_translation fd
                   ON fd.unit_id = fu.id AND fd.language_code = ?
            LEFT JOIN unit_translation tt
                   ON tt.unit_id = tu.id AND tt.language_code = ?
            LEFT JOIN unit_translation td
                   ON td.unit_id = tu.id AND td.language_code = ?
            ORDER BY r.created_at DESC, r.id DESC
            LIMIT ?
            """;

    private final ConnectionProvider connectionProvider;

    public ConversionRecordDAO() {
        this(DatabaseConnection::getConnection);
    }

    public ConversionRecordDAO(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public void insert(ConversionRecord record) throws SQLException {
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setDouble(1, record.getInputValue());
            statement.setInt(2, record.getFromUnit().getId());
            statement.setInt(3, record.getToUnit().getId());
            statement.setDouble(4, record.getResultValue());
            statement.setString(5, record.getLanguageCode());
            statement.executeUpdate();
        }
    }

    public List<ConversionRecord> findLatest(String languageCode, int limit) throws SQLException {
        List<ConversionRecord> records = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_LATEST_SQL)) {
            statement.setString(1, languageCode);
            statement.setString(2, MeasurementUnitDAO.DEFAULT_LANGUAGE);
            statement.setString(3, languageCode);
            statement.setString(4, MeasurementUnitDAO.DEFAULT_LANGUAGE);
            statement.setInt(5, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    records.add(mapRecord(resultSet));
                }
            }
        }
        return records;
    }

    private ConversionRecord mapRecord(ResultSet resultSet) throws SQLException {
        return new ConversionRecord(
                resultSet.getLong("id"),
                resultSet.getDouble("input_value"),
                mapUnit(resultSet, "from"),
                mapUnit(resultSet, "to"),
                resultSet.getDouble("result_value"),
                resultSet.getString("language_code"),
                resultSet.getTimestamp("created_at").toLocalDateTime());
    }

    private MeasurementUnit mapUnit(ResultSet resultSet, String prefix) throws SQLException {
        return new MeasurementUnit(
                resultSet.getInt(prefix + "_id"),
                resultSet.getString(prefix + "_key"),
                resultSet.getString(prefix + "_category"),
                resultSet.getDouble(prefix + "_factor"),
                resultSet.getString(prefix + "_name"),
                resultSet.getString(prefix + "_description"));
    }
}
