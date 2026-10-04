package app;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TemperatureConverterTest {

        private final TemperatureConverter converter = new TemperatureConverter();

        @Test
        void testFahrenheitToCelsius() {
                assertEquals(
                                0.0,
                                converter.fahrenheitToCelsius(32),
                                0.001);

                assertEquals(
                                100.0,
                                converter.fahrenheitToCelsius(212),
                                0.001);

                assertEquals(
                                -40.0,
                                converter.fahrenheitToCelsius(-40),
                                0.001);
        }

        @Test
        void testCelsiusToFahrenheit() {
                assertEquals(
                                32.0,
                                converter.celsiusToFahrenheit(0),
                                0.001);

                assertEquals(
                                212.0,
                                converter.celsiusToFahrenheit(100),
                                0.001);

                assertEquals(
                                -40.0,
                                converter.celsiusToFahrenheit(-40),
                                0.001);
        }

        @Test
        void testKelvinToCelsius() {
                assertEquals(
                                26.85,
                                converter.kelvinToCelsius(300),
                                0.001);

                assertEquals(
                                0.0,
                                converter.kelvinToCelsius(273.15),
                                0.001);
        }

        @Test
        void testCelsiusToKelvin() {
                assertEquals(
                                273.15,
                                converter.celsiusToKelvin(0),
                                0.001);

                assertEquals(
                                373.15,
                                converter.celsiusToKelvin(100),
                                0.001);
        }

        @Test
        void testFahrenheitToKelvin() {
                assertEquals(
                                273.15,
                                converter.fahrenheitToKelvin(32),
                                0.001);
        }

        @Test
        void testKelvinToFahrenheit() {
                assertEquals(
                                32.0,
                                converter.kelvinToFahrenheit(273.15),
                                0.001);
        }

        @Test
        void testIsExtremeTemperature() {
                assertFalse(
                                converter.isExtremeTemperature(-40));

                assertFalse(
                                converter.isExtremeTemperature(50));

                assertTrue(
                                converter.isExtremeTemperature(-41));

                assertTrue(
                                converter.isExtremeTemperature(51));

                assertFalse(
                                converter.isExtremeTemperature(20));
        }

        @Test
        void testValidKelvin() {
                assertTrue(
                                converter.isValidTemperature(
                                                0,
                                                "Kelvin"));

                assertTrue(
                                converter.isValidTemperature(
                                                273.15,
                                                "Kelvin"));
        }

        @Test
        void testInvalidKelvin() {
                assertFalse(
                                converter.isValidTemperature(
                                                -1,
                                                "Kelvin"));
        }

        @Test
        void testValidCelsius() {
                assertTrue(
                                converter.isValidTemperature(
                                                -100,
                                                "Celsius"));
        }

        @Test
        void testValidFahrenheit() {
                assertTrue(
                                converter.isValidTemperature(
                                                -100,
                                                "Fahrenheit"));
        }

        @Test
        void testNullUnitIsInvalid() {
                assertFalse(
                                converter.isValidTemperature(
                                                10,
                                                null));
        }

        @Test
        void testToCelsius() {
                assertEquals(
                                0.0,
                                converter.toCelsius(
                                                32,
                                                "Fahrenheit"),
                                0.001);

                assertEquals(
                                0.0,
                                converter.toCelsius(
                                                273.15,
                                                "Kelvin"),
                                0.001);
        }

        @Test
        void testToFahrenheit() {
                assertEquals(
                                32.0,
                                converter.toFahrenheit(
                                                0,
                                                "Celsius"),
                                0.001);
        }

        @Test
        void testToKelvin() {
                assertEquals(
                                273.15,
                                converter.toKelvin(
                                                0,
                                                "Celsius"),
                                0.001);
        }

        @Test
        void testUnknownUnitThrowsException() {
                assertThrows(
                                IllegalArgumentException.class,
                                () -> converter.toCelsius(
                                                10,
                                                "Unknown"));
        }
}
