package app;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TemperatureConverterTest {
        private final TemperatureConverter converter = new TemperatureConverter();
        private static final double DELTA = 0.001;

        @Test
        void fahrenheitToCelsiusHandlesCommonValues() {
                assertEquals(0.0, converter.fahrenheitToCelsius(32), DELTA);
                assertEquals(100.0, converter.fahrenheitToCelsius(212), DELTA);
                assertEquals(-40.0, converter.fahrenheitToCelsius(-40), DELTA);
                assertEquals(37.0, converter.fahrenheitToCelsius(98.6), DELTA);
                assertEquals(-17.7778, converter.fahrenheitToCelsius(0), 0.001);
        }

        @Test
        void celsiusToFahrenheitHandlesCommonValues() {
                assertEquals(32.0, converter.celsiusToFahrenheit(0), DELTA);
                assertEquals(212.0, converter.celsiusToFahrenheit(100), DELTA);
                assertEquals(-40.0, converter.celsiusToFahrenheit(-40), DELTA);
                assertEquals(77.0, converter.celsiusToFahrenheit(25), DELTA);
                assertEquals(122.0, converter.celsiusToFahrenheit(50), DELTA);
        }

        @Test
        void isExtremeTemperatureChecksLowerBoundary() {
                assertFalse(converter.isExtremeTemperature(-40));
                assertTrue(converter.isExtremeTemperature(-41));
                assertTrue(converter.isExtremeTemperature(-40.01));
                assertFalse(converter.isExtremeTemperature(-39.99));
        }

        @Test
        void isExtremeTemperatureChecksUpperBoundary() {
                assertFalse(converter.isExtremeTemperature(50));
                assertTrue(converter.isExtremeTemperature(51));
                assertTrue(converter.isExtremeTemperature(50.01));
                assertFalse(converter.isExtremeTemperature(49.99));
        }

        @Test
        void isExtremeTemperatureChecksNormalValues() {
                assertFalse(converter.isExtremeTemperature(0));
                assertFalse(converter.isExtremeTemperature(20));
                assertFalse(converter.isExtremeTemperature(25));
        }

        @Test
        void kelvinToCelsiusWorks() {
                assertEquals(26.85, converter.kelvinToCelsius(300), DELTA);
                assertEquals(0.0, converter.kelvinToCelsius(273.15), DELTA);
                assertEquals(-273.15, converter.kelvinToCelsius(0), DELTA);
        }

        @Test
        void celsiusToKelvinWorks() {
                assertEquals(273.15, converter.celsiusToKelvin(0), DELTA);
                assertEquals(373.15, converter.celsiusToKelvin(100), DELTA);
        }

        @Test
        void fahrenheitToKelvinWorks() {
                assertEquals(273.15, converter.fahrenheitToKelvin(32), DELTA);
                assertEquals(373.15, converter.fahrenheitToKelvin(212), DELTA);
        }

        @Test
        void kelvinToFahrenheitWorks() {
                assertEquals(32.0, converter.kelvinToFahrenheit(273.15), DELTA);
                assertEquals(212.0, converter.kelvinToFahrenheit(373.15), DELTA);
        }

        @Test
        void validKelvinValuesAreAccepted() {
                assertTrue(converter.isValidTemperature(0, "Kelvin"));
                assertTrue(converter.isValidTemperature(273.15, "Kelvin"));
                assertTrue(converter.isValidTemperature(100, "kelvin"));
                assertTrue(converter.isValidTemperature(100, "KELVIN"));
        }

        @Test
        void negativeKelvinValuesAreRejected() {
                assertFalse(converter.isValidTemperature(-1, "Kelvin"));
                assertFalse(converter.isValidTemperature(-273.15, "Kelvin"));
        }

        @Test
        void otherUnitsAllowNegativeValues() {
                assertTrue(converter.isValidTemperature(-100, "Celsius"));
                assertTrue(converter.isValidTemperature(-100, "Fahrenheit"));
        }

        @Test
        void nullUnitIsInvalid() {
                assertFalse(converter.isValidTemperature(10, null));
        }

        @Test
        void toCelsiusSupportsAllUnits() {
                assertEquals(25.0, converter.toCelsius(25, "Celsius"), DELTA);
                assertEquals(0.0, converter.toCelsius(32, "Fahrenheit"), DELTA);
                assertEquals(0.0, converter.toCelsius(273.15, "Kelvin"), DELTA);
        }

        @Test
        void toFahrenheitSupportsAllUnits() {
                assertEquals(77.0, converter.toFahrenheit(25, "Celsius"), DELTA);
                assertEquals(32.0, converter.toFahrenheit(32, "Fahrenheit"), DELTA);
                assertEquals(32.0, converter.toFahrenheit(273.15, "Kelvin"), DELTA);
        }

        @Test
        void toKelvinSupportsAllUnits() {
                assertEquals(298.15, converter.toKelvin(25, "Celsius"), DELTA);
                assertEquals(273.15, converter.toKelvin(32, "Fahrenheit"), DELTA);
                assertEquals(273.15, converter.toKelvin(273.15, "Kelvin"), DELTA);
        }

        @Test
        void unknownUnitsThrowIllegalArgumentException() {
                assertThrows(IllegalArgumentException.class, () -> converter.toCelsius(10, "Unknown"));
                assertThrows(IllegalArgumentException.class, () -> converter.toFahrenheit(10, "Unknown"));
                assertThrows(IllegalArgumentException.class, () -> converter.toKelvin(10, "Unknown"));
        }

        @Test
        void nullUnitsThrowIllegalArgumentExceptionInConversions() {
                assertThrows(IllegalArgumentException.class, () -> converter.toCelsius(10, null));
                assertThrows(IllegalArgumentException.class, () -> converter.toFahrenheit(10, null));
                assertThrows(IllegalArgumentException.class, () -> converter.toKelvin(10, null));
        }
}
