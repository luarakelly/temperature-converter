package app.model;

import java.time.LocalDateTime;

public class ConversionRecord {

    private final long id;
    private final double inputValue;
    private final MeasurementUnit fromUnit;
    private final MeasurementUnit toUnit;
    private final double resultValue;
    private final String languageCode;
    private final LocalDateTime createdAt;

    public ConversionRecord(
            long id,
            double inputValue,
            MeasurementUnit fromUnit,
            MeasurementUnit toUnit,
            double resultValue,
            String languageCode,
            LocalDateTime createdAt) {
        this.id = id;
        this.inputValue = inputValue;
        this.fromUnit = fromUnit;
        this.toUnit = toUnit;
        this.resultValue = resultValue;
        this.languageCode = languageCode;
        this.createdAt = createdAt;
    }

    public ConversionRecord(
            double inputValue,
            MeasurementUnit fromUnit,
            MeasurementUnit toUnit,
            double resultValue,
            String languageCode) {
        this(0, inputValue, fromUnit, toUnit, resultValue, languageCode, null);
    }

    public long getId() {
        return id;
    }

    public double getInputValue() {
        return inputValue;
    }

    public MeasurementUnit getFromUnit() {
        return fromUnit;
    }

    public MeasurementUnit getToUnit() {
        return toUnit;
    }

    public double getResultValue() {
        return resultValue;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public ConversionRecord withUnits(MeasurementUnit newFromUnit, MeasurementUnit newToUnit) {
        return new ConversionRecord(
                id, inputValue, newFromUnit, newToUnit, resultValue, languageCode, createdAt);
    }
}
