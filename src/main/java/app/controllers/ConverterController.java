package app.controllers;

import app.model.TemperatureUnit;

import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

public class ConverterController {

    @FXML
    private Label temperatureLabel;

    @FXML
    private TextField temperatureField;

    @FXML
    private Label unitLabel;

    @FXML
    private ComboBox<TemperatureUnit> unitComboBox;

    @FXML
    private Button calculateButton;

    private ResourceBundle messages;

    @FXML
    private void initialize() {
        unitComboBox.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(TemperatureUnit unit, boolean empty) {
                super.updateItem(unit, empty);
                setText(empty || unit == null ? null : localizedUnit(unit.getUnitName()));
            }
        });
        unitComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(TemperatureUnit unit) {
                return unit == null ? "" : localizedUnit(unit.getUnitName());
            }

            @Override
            public TemperatureUnit fromString(String value) {
                return unitComboBox.getItems().stream()
                        .filter(unit -> localizedUnit(unit.getUnitName()).equals(value))
                        .findFirst()
                        .orElse(null);
            }
        });
    }

    public void setCalculateHandler(Runnable handler) {
        calculateButton.setOnAction(event -> handler.run());
    }

    public String getTemperatureInput() {
        return temperatureField.getText();
    }

    public TemperatureUnit getSelectedUnit() {
        return unitComboBox.getSelectionModel().getSelectedItem();
    }

    public void setUnits(List<TemperatureUnit> units) {
        unitComboBox.getItems().setAll(units);
        if (!units.isEmpty()) {
            unitComboBox.getSelectionModel().selectFirst();
        }
    }

    public void updateLanguage(ResourceBundle bundle) {
        messages = bundle;
        temperatureLabel.setText(bundle.getString("temperature.label"));
        temperatureField.setPromptText(bundle.getString("temperature.prompt"));
        unitLabel.setText(bundle.getString("unit.label"));
        calculateButton.setText(bundle.getString("calculate.button"));
        unitComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(TemperatureUnit unit, boolean empty) {
                super.updateItem(unit, empty);
                setText(empty || unit == null ? null : localizedUnit(unit.getUnitName()));
            }
        });
    }

    private String localizedUnit(String unitName) {
        if (messages == null) {
            return unitName;
        }

        return switch (unitName.toLowerCase(java.util.Locale.ROOT)) {
            case "celsius" -> messages.getString("unit.celsius");
            case "fahrenheit" -> messages.getString("unit.fahrenheit");
            case "kelvin" -> messages.getString("unit.kelvin");
            default -> unitName;
        };
    }
}
