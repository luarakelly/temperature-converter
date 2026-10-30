package app.controllers;

import app.model.TemperatureRecord;

import java.util.Locale;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ResultsController {

    @FXML
    private Label resultsTitle;

    @FXML
    private Label celsiusResult;

    @FXML
    private Label fahrenheitResult;

    @FXML
    private Label kelvinResult;

    public void updateLanguage(ResourceBundle messages, TemperatureRecord record) {
        resultsTitle.setText(messages.getString("results.title"));
        showResults(messages, record);
    }

    public void showResults(ResourceBundle messages, TemperatureRecord record) {
        if (record == null) {
            celsiusResult.setText(messages.getString("result.celsius.empty"));
            fahrenheitResult.setText(messages.getString("result.fahrenheit.empty"));
            kelvinResult.setText(messages.getString("result.kelvin.empty"));
            return;
        }

        celsiusResult.setText(format(
                messages, "result.celsius", record.getCelsius()));
        fahrenheitResult.setText(format(
                messages, "result.fahrenheit", record.getFahrenheit()));
        kelvinResult.setText(format(
                messages, "result.kelvin", record.getKelvin()));
    }

    private String format(ResourceBundle messages, String key, double value) {
        return java.text.MessageFormat.format(
                messages.getString(key),
                String.format(Locale.ROOT, "%.2f", value));
    }
}
