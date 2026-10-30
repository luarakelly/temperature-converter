package app.controllers;

import app.model.TemperatureRecord;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class HistoryController {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private TableView<TemperatureRecord> historyTable;

    @FXML
    private Label historyTitle;

    private final ObservableList<TemperatureRecord> records =
            FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        historyTable.setItems(records);
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    public void setRecords(List<TemperatureRecord> loadedRecords) {
        records.setAll(loadedRecords);
    }

    public void updateLanguage(ResourceBundle messages) {
        historyTitle.setText(messages.getString("history.title"));
        TableColumn<TemperatureRecord, String> idColumn =
                column(messages.getString("column.id"), record ->
                        Integer.toString(record.getId()));
        TableColumn<TemperatureRecord, String> inputColumn =
                column(messages.getString("column.input"), record ->
                        String.format(
                                Locale.ROOT,
                                "%.2f %s",
                                record.getInputValue(),
                                localizedUnit(messages, record.getInputUnit().getUnitName())));
        TableColumn<TemperatureRecord, String> celsiusColumn =
                column(messages.getString("column.celsius"), record ->
                        String.format(Locale.ROOT, "%.2f °C", record.getCelsius()));
        TableColumn<TemperatureRecord, String> fahrenheitColumn =
                column(messages.getString("column.fahrenheit"), record ->
                        String.format(Locale.ROOT, "%.2f °F", record.getFahrenheit()));
        TableColumn<TemperatureRecord, String> kelvinColumn =
                column(messages.getString("column.kelvin"), record ->
                        String.format(Locale.ROOT, "%.2f K", record.getKelvin()));
        TableColumn<TemperatureRecord, String> createdColumn =
                column(messages.getString("column.created"), record ->
                        record.getCreatedAt() == null
                                ? ""
                                : record.getCreatedAt().format(DATE_FORMAT));

        historyTable.getColumns().setAll(
                idColumn, inputColumn, celsiusColumn,
                fahrenheitColumn, kelvinColumn, createdColumn);
        historyTable.refresh();
    }

    private TableColumn<TemperatureRecord, String> column(
            String title,
            java.util.function.Function<TemperatureRecord, String> valueFactory) {
        TableColumn<TemperatureRecord, String> column = new TableColumn<>(title);
        column.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(valueFactory.apply(data.getValue())));
        return column;
    }

    private String localizedUnit(ResourceBundle messages, String unitName) {
        return switch (unitName.toLowerCase(Locale.ROOT)) {
            case "celsius" -> messages.getString("unit.celsius");
            case "fahrenheit" -> messages.getString("unit.fahrenheit");
            case "kelvin" -> messages.getString("unit.kelvin");
            default -> unitName;
        };
    }
}
