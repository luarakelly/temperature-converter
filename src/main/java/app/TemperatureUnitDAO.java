package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TemperatureUnitDAO {

        public List<TemperatureUnit> findAll()
                        throws Exception {

                String sql = """
                                SELECT id, unit_name
                                FROM temperature_unit
                                ORDER BY id
                                """;

                List<TemperatureUnit> units = new ArrayList<>();

                try (
                                Connection connection = DatabaseConnection.getConnection();

                                PreparedStatement statement = connection.prepareStatement(sql);

                                ResultSet resultSet = statement.executeQuery()) {

                        while (resultSet.next()) {

                                units.add(
                                                new TemperatureUnit(
                                                                resultSet.getInt("id"),
                                                                resultSet.getString(
                                                                                "unit_name")));
                        }
                }

                return units;
        }

}