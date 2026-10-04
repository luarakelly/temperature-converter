package app;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TemperatureRecordDAOTest {

    @Test
    void testFindAll() throws Exception {
        TemperatureRecordDAO dao = new TemperatureRecordDAO();

        List<TemperatureRecord> records = dao.findAll();

        assertNotNull(records);
    }
}
