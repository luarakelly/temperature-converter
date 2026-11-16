package app.controllers;

import app.db.ConversionRecordDAO;
import app.db.MeasurementUnitDAO;
import app.model.ConversionRecord;
import app.model.MeasurementUnit;
import app.model.UnitConverter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class MainController {

    public static final int HISTORY_LIMIT = 5;

    private static final Logger LOGGER =
            Logger.getLogger(MainController.class.getName());

    private final UnitConverter converter;
    private final MeasurementUnitDAO unitDAO;
    private final ConversionRecordDAO recordDAO;
    private final Map<String, Locale> availableLanguages = new LinkedHashMap<>();

    @FXML
    private VBox rootPane;

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
    private String languageCode = MeasurementUnitDAO.DEFAULT_LANGUAGE;
    private List<MeasurementUnit> units = List.of();
    private ConversionRecord lastResult;
    private String statusMessageKey = "status.ready";

    public MainController() {
        this(new UnitConverter(), new MeasurementUnitDAO(), new ConversionRecordDAO());
    }

    public MainController(
            UnitConverter converter,
            MeasurementUnitDAO unitDAO,
            ConversionRecordDAO recordDAO) {
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
        converterPanelController.setConvertHandler(this::convertAndSave);

        changeLanguage("English");
    }

    public void changeLanguage(String language) {
        Locale locale = availableLanguages.get(language);
        if (locale == null) {
            throw new IllegalArgumentException("Unsupported language: " + language);
        }

        messages = ResourceBundle.getBundle("i18n.MessagesBundle", locale);
        languageCode = locale.getLanguage();
        rootPane.setNodeOrientation("ar".equals(languageCode)
                ? NodeOrientation.RIGHT_TO_LEFT
                : NodeOrientation.LEFT_TO_RIGHT);

        titleLabel.setText(messages.getString("app.title"));
        languageSelectorController.updateLanguage(messages);
        converterPanelController.updateLanguage(messages);
        historyPanelController.updateLanguage(messages);
        statusLabel.setText(messages.getString(statusMessageKey));

        loadUnits();
        lastResult = relocalize(lastResult);
        resultsPanelController.updateLanguage(messages, lastResult);
        loadRecords();
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public ReadOnlyStringProperty titleProperty() {
        return titleLabel.textProperty();
    }

    private void loadUnits() {
        try {
            units = unitDAO.findAll(languageCode);
            converterPanelController.setUnits(units);
        } catch (Exception exception) {
            showError("error.database.title", "error.load.units", exception);
        }
    }

    private void loadRecords() {
        try {
            historyPanelController.setRecords(recordDAO.findLatest(languageCode, HISTORY_LIMIT));
        } catch (Exception exception) {
            showError("error.database.title", "error.load.records", exception);
        }
    }

    private ConversionRecord relocalize(ConversionRecord record) {
        if (record == null) {
            return null;
        }
        return record.withUnits(
                findUnit(record.getFromUnit()),
                findUnit(record.getToUnit()));
    }

    private MeasurementUnit findUnit(MeasurementUnit unit) {
        return units.stream()
                .filter(candidate -> candidate.getId() == unit.getId())
                .findFirst()
                .orElse(unit);
    }

    private void convertAndSave() {
        String input = converterPanelController.getValueInput();
        if (input == null || input.isBlank()) {
            showWarning("warning.input.title", "warning.input");
            return;
        }

        MeasurementUnit from = converterPanelController.getFromUnit();
        MeasurementUnit to = converterPanelController.getToUnit();
        if (from == null || to == null) {
            showWarning("warning.unit.title", "warning.unit");
            return;
        }

        final double value;
        try {
            value = Double.parseDouble(input.trim().replace(',', '.'));
        } catch (NumberFormatException exception) {
            showWarning("warning.invalid.title", "warning.number");
            return;
        }

        if (!converter.isValidValue(value, from)) {
            showWarning("warning.invalid.title", "warning.invalid");
            return;
        }

        ConversionRecord result = new ConversionRecord(
                value, from, to, converter.convert(value, from, to), languageCode);

        try {
            recordDAO.insert(result);
            lastResult = result;
            resultsPanelController.showResult(messages, lastResult);
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
