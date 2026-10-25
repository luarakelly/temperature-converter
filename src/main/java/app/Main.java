package app;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class Main extends Application {

        private final TemperatureConverter converter;
        private final TemperatureUnitDAO unitDAO;
        private final TemperatureRecordDAO recordDAO;
        private final ObservableList<TemperatureRecord> records = FXCollections.observableArrayList();
        private final TableView<TemperatureRecord> table = new TableView<>(records);
        private final Map<String, Locale> languageOptions = new LinkedHashMap<>();

        private ResourceBundle messages;
        private TextField temperatureField;
        private ComboBox<TemperatureUnit> unitComboBox;
        private Label celsiusResult;
        private Label fahrenheitResult;
        private Label kelvinResult;
        private Label statusLabel;
        private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        public Main() {
                this(new TemperatureConverter(), new TemperatureUnitDAO(), new TemperatureRecordDAO());
        }

        // Constructor for unit tests
        Main(TemperatureConverter converter, TemperatureUnitDAO unitDAO, TemperatureRecordDAO recordDAO) {
                this.converter = converter;
                this.unitDAO = unitDAO;
                this.recordDAO = recordDAO;
        }

        @Override
        public void start(Stage stage) {

                languageOptions.put("English", Locale.US);
                languageOptions.put("Finnish", Locale.forLanguageTag("fi-FI"));
                languageOptions.put("Swedish", Locale.forLanguageTag("sv-SE"));
                languageOptions.put("Japanese", Locale.JAPAN);

                ChoiceDialog<String> languageDialog = new ChoiceDialog<>("English", languageOptions.keySet());

                languageDialog.setTitle("Language / Kieli / Språk / 言語");
                languageDialog.setHeaderText("Select your preferred language / Valitse kieli");
                languageDialog.setContentText("Language / Kieli / Språk / 言語:");

                String selectedLanguage = languageDialog.showAndWait().orElse("English");

                Locale selectedLocale = languageOptions.getOrDefault(selectedLanguage, Locale.US);

                messages = ResourceBundle.getBundle("MessagesBundle", selectedLocale);
                stage.setTitle(text("app.title"));

                Label title = new Label(text("app.title"));
                title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");
                Label temperatureLabel = new Label(text("temperature.label"));
                temperatureField = new TextField();
                temperatureField.setPromptText(text("temperature.prompt"));
                Label unitLabel = new Label(text("unit.label"));

                unitComboBox = new ComboBox<>();
                unitComboBox.setPrefWidth(180);

                loadUnits();

                Button calculateButton = new Button(text("calculate.button"));

                calculateButton.setPrefWidth(180);
                calculateButton.setOnAction(event -> calculateAndSave());

                GridPane inputGrid = new GridPane();
                inputGrid.setHgap(15);
                inputGrid.setVgap(12);
                inputGrid.add(temperatureLabel, 0, 0);
                inputGrid.add(temperatureField, 1, 0);
                inputGrid.add(unitLabel, 0, 1);
                inputGrid.add(unitComboBox, 1, 1);
                inputGrid.add(calculateButton, 1, 2);

                Label resultsTitle = new Label(text("results.title"));
                resultsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

                celsiusResult = new Label(format("result.celsius", "-"));
                fahrenheitResult = new Label(format("result.fahrenheit", "-"));
                kelvinResult = new Label(format("result.kelvin", "-"));

                VBox resultsBox = new VBox(
                                8,
                                resultsTitle,
                                celsiusResult,
                                fahrenheitResult,
                                kelvinResult);
                resultsBox.setPadding(new Insets(10));
                resultsBox.setStyle("-fx-border-color: #cccccc; " +
                                "-fx-border-radius: 5; " +
                                "-fx-background-color: #f8f8f8;");

                Label historyTitle = new Label(text("history.title"));
                historyTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

                createTableColumns();

                table.setPrefHeight(350);
                table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

                statusLabel = new Label(text("status.ready"));
                statusLabel.setStyle("-fx-text-fill: #555555;");

                HBox statusBox = new HBox(statusLabel);
                statusBox.setAlignment(Pos.CENTER_LEFT);

                VBox root = new VBox(
                                15,
                                title,
                                inputGrid,
                                resultsBox,
                                historyTitle,
                                table,
                                statusBox);

                root.setPadding(new Insets(20));
                root.setStyle("-fx-background-color: white;");

                loadRecords();

                Scene scene = new Scene(root, 850, 700);

                stage.setScene(scene);
                stage.show();
        }

        private String text(String key) {
                return messages.getString(key);
        }

        private String format(String key, Object value) {
                return MessageFormat.format(text(key), value);
        }

        private String localizedUnit(String unitName) {
                return switch (unitName.toLowerCase(Locale.ROOT)) {
                        case "celsius" -> text("unit.celsius");
                        case "fahrenheit" -> text("unit.fahrenheit");
                        case "kelvin" -> text("unit.kelvin");
                        default -> unitName;
                };
        }

        private void loadUnits() {
                try {
                        List<TemperatureUnit> units = unitDAO.findAll();
                        units.forEach(unit -> unit.setUnitName(localizedUnit(unit.getUnitName())));
                        unitComboBox.setItems(FXCollections.observableArrayList(units));

                        if (!units.isEmpty()) {
                                unitComboBox.getSelectionModel().selectFirst();
                        }

                } catch (Exception e) {
                        showError(text("error.database.title"), text("error.load.units"), e);
                }
        }

        private void calculateAndSave() {
                try {
                        String input = temperatureField.getText().trim();

                        if (input.isEmpty()) {
                                showWarning(text("warning.input.title"), text("warning.input"));
                                return;
                        }

                        double value = Double.parseDouble(input);

                        TemperatureUnit selectedUnit = unitComboBox.getSelectionModel().getSelectedItem();

                        if (selectedUnit == null) {
                                showWarning(text("warning.unit.title"), text("warning.unit"));
                                return;
                        }

                        String unit = canonicalUnit(selectedUnit.getUnitName());

                        if (!Double.isFinite(value)) {
                                showWarning(text("warning.invalid.title"), text("warning.number"));
                                return;
                        }

                        if (!converter.isValidTemperature(value, unit)) {
                                showWarning(text("warning.invalid.title"), text("warning.invalid"));
                                return;
                        }

                        double celsius = converter.toCelsius(value, unit);
                        double fahrenheit = converter.toFahrenheit(value, unit);
                        double kelvin = converter.toKelvin(value, unit);

                        celsiusResult.setText(format("result.celsius",
                                        String.format(Locale.ROOT, "%.2f", celsius)));

                        fahrenheitResult.setText(format("result.fahrenheit",
                                        String.format(Locale.ROOT, "%.2f", fahrenheit)));

                        kelvinResult.setText(format("result.kelvin",
                                        String.format(Locale.ROOT, "%.2f", kelvin)));

                        TemperatureUnit databaseUnit = new TemperatureUnit(selectedUnit.getId(), unit);

                        TemperatureRecord record = new TemperatureRecord(
                                        value,
                                        celsius,
                                        fahrenheit,
                                        kelvin,
                                        databaseUnit);

                        recordDAO.insert(record);
                        loadRecords();

                        statusLabel.setText(text("status.saved"));

                } catch (NumberFormatException e) {
                        showWarning(text("warning.invalid.title"), text("warning.number"));

                } catch (Exception e) {
                        showError(text("error.database.title"), text("error.save"), e);
                }
        }

        private String canonicalUnit(String displayedUnit) {
                for (String unit : List.of("Celsius", "Fahrenheit", "Kelvin")) {
                        if (localizedUnit(unit).equals(displayedUnit)) {
                                return unit;
                        }
                }

                if (List.of("Celsius", "Fahrenheit", "Kelvin").contains(displayedUnit)) {
                        return displayedUnit;
                }

                throw new IllegalArgumentException("Unknown temperature unit: " + displayedUnit);
        }

        private void loadRecords() {
                try {
                        List<TemperatureRecord> savedRecords = recordDAO.findAll();
                        records.setAll(savedRecords);

                } catch (Exception e) {
                        showError(text("error.database.title"), text("error.load.records"), e);
                }
        }

        private void createTableColumns() {

                TableColumn<TemperatureRecord, String> idColumn = new TableColumn<>(text("column.id"));
                idColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));

                TableColumn<TemperatureRecord, String> inputColumn = new TableColumn<>(text("column.input"));
                inputColumn.setCellValueFactory(data -> new SimpleStringProperty(
                                String.format(Locale.ROOT, "%.2f %s", data.getValue().getInputValue(),
                                                localizedUnit(data.getValue().getInputUnit().getUnitName()))));

                TableColumn<TemperatureRecord, String> celsiusColumn = new TableColumn<>(text("column.celsius"));
                celsiusColumn.setCellValueFactory(data -> new SimpleStringProperty(
                                String.format(Locale.ROOT, "%.2f °C", data.getValue().getCelsius())));

                TableColumn<TemperatureRecord, String> fahrenheitColumn = new TableColumn<>(text("column.fahrenheit"));
                fahrenheitColumn.setCellValueFactory(data -> new SimpleStringProperty(
                                String.format(Locale.ROOT, "%.2f °F", data.getValue().getFahrenheit())));

                TableColumn<TemperatureRecord, String> kelvinColumn = new TableColumn<>(text("column.kelvin"));
                kelvinColumn.setCellValueFactory(data -> new SimpleStringProperty(
                                String.format(Locale.ROOT, "%.2f K", data.getValue().getKelvin())));

                TableColumn<TemperatureRecord, String> dateColumn = new TableColumn<>(text("column.created"));
                dateColumn.setCellValueFactory(
                                data -> new SimpleStringProperty(data.getValue().getCreatedAt().format(dateFormatter)));

                table.getColumns().setAll(idColumn, inputColumn, celsiusColumn, fahrenheitColumn, kelvinColumn,
                                dateColumn);
        }

        private void showWarning(String title, String message) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
        }

        private void showError(String title, String message, Exception exception) {
                exception.printStackTrace();

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message + "\n\n" + exception.getMessage());
                alert.showAndWait();
        }

        public static void main(String[] args) {
                launch(args);
        }
}
