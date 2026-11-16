package app.model;

public class UnitConverter {

    public static final String TEMPERATURE = "temperature";

    private final TemperatureConverter temperatureConverter;

    public UnitConverter() {
        this(new TemperatureConverter());
    }

    public UnitConverter(TemperatureConverter temperatureConverter) {
        this.temperatureConverter = temperatureConverter;
    }

    public boolean isValidValue(double value, MeasurementUnit unit) {
        if (unit == null || !Double.isFinite(value)) {
            return false;
        }
        if (TEMPERATURE.equals(unit.getCategoryCode())) {
            return temperatureConverter.isValidTemperature(value, unit.getUnitKey());
        }
        return value >= 0;
    }

    public double convert(double value, MeasurementUnit from, MeasurementUnit to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Units cannot be null");
        }
        if (!from.isSameCategory(to)) {
            throw new IllegalArgumentException(
                    "Cannot convert " + from.getUnitKey() + " to " + to.getUnitKey());
        }
        if (TEMPERATURE.equals(from.getCategoryCode())) {
            return convertTemperature(value, from.getUnitKey(), to.getUnitKey());
        }
        return value * from.getFactorToBase() / to.getFactorToBase();
    }

    private double convertTemperature(double value, String fromKey, String toKey) {
        double celsius = temperatureConverter.toCelsius(value, fromKey);
        return switch (toKey) {
            case "celsius" -> celsius;
            case "fahrenheit" -> temperatureConverter.celsiusToFahrenheit(celsius);
            case "kelvin" -> temperatureConverter.celsiusToKelvin(celsius);
            default -> throw new IllegalArgumentException("Unknown temperature unit: " + toKey);
        };
    }
}
