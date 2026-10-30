package app.controllers;

import app.db.TemperatureRecordDAO;
import app.db.TemperatureUnitDAO;
import app.model.TemperatureConverter;
import app.model.TemperatureRecord;
import app.model.TemperatureUnit;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

public class MainController {

    private static final Logger LOGGER =
            Logger.getLogger(MainController.class.getName());

    private final TemperatureConverter converter;
    private final TemperatureUnitDAO unitDAO;
    private final TemperatureRecordDAO recordDAO;
    private final Map<String, Locale> availableLanguages = new LinkedHashMap<>();

    @FXML
    private Label titleLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private LanguageSelectorController languageSelectorController;

    @FXML
    private ConverterController converterPanelController;

    @FXML
    private ResultsController resultsPanelController;

    @FXML
    private HistoryController historyPanelController;

    private ResourceBundle messages;
    private TemperatureRecord lastResult;
    private String statusMessageKey = "status.ready";

    public MainController() {
        this(new TemperatureConverter(), new TemperatureUnitDAO(), new TemperatureRecordDAO());
    }

    public MainController(
            TemperatureConverter converter,
            TemperatureUnitDAO unitDAO,
            TemperatureRecordDAO recordDAO) {
        this.converter = converter;
        this.unitDAO = unitDAO;
        this.recordDAO = recordDAO;
        availableLanguages.put("English", Locale.US);
        availableLanguages.put("Finnish", Locale.forLanguageTag("fi-FI"));
        availableLanguages.put("Swedish", Locale.forLanguageTag("sv-SE"));
        availableLanguages.put("Japanese", Locale.JAPAN);
        availableLanguages.put("Arabic", Locale.forLanguageTag("ar-AR"));
    }

    @FXML
    private void initialize() {
        languageSelectorController.configureLanguages(
                List.copyOf(availableLanguages.keySet()), "English");
        languageSelectorController.setLanguageSelectionHandler(this::changeLanguage);
        converterPanelController.setCalculateHandler(this::calculateAndSave);

        changeLanguage("English");
        loadUnits();
        loadRecords();
    }

    public void changeLanguage(String language) {
        Locale locale = availableLanguages.get(language);
        if (locale == null) {
            throw new IllegalArgumentException("Unsupported language: " + language);
        }

        messages = ResourceBundle.getBundle("i18n.MessagesBundle", locale);
        titleLabel.setText(messages.getString("app.title"));
        languageSelectorController.updateLanguage(messages);
        converterPanelController.updateLanguage(messages);
        resultsPanelController.updateLanguage(messages, lastResult);
        historyPanelController.updateLanguage(messages);
        statusLabel.setText(messages.getString(statusMessageKey));
    }

    public ReadOnlyStringProperty titleProperty() {
        return titleLabel.textProperty();
    }

    private void loadUnits() {
        try {
            converterPanelController.setUnits(unitDAO.findAll());
        } catch (Exception exception) {
            showError("error.database.title", "error.load.units", exception);
        }
    }

    private void loadRecords() {
        try {
            historyPanelController.setRecords(recordDAO.findAll());
        } catch (Exception exception) {
            showError("error.database.title", "error.load.records", exception);
        }
    }

    @FXML
    private void calculateAndSave() {
        String input = converterPanelController.getTemperatureInput().trim();
        if (input.isEmpty()) {
            showWarning("warning.input.title", "warning.input");
            return;
        }

        TemperatureUnit selectedUnit = converterPanelController.getSelectedUnit();
        if (selectedUnit == null) {
            showWarning("warning.unit.title", "warning.unit");
            return;
        }

        final double value;
        try {
            value = Double.parseDouble(input);
        } catch (NumberFormatException exception) {
            showWarning("warning.invalid.title", "warning.number");
            return;
        }

        String unit = selectedUnit.getUnitName();
        if (!Double.isFinite(value)) {
            showWarning("warning.invalid.title", "warning.number");
            return;
        }
        if (!converter.isValidTemperature(value, unit)) {
            showWarning("warning.invalid.title", "warning.invalid");
            return;
        }

        TemperatureRecord result = new TemperatureRecord(
                value,
                converter.toCelsius(value, unit),
                converter.toFahrenheit(value, unit),
                converter.toKelvin(value, unit),
                selectedUnit);

        try {
            recordDAO.insert(result);
            lastResult = result;
            resultsPanelController.showResults(messages, lastResult);
            loadRecords();
            statusMessageKey = "status.saved";
            statusLabel.setText(messages.getString(statusMessageKey));
        } catch (Exception exception) {
            showError("error.database.title", "error.save", exception);
        }
    }

    private void showWarning(String titleKey, String messageKey) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(messages.getString(titleKey));
        alert.setHeaderText(null);
        alert.setContentText(messages.getString(messageKey));
        alert.showAndWait();
    }

    private void showError(String titleKey, String messageKey, Exception exception) {
        LOGGER.log(Level.SEVERE, messages.getString(messageKey), exception);

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(messages.getString(titleKey));
        alert.setHeaderText(null);
        alert.setContentText(messages.getString(messageKey) + "\n\n" + exception.getMessage());
        alert.showAndWait();
    }
}
