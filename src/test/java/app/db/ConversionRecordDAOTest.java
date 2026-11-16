package app.db;

import app.model.ConversionRecord;
import app.model.MeasurementUnit;

import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConversionRecordDAOTest {

    private ConversionRecordDAO dao;
    private MeasurementUnit celsius;
    private MeasurementUnit fahrenheit;
    private MeasurementUnit kilometre;
    private MeasurementUnit mile;

    @BeforeEach
    void setUp() throws SQLException {
        ConnectionProvider provider = TestDatabase.create();
        dao = new ConversionRecordDAO(provider);
        List<MeasurementUnit> units = new MeasurementUnitDAO(provider).findAll("en");
        celsius = units.get(0);
        fahrenheit = units.get(1);
        kilometre = units.get(2);
        mile = units.get(3);
    }

    @Test
    void savesAConversionAndReadsItBack() throws SQLException {
        dao.insert(new ConversionRecord(100, celsius, fahrenheit, 212, "fi"));

        List<ConversionRecord> records = dao.findLatest("en", 5);

        assertEquals(1, records.size());
        ConversionRecord record = records.get(0);
        assertTrue(record.getId() > 0);
        assertEquals(100.0, record.getInputValue());
        assertEquals(212.0, record.getResultValue());
        assertEquals("fi", record.getLanguageCode());
        assertEquals("celsius", record.getFromUnit().getUnitKey());
        assertEquals("Fahrenheit", record.getToUnit().getName());
        assertNotNull(record.getCreatedAt());
    }

    @Test
    void returnsOnlyTheLatestRecordsNewestFirst() throws SQLException {
        for (int value = 1; value <= 7; value++) {
            dao.insert(new ConversionRecord(value, kilometre, mile, value / 1.609344, "en"));
        }

        List<ConversionRecord> records = dao.findLatest("en", 5);

        assertEquals(5, records.size());
        assertEquals(7.0, records.get(0).getInputValue());
        assertEquals(3.0, records.get(4).getInputValue());
    }

    @Test
    void localizesUnitNamesWithEnglishFallback() throws SQLException {
        dao.insert(new ConversionRecord(32, fahrenheit, celsius, 0, "ar"));

        ConversionRecord record = dao.findLatest("ar", 5).get(0);

        assertEquals("Fahrenheit", record.getFromUnit().getName());
        assertEquals("درجة مئوية", record.getToUnit().getName());
    }

    @Test
    void returnsEmptyListWhenNothingIsSaved() throws SQLException {
        assertTrue(dao.findLatest("en", 5).isEmpty());
    }

    @Test
    void propagatesConnectionErrors() {
        ConversionRecordDAO failing = new ConversionRecordDAO(() -> {
            throw new SQLException("Connection failed");
        });
        ConversionRecord record = new ConversionRecord(1, celsius, fahrenheit, 33.8, "en");

        assertThrows(SQLException.class, () -> failing.insert(record));
        assertThrows(SQLException.class, () -> failing.findLatest("en", 5));
    }
}
