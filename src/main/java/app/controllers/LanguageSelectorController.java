package app.controllers;

import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public class LanguageSelectorController {

    @FXML
    private Label languageLabel;

    @FXML
    private ComboBox<String> languageComboBox;

    private Consumer<String> languageSelectionHandler;

    @FXML
    private void initialize() {
        languageComboBox.setOnAction(event -> {
            String selectedLanguage = languageComboBox.getValue();
            if (selectedLanguage != null && languageSelectionHandler != null) {
                languageSelectionHandler.accept(selectedLanguage);
            }
        });
    }

    public void configureLanguages(List<String> languages, String defaultLanguage) {
        languageComboBox.getItems().setAll(languages);
        languageComboBox.setValue(defaultLanguage);
    }

    public void setLanguageSelectionHandler(Consumer<String> handler) {
        languageSelectionHandler = handler;
    }

    public void updateLanguage(ResourceBundle messages) {
        languageLabel.setText(messages.getString("language.prompt"));
    }
}
