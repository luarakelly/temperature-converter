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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class Main extends Application {

        private final TemperatureConverter converter = new TemperatureConverter();
        private final TemperatureUnitDAO unitDAO = new TemperatureUnitDAO();
        private final TemperatureRecordDAO recordDAO = new TemperatureRecordDAO();
        private final ObservableList<TemperatureRecord> records = FXCollections.observableArrayList();
        private final TableView<TemperatureRecord> table = new TableView<>(records);

        private TextField temperatureField;
        private ComboBox<TemperatureUnit> unitComboBox;
        private Label celsiusResult;
        private Label fahrenheitResult;
        private Label kelvinResult;
        private Label statusLabel;

        private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        @Override
        public void start(Stage stage) {
                Label title = new Label("Temperature Converter");
                title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");

                Label temperatureLabel = new Label("Temperature:");
                temperatureField = new TextField();
                temperatureField.setPromptText("Enter temperature");

                Label unitLabel = new Label("Unit:");
                unitComboBox = new ComboBox<>();
                unitComboBox.setPrefWidth(180);

                loadUnits();

                Button calculateButton = new Button("Calculate & Save");
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

                Label resultsTitle = new Label("Conversion Results");
                resultsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

                celsiusResult = new Label("Celsius: -");
                fahrenheitResult = new Label("Fahrenheit: -");
                kelvinResult = new Label("Kelvin: -");

                VBox resultsBox = new VBox(8, resultsTitle, celsiusResult, fahrenheitResult, kelvinResult);
                resultsBox.setPadding(new Insets(10));
                resultsBox.setStyle(
                                "-fx-border-color: #cccccc; -fx-border-radius: 5; " +
                                                "-fx-background-color: #f8f8f8;");

                Label historyTitle = new Label("Saved Conversions");
                historyTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

                createTableColumns();

                table.setPrefHeight(350);
                table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

                statusLabel = new Label("Ready");
                statusLabel.setStyle("-fx-text-fill: #555555;");

                HBox statusBox = new HBox(statusLabel);
                statusBox.setAlignment(Pos.CENTER_LEFT);

                VBox root = new VBox(15, title, inputGrid, resultsBox, historyTitle, table, statusBox);
                root.setPadding(new Insets(20));
                root.setStyle("-fx-background-color: white;");

                loadRecords();

                Scene scene = new Scene(root, 850, 700);
                stage.setTitle("Temperature Converter");
                stage.setScene(scene);
                stage.show();
        }

        private void loadUnits() {
                try {
                        List<TemperatureUnit> units = unitDAO.findAll();
                        unitComboBox.setItems(FXCollections.observableArrayList(units));

                        if (!units.isEmpty()) {
                                unitComboBox.getSelectionModel().selectFirst();
                        }
                } catch (Exception e) {
                        showError("Database Error", "Could not load temperature units.", e);
                }
        }

        private void calculateAndSave() {
                try {
                        String input = temperatureField.getText().trim();

                        if (input.isEmpty()) {
                                showWarning("Input Required", "Please enter a temperature.");
                                return;
                        }

                        double value = Double.parseDouble(input);
                        TemperatureUnit selectedUnit = unitComboBox.getSelectionModel().getSelectedItem();

                        if (selectedUnit == null) {
                                showWarning("Unit Required", "Please select a temperature unit.");
                                return;
                        }

                        if (!converter.isValidTemperature(value, selectedUnit.getUnitName())) {
                                showWarning("Invalid Temperature", "Kelvin cannot be below 0.");
                                return;
                        }

                        double celsius = converter.toCelsius(value, selectedUnit.getUnitName());
                        double fahrenheit = converter.toFahrenheit(value, selectedUnit.getUnitName());
                        double kelvin = converter.toKelvin(value, selectedUnit.getUnitName());

                        celsiusResult.setText(String.format("Celsius: %.2f °C", celsius));
                        fahrenheitResult.setText(String.format("Fahrenheit: %.2f °F", fahrenheit));
                        kelvinResult.setText(String.format("Kelvin: %.2f K", kelvin));

                        TemperatureRecord record = new TemperatureRecord(
                                        value, celsius, fahrenheit, kelvin, selectedUnit);

                        recordDAO.insert(record);
                        loadRecords();
                        statusLabel.setText("Conversion calculated and saved.");

                } catch (NumberFormatException e) {
                        showWarning("Invalid Input", "Please enter a valid number.");
                } catch (Exception e) {
                        showError("Database Error", "Could not save the conversion.", e);
                }
        }

        private void loadRecords() {
                try {
                        List<TemperatureRecord> savedRecords = recordDAO.findAll();
                        records.setAll(savedRecords);
                } catch (Exception e) {
                        showError("Database Error", "Could not load saved conversions.", e);
                }
        }

        private void createTableColumns() {
                TableColumn<TemperatureRecord, String> idColumn = new TableColumn<>("ID");
                idColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));

                TableColumn<TemperatureRecord, String> inputColumn = new TableColumn<>("Input");
                inputColumn.setCellValueFactory(data -> new SimpleStringProperty(String.format(
                                "%.2f %s",
                                data.getValue().getInputValue(),
                                data.getValue().getInputUnit().getUnitName())));

                TableColumn<TemperatureRecord, String> celsiusColumn = new TableColumn<>("Celsius");
                celsiusColumn.setCellValueFactory(data -> new SimpleStringProperty(String.format(
                                "%.2f °C",
                                data.getValue().getCelsius())));

                TableColumn<TemperatureRecord, String> fahrenheitColumn = new TableColumn<>("Fahrenheit");
                fahrenheitColumn.setCellValueFactory(data -> new SimpleStringProperty(String.format(
                                "%.2f °F",
                                data.getValue().getFahrenheit())));

                TableColumn<TemperatureRecord, String> kelvinColumn = new TableColumn<>("Kelvin");
                kelvinColumn.setCellValueFactory(data -> new SimpleStringProperty(String.format(
                                "%.2f K",
                                data.getValue().getKelvin())));

                TableColumn<TemperatureRecord, String> dateColumn = new TableColumn<>("Created");
                dateColumn.setCellValueFactory(data -> new SimpleStringProperty(
                                data.getValue().getCreatedAt().format(dateFormatter)));

                table.getColumns().addAll(
                                idColumn, inputColumn, celsiusColumn,
                                fahrenheitColumn, kelvinColumn, dateColumn);
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
