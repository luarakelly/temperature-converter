package app;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TemperatureUnitDAOTest {

    @Test
    void testFindAll() throws Exception {
        TemperatureUnitDAO dao = new TemperatureUnitDAO();

        List<TemperatureUnit> units = dao.findAll();

        assertNotNull(units);
        assertTrue(units.size() >= 3);
    }
}
