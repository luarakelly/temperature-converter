package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TemperatureRecordDAO {

    public void insert(
            TemperatureRecord record)
            throws Exception {

        String sql = """
                INSERT INTO temperature_record
                (
                    input_value,
                    celsius,
                    fahrenheit,
                    kelvin,
                    input_unit_id
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDouble(
                    1,
                    record.getInputValue());

            statement.setDouble(
                    2,
                    record.getCelsius());

            statement.setDouble(
                    3,
                    record.getFahrenheit());

            statement.setDouble(
                    4,
                    record.getKelvin());

            statement.setInt(
                    5,
                    record.getInputUnit().getId());

            statement.executeUpdate();
        }
    }

    public List<TemperatureRecord> findAll()
            throws Exception {

        String sql = """
                SELECT
                    r.id,
                    r.input_value,
                    r.celsius,
                    r.fahrenheit,
                    r.kelvin,
                    r.created_at,
                    u.id AS unit_id,
                    u.unit_name
                FROM temperature_record r
                INNER JOIN temperature_unit u
                    ON r.input_unit_id = u.id
                ORDER BY r.id DESC
                """;

        List<TemperatureRecord> records = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(sql);

                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                TemperatureUnit unit = new TemperatureUnit(
                        resultSet.getInt(
                                "unit_id"),
                        resultSet.getString(
                                "unit_name"));

                TemperatureRecord record = new TemperatureRecord(
                        resultSet.getInt("id"),
                        resultSet.getDouble(
                                "input_value"),
                        resultSet.getDouble(
                                "celsius"),
                        resultSet.getDouble(
                                "fahrenheit"),
                        resultSet.getDouble(
                                "kelvin"),
                        unit,
                        resultSet
                                .getTimestamp(
                                        "created_at")
                                .toLocalDateTime());

                records.add(record);
            }
        }

        return records;
    }

}