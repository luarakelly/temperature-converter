package app;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TemperatureRecordTest {

        @Test
        void testRecordConstructor() {

                TemperatureUnit unit = new TemperatureUnit(
                                1,
                                "Celsius");

                TemperatureRecord record = new TemperatureRecord(
                                25.0,
                                25.0,
                                77.0,
                                298.15,
                                unit);

                assertEquals(0, record.getId());
                assertEquals(25.0, record.getInputValue());
                assertEquals(25.0, record.getCelsius());
                assertEquals(77.0, record.getFahrenheit());
                assertEquals(298.15, record.getKelvin());

                assertSame(unit, record.getInputUnit());

                assertNull(record.getCreatedAt());
        }

        @Test
        void testFullRecordConstructor() {

                TemperatureUnit unit = new TemperatureUnit(
                                2,
                                "Fahrenheit");

                TemperatureRecord record = new TemperatureRecord(
                                5,
                                32.0,
                                0.0,
                                32.0,
                                273.15,
                                unit,
                                null);

                assertEquals(5, record.getId());
                assertEquals(32.0, record.getFahrenheit());

                assertEquals(
                                "Fahrenheit",
                                record.getInputUnit().getUnitName());
        }
}
