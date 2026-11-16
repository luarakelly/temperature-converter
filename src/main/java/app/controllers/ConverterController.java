package app.controllers;

import app.model.MeasurementUnit;

import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class ConverterController {

    @FXML
    private Label valueLabel;

    @FXML
    private TextField valueField;

    @FXML
    private Label fromLabel;

    @FXML
    private ComboBox<MeasurementUnit> fromUnitComboBox;

    @FXML
    private Label toLabel;

    @FXML
    private ComboBox<MeasurementUnit> toUnitComboBox;

    @FXML
    private Label unitDescriptionLabel;

    @FXML
    private Button convertButton;

    private List<MeasurementUnit> allUnits = List.of();

    @FXML
    private void initialize() {
        fromUnitComboBox.setOnAction(event -> refreshTargetUnits(selectedId(toUnitComboBox)));
        toUnitComboBox.setOnAction(event -> refreshDescription());
    }

    public void setConvertHandler(Runnable handler) {
        convertButton.setOnAction(event -> handler.run());
    }

    public String getValueInput() {
        return valueField.getText();
    }

    public MeasurementUnit getFromUnit() {
        return fromUnitComboBox.getValue();
    }

    public MeasurementUnit getToUnit() {
        return toUnitComboBox.getValue();
    }

    public void setUnits(List<MeasurementUnit> units) {
        int fromId = selectedId(fromUnitComboBox);
        int toId = selectedId(toUnitComboBox);
        allUnits = List.copyOf(units);

        fromUnitComboBox.getItems().setAll(allUnits);
        fromUnitComboBox.setValue(findById(allUnits, fromId, 0));
        refreshTargetUnits(toId);
    }

    public void updateLanguage(ResourceBundle messages) {
        valueLabel.setText(messages.getString("value.label"));
        valueField.setPromptText(messages.getString("value.prompt"));
        fromLabel.setText(messages.getString("from.label"));
        toLabel.setText(messages.getString("to.label"));
        convertButton.setText(messages.getString("convert.button"));
    }

    private void refreshTargetUnits(int preferredToId) {
        MeasurementUnit from = fromUnitComboBox.getValue();
        List<MeasurementUnit> targets = allUnits.stream()
                .filter(unit -> unit.isSameCategory(from))
                .toList();
        toUnitComboBox.getItems().setAll(targets);
        toUnitComboBox.setValue(findById(targets, preferredToId, targets.size() > 1 ? 1 : 0));
        refreshDescription();
    }

    private void refreshDescription() {
        MeasurementUnit to = toUnitComboBox.getValue();
        unitDescriptionLabel.setText(to == null ? "" : to.getName() + ": " + to.getDescription());
    }

    private static int selectedId(ComboBox<MeasurementUnit> comboBox) {
        MeasurementUnit unit = comboBox.getValue();
        return unit == null ? -1 : unit.getId();
    }

    private static MeasurementUnit findById(List<MeasurementUnit> units, int id, int fallbackIndex) {
        return units.stream()
                .filter(unit -> unit.getId() == id)
                .findFirst()
                .orElse(units.isEmpty() ? null : units.get(fallbackIndex));
    }
}
