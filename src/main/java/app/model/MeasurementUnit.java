package app.model;

public class MeasurementUnit {

    private final int id;
    private final String unitKey;
    private final String categoryCode;
    private final double factorToBase;
    private final String name;
    private final String description;

    public MeasurementUnit(
            int id,
            String unitKey,
            String categoryCode,
            double factorToBase,
            String name,
            String description) {
        this.id = id;
        this.unitKey = unitKey;
        this.categoryCode = categoryCode;
        this.factorToBase = factorToBase;
        this.name = name;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getUnitKey() {
        return unitKey;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public double getFactorToBase() {
        return factorToBase;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSameCategory(MeasurementUnit other) {
        return other != null && categoryCode.equals(other.categoryCode);
    }

    @Override
    public String toString() {
        return name;
    }
}
