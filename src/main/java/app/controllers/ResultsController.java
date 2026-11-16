package app.controllers;

import app.model.ConversionRecord;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ResultsController {

    @FXML
    private Label resultsTitle;

    @FXML
    private Label resultLabel;

    @FXML
    private Label resultDescriptionLabel;

    public void updateLanguage(ResourceBundle messages, ConversionRecord record) {
        resultsTitle.setText(messages.getString("results.title"));
        showResult(messages, record);
    }

    public void showResult(ResourceBundle messages, ConversionRecord record) {
        if (record == null) {
            resultLabel.setText(messages.getString("result.empty"));
            resultDescriptionLabel.setText("");
            return;
        }

        resultLabel.setText(MessageFormat.format(
                messages.getString("result.format"),
                DisplayFormat.number(record.getInputValue()),
                record.getFromUnit().getName(),
                DisplayFormat.number(record.getResultValue()),
                record.getToUnit().getName()));
        resultDescriptionLabel.setText(
                record.getToUnit().getName() + ": " + record.getToUnit().getDescription());
    }
}
