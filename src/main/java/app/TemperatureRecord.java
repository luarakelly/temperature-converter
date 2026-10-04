package app;

import java.time.LocalDateTime;

public class TemperatureRecord {

    private int id;
    private double inputValue;
    private double celsius;
    private double fahrenheit;
    private double kelvin;
    private TemperatureUnit inputUnit;
    private LocalDateTime createdAt;

    public TemperatureRecord(
            int id,
            double inputValue,
            double celsius,
            double fahrenheit,
            double kelvin,
            TemperatureUnit inputUnit,
            LocalDateTime createdAt) {

        this.id = id;
        this.inputValue = inputValue;
        this.celsius = celsius;
        this.fahrenheit = fahrenheit;
        this.kelvin = kelvin;
        this.inputUnit = inputUnit;
        this.createdAt = createdAt;
    }

    public TemperatureRecord(
            double inputValue,
            double celsius,
            double fahrenheit,
            double kelvin,
            TemperatureUnit inputUnit) {

        this(
                0,
                inputValue,
                celsius,
                fahrenheit,
                kelvin,
                inputUnit,
                null);
    }

    public int getId() {
        return id;
    }

    public double getInputValue() {
        return inputValue;
    }

    public double getCelsius() {
        return celsius;
    }

    public double getFahrenheit() {
        return fahrenheit;
    }

    public double getKelvin() {
        return kelvin;
    }

    public TemperatureUnit getInputUnit() {
        return inputUnit;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

}