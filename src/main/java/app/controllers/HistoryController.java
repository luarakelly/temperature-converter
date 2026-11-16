package app.controllers;

import app.model.ConversionRecord;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class HistoryController {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    private TableView<ConversionRecord> historyTable;

    @FXML
    private Label historyTitle;

    private final ObservableList<ConversionRecord> records =
            FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        historyTable.setItems(records);
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    public void setRecords(List<ConversionRecord> loadedRecords) {
        records.setAll(loadedRecords);
    }

    public void updateLanguage(ResourceBundle messages) {
        historyTitle.setText(messages.getString("history.title"));
        historyTable.getColumns().setAll(List.of(
                column(messages.getString("column.input"), record ->
                        DisplayFormat.number(record.getInputValue())
                                + " " + record.getFromUnit().getName()),
                column(messages.getString("column.result"), record ->
                        DisplayFormat.number(record.getResultValue())
                                + " " + record.getToUnit().getName()),
                column(messages.getString("column.language"), ConversionRecord::getLanguageCode),
                column(messages.getString("column.created"), record ->
                        record.getCreatedAt() == null
                                ? ""
                                : record.getCreatedAt().format(DATE_FORMAT))));
        historyTable.refresh();
    }

    private TableColumn<ConversionRecord, String> column(
            String title,
            Function<ConversionRecord, String> valueFactory) {
        TableColumn<ConversionRecord, String> column = new TableColumn<>(title);
        column.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(valueFactory.apply(data.getValue())));
        return column;
    }
}
