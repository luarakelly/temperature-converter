package app;

public class TemperatureConverter {

    public double fahrenheitToCelsius(double fahrenheit) {
        return (fahrenheit - 32) * 5 / 9;
    }

    public double celsiusToFahrenheit(double celsius) {
        return (celsius * 9 / 5) + 32;
    }

    public double kelvinToCelsius(double kelvin) {
        return kelvin - 273.15;
    }

    public double celsiusToKelvin(double celsius) {
        return celsius + 273.15;
    }

    public double fahrenheitToKelvin(double fahrenheit) {
        return celsiusToKelvin(
                fahrenheitToCelsius(fahrenheit));
    }

    public double kelvinToFahrenheit(double kelvin) {
        return celsiusToFahrenheit(
                kelvinToCelsius(kelvin));
    }

    public boolean isExtremeTemperature(double celsius) {
        return celsius < -40 || celsius > 50;
    }

    public boolean isValidTemperature(
            double value,
            String unit) {

        if (unit == null) {
            return false;
        }

        if (unit.equalsIgnoreCase("Kelvin")) {
            return value >= 0;
        }

        return true;
    }

    public double toCelsius(
            double value,
            String unit) {

        return switch (unit.toLowerCase()) {

            case "celsius" -> value;

            case "fahrenheit" ->
                fahrenheitToCelsius(value);

            case "kelvin" ->
                kelvinToCelsius(value);

            default ->
                throw new IllegalArgumentException(
                        "Unknown temperature unit");
        };
    }

    public double toFahrenheit(
            double value,
            String unit) {

        return switch (unit.toLowerCase()) {

            case "celsius" ->
                celsiusToFahrenheit(value);

            case "fahrenheit" ->
                value;

            case "kelvin" ->
                kelvinToFahrenheit(value);

            default ->
                throw new IllegalArgumentException(
                        "Unknown temperature unit");
        };
    }

    public double toKelvin(
            double value,
            String unit) {

        return switch (unit.toLowerCase()) {

            case "celsius" ->
                celsiusToKelvin(value);

            case "fahrenheit" ->
                fahrenheitToKelvin(value);

            case "kelvin" ->
                value;

            default ->
                throw new IllegalArgumentException(
                        "Unknown temperature unit");
        };
    }
}