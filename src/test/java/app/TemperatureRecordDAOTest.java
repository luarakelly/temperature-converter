package app;

import app.db.DatabaseConnection;
import app.db.TemperatureRecordDAO;
import app.model.TemperatureRecord;
import app.model.TemperatureUnit;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class TemperatureRecordDAOTest {

    @Test
    void insertExecutesStatementWithCorrectValues() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);

        when(connection.prepareStatement(anyString()))
                .thenReturn(statement);

        TemperatureUnit unit = new TemperatureUnit(1, "Celsius");
        TemperatureRecord record = new TemperatureRecord(
                25.0, 25.0, 77.0, 298.15, unit);

        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            db.when(DatabaseConnection::getConnection)
                    .thenReturn(connection);

            new TemperatureRecordDAO().insert(record);

            verify(statement).setDouble(1, 25.0);
            verify(statement).setDouble(2, 25.0);
            verify(statement).setDouble(3, 77.0);
            verify(statement).setDouble(4, 298.15);
            verify(statement).setInt(5, 1);
            verify(statement).executeUpdate();
            verify(statement).close();
            verify(connection).close();
        }
    }

    @Test
    void insertPropagatesConnectionException() throws Exception {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            db.when(DatabaseConnection::getConnection)
                    .thenThrow(new SQLException("Connection failed"));

            TemperatureRecordDAO dao = new TemperatureRecordDAO();
            TemperatureRecord record = new TemperatureRecord(
                    25.0, 25.0, 77.0, 298.15,
                    new TemperatureUnit(1, "Celsius"));

            assertThrows(SQLException.class, () -> dao.insert(record));
        }
    }

    @Test
    void insertPropagatesStatementException() throws Exception {
        Connection connection = mock(Connection.class);

        when(connection.prepareStatement(anyString()))
                .thenThrow(new SQLException("Statement failed"));

        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            db.when(DatabaseConnection::getConnection)
                    .thenReturn(connection);

            TemperatureRecord record = new TemperatureRecord(
                    25.0, 25.0, 77.0, 298.15,
                    new TemperatureUnit(1, "Celsius"));

            assertThrows(SQLException.class,
                    () -> new TemperatureRecordDAO().insert(record));

            verify(connection).close();
        }
    }

    @Test
    void insertPropagatesExecutionException() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);

        when(connection.prepareStatement(anyString()))
                .thenReturn(statement);

        doThrow(new SQLException("Insert failed"))
                .when(statement).executeUpdate();

        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            db.when(DatabaseConnection::getConnection)
                    .thenReturn(connection);

            TemperatureRecord record = new TemperatureRecord(
                    25.0, 25.0, 77.0, 298.15,
                    new TemperatureUnit(1, "Celsius"));

            assertThrows(SQLException.class,
                    () -> new TemperatureRecordDAO().insert(record));

            verify(statement).close();
            verify(connection).close();
        }
    }

    @Test
    void findAllReturnsEmptyListWhenNoRowsExist() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.prepareStatement(anyString()))
                .thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            db.when(DatabaseConnection::getConnection)
                    .thenReturn(connection);

            List<TemperatureRecord> records = new TemperatureRecordDAO().findAll();

            assertNotNull(records);
            assertTrue(records.isEmpty());

            verify(resultSet).next();
            verify(resultSet).close();
            verify(statement).close();
            verify(connection).close();
        }
    }

    @Test
    void findAllMapsDatabaseRowToRecord() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        LocalDateTime createdAt = LocalDateTime.of(
                2026, 1, 15, 10, 30);

        when(connection.prepareStatement(anyString()))
                .thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("id")).thenReturn(7);
        when(resultSet.getDouble("input_value")).thenReturn(25.0);
        when(resultSet.getDouble("celsius")).thenReturn(25.0);
        when(resultSet.getDouble("fahrenheit")).thenReturn(77.0);
        when(resultSet.getDouble("kelvin")).thenReturn(298.15);
        when(resultSet.getInt("unit_id")).thenReturn(1);
        when(resultSet.getString("unit_name")).thenReturn("Celsius");
        when(resultSet.getTimestamp("created_at"))
                .thenReturn(Timestamp.valueOf(createdAt));

        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            db.when(DatabaseConnection::getConnection)
                    .thenReturn(connection);

            List<TemperatureRecord> records = new TemperatureRecordDAO().findAll();

            assertEquals(1, records.size());

            TemperatureRecord record = records.get(0);
            assertEquals(7, record.getId());
            assertEquals(25.0, record.getInputValue());
            assertEquals(25.0, record.getCelsius());
            assertEquals(77.0, record.getFahrenheit());
            assertEquals(298.15, record.getKelvin());
            assertEquals(1, record.getInputUnit().getId());
            assertEquals("Celsius", record.getInputUnit().getUnitName());
            assertEquals(createdAt, record.getCreatedAt());
        }
    }

    @Test
    void findAllMapsMultipleRows() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.prepareStatement(anyString()))
                .thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);

        when(resultSet.getInt("id")).thenReturn(2, 1);
        when(resultSet.getDouble("input_value")).thenReturn(20.0, 10.0);
        when(resultSet.getDouble("celsius")).thenReturn(20.0, 10.0);
        when(resultSet.getDouble("fahrenheit")).thenReturn(68.0, 50.0);
        when(resultSet.getDouble("kelvin")).thenReturn(293.15, 283.15);
        when(resultSet.getInt("unit_id")).thenReturn(1);
        when(resultSet.getString("unit_name")).thenReturn("Celsius");
        when(resultSet.getTimestamp("created_at"))
                .thenReturn(Timestamp.valueOf(
                        LocalDateTime.of(2026, 1, 15, 10, 30)));

        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            db.when(DatabaseConnection::getConnection)
                    .thenReturn(connection);

            List<TemperatureRecord> records = new TemperatureRecordDAO().findAll();

            assertEquals(2, records.size());
            assertEquals(2, records.get(0).getId());
            assertEquals(1, records.get(1).getId());
        }
    }

    @Test
    void findAllPropagatesConnectionException() throws Exception {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            db.when(DatabaseConnection::getConnection)
                    .thenThrow(new SQLException("Connection failed"));

            assertThrows(SQLException.class,
                    () -> new TemperatureRecordDAO().findAll());
        }
    }

    @Test
    void findAllPropagatesQueryException() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);

        when(connection.prepareStatement(anyString()))
                .thenReturn(statement);
        when(statement.executeQuery())
                .thenThrow(new SQLException("Query failed"));

        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            db.when(DatabaseConnection::getConnection)
                    .thenReturn(connection);

            assertThrows(SQLException.class,
                    () -> new TemperatureRecordDAO().findAll());

            verify(statement).close();
            verify(connection).close();
        }
    }
}
