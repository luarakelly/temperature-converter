package app.db;

import app.model.MeasurementUnit;

import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MeasurementUnitDAOTest {

    private MeasurementUnitDAO dao;

    @BeforeEach
    void setUp() {
        dao = new MeasurementUnitDAO(TestDatabase.create());
    }

    @Test
    void returnsAllUnitsWithTheirLookupData() throws SQLException {
        List<MeasurementUnit> units = dao.findAll("en");

        assertEquals(4, units.size());
        MeasurementUnit kilometre = units.get(2);
        assertEquals(3, kilometre.getId());
        assertEquals("kilometer", kilometre.getUnitKey());
        assertEquals("distance", kilometre.getCategoryCode());
        assertEquals(1000.0, kilometre.getFactorToBase());
        assertEquals("kilometre", kilometre.getName());
        assertEquals("One thousand metres.", kilometre.getDescription());
    }

    @Test
    void returnsNamesAndDescriptionsInTheSelectedLanguage() throws SQLException {
        List<MeasurementUnit> units = dao.findAll("ja");

        assertEquals("摂氏", units.get(0).getName());
        assertEquals("水を基準とする温度単位です。", units.get(0).getDescription());
        assertEquals("キロメートル", units.get(2).getName());
    }

    @Test
    void returnsArabicTextUnchanged() throws SQLException {
        assertEquals("درجة مئوية", dao.findAll("ar").get(0).getName());
    }

    @Test
    void fallsBackToEnglishWhenTranslationIsMissing() throws SQLException {
        List<MeasurementUnit> units = dao.findAll("ar");

        assertEquals("Fahrenheit", units.get(1).getName());
        assertEquals("Temperature scale used in the United States.", units.get(1).getDescription());
        assertEquals("kilometre", units.get(2).getName());
    }

    @Test
    void fallsBackToEnglishForAnUnknownLanguage() throws SQLException {
        assertEquals("Celsius", dao.findAll("xx").get(0).getName());
    }

    @Test
    void fallsBackToUnitKeyWhenNoTranslationExists() throws SQLException {
        MeasurementUnit mile = dao.findAll("fi").get(3);

        assertEquals("mile", mile.getName());
        assertEquals("", mile.getDescription());
    }

    @Test
    void propagatesConnectionErrors() {
        MeasurementUnitDAO failing = new MeasurementUnitDAO(() -> {
            throw new SQLException("Connection failed");
        });

        assertThrows(SQLException.class, () -> failing.findAll("en"));
    }
}
