package app;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TemperatureUnitTest {

        @Test
        void testConstructor() {
                TemperatureUnit unit = new TemperatureUnit(1, "Celsius");

                assertEquals(1, unit.getId());
                assertEquals("Celsius", unit.getUnitName());
        }

        @Test
        void testSetters() {
                TemperatureUnit unit = new TemperatureUnit("Celsius");

                unit.setId(10);
                unit.setUnitName("Kelvin");

                assertEquals(10, unit.getId());
                assertEquals("Kelvin", unit.getUnitName());
        }

        @Test
        void testToString() {
                TemperatureUnit unit = new TemperatureUnit(1, "Fahrenheit");

                assertEquals("Fahrenheit", unit.toString());
        }
}
