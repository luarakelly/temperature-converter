package app.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnitConverterTest {

    private static final double DELTA = 0.0001;

    private final UnitConverter converter = new UnitConverter();

    private final MeasurementUnit celsius = unit(1, "celsius", "temperature", 1);
    private final MeasurementUnit fahrenheit = unit(2, "fahrenheit", "temperature", 1);
    private final MeasurementUnit kelvin = unit(3, "kelvin", "temperature", 1);
    private final MeasurementUnit metrePerSecond = unit(4, "meter_per_second", "speed", 1);
    private final MeasurementUnit kilometrePerHour =
            unit(5, "kilometer_per_hour", "speed", 0.2777777777777778);
    private final MeasurementUnit metre = unit(7, "meter", "distance", 1);
    private final MeasurementUnit kilometre = unit(8, "kilometer", "distance", 1000);
    private final MeasurementUnit mile = unit(9, "mile", "distance", 1609.344);
    private final MeasurementUnit minute = unit(11, "minute", "time", 60);
    private final MeasurementUnit hour = unit(12, "hour", "time", 3600);

    @Test
    void convertsBetweenTemperatureUnits() {
        assertEquals(212.0, converter.convert(100, celsius, fahrenheit), DELTA);
        assertEquals(0.0, converter.convert(32, fahrenheit, celsius), DELTA);
        assertEquals(273.15, converter.convert(0, celsius, kelvin), DELTA);
        assertEquals(-459.67, converter.convert(0, kelvin, fahrenheit), DELTA);
        assertEquals(25.0, converter.convert(25, celsius, celsius), DELTA);
    }

    @Test
    void convertsFactorBasedUnits() {
        assertEquals(10.0, converter.convert(36, kilometrePerHour, metrePerSecond), DELTA);
        assertEquals(1.609344, converter.convert(1, mile, kilometre), DELTA);
        assertEquals(1500.0, converter.convert(1.5, kilometre, metre), DELTA);
        assertEquals(2.5, converter.convert(150, minute, hour), DELTA);
    }

    @Test
    void rejectsConversionBetweenCategories() {
        assertThrows(IllegalArgumentException.class,
                () -> converter.convert(1, celsius, metre));
    }

    @Test
    void rejectsNullUnits() {
        assertThrows(IllegalArgumentException.class, () -> converter.convert(1, null, metre));
        assertThrows(IllegalArgumentException.class, () -> converter.convert(1, metre, null));
    }

    @Test
    void rejectsUnknownTemperatureTarget() {
        MeasurementUnit rankine = unit(99, "rankine", "temperature", 1);

        assertThrows(IllegalArgumentException.class,
                () -> converter.convert(1, celsius, rankine));
    }

    @Test
    void validatesValues() {
        assertTrue(converter.isValidValue(-40, celsius));
        assertTrue(converter.isValidValue(0, kelvin));
        assertFalse(converter.isValidValue(-1, kelvin));
        assertTrue(converter.isValidValue(0, metre));
        assertFalse(converter.isValidValue(-1, metre));
        assertFalse(converter.isValidValue(Double.NaN, metre));
        assertFalse(converter.isValidValue(Double.POSITIVE_INFINITY, celsius));
        assertFalse(converter.isValidValue(1, null));
    }

    @Test
    void unitsKnowTheirCategory() {
        assertTrue(celsius.isSameCategory(kelvin));
        assertFalse(celsius.isSameCategory(metre));
        assertFalse(celsius.isSameCategory(null));
        assertEquals("celsius-name", celsius.toString());
    }

    @Test
    void recordCanBeRelocalized() {
        ConversionRecord record = new ConversionRecord(1, kilometre, mile, 0.6214, "en");
        MeasurementUnit japaneseKilometre =
                new MeasurementUnit(8, "kilometer", "distance", 1000, "キロメートル", "");

        ConversionRecord relocalized = record.withUnits(japaneseKilometre, mile);

        assertEquals("キロメートル", relocalized.getFromUnit().getName());
        assertEquals(0.6214, relocalized.getResultValue());
        assertEquals("en", relocalized.getLanguageCode());
        assertNull(relocalized.getCreatedAt());
    }

    private static MeasurementUnit unit(int id, String key, String category, double factor) {
        return new MeasurementUnit(id, key, category, factor, key + "-name", key + "-description");
    }
}
