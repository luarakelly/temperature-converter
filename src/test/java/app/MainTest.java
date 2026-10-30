package app;

import app.controllers.MainController;
import app.db.TemperatureRecordDAO;
import app.db.TemperatureUnitDAO;
import app.model.TemperatureConverter;
import app.model.TemperatureRecord;
import app.model.TemperatureUnit;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class MainTest {

    private TemperatureUnitDAO unitDAO;
    private TemperatureRecordDAO recordDAO;
    private MainController controller;
    private Parent root;

    @Start
    void start(Stage stage) throws Exception {
        unitDAO = mock(TemperatureUnitDAO.class);
        recordDAO = mock(TemperatureRecordDAO.class);
        when(unitDAO.findAll()).thenReturn(List.of(
                new TemperatureUnit(1, "Celsius"),
                new TemperatureUnit(2, "Fahrenheit"),
                new TemperatureUnit(3, "Kelvin")));
        when(recordDAO.findAll()).thenReturn(List.of());

        FXMLLoader loader = new FXMLLoader(
                Main.class.getResource("/fxml/main.fxml"),
                ResourceBundle.getBundle("i18n.MessagesBundle", Locale.US));
        loader.setControllerFactory(type -> {
            if (type == MainController.class) {
                return new MainController(
                        new TemperatureConverter(), unitDAO, recordDAO);
            }
            try {
                return type.getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Could not create FXML controller", exception);
            }
        });

        root = loader.load();
        controller = loader.getController();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void coordinatorLoadsAllScreenComponentsAndDatabaseData() throws Exception {
        assertNotNull(root.lookup("#temperatureField"));
        assertNotNull(root.lookup("#calculateButton"));
        assertNotNull(root.lookup("#historyTable"));
        verify(unitDAO).findAll();
        verify(recordDAO).findAll();
    }

    @Test
    void changingLanguageUpdatesAllIncludedScreens() throws Exception {
        runOnFxThread(() -> {
            String[][] localizedTitles = {
                    {"English", "Temperature Converter", "Saved Conversions"},
                    {"Finnish", "Lämpötilamuunnin", "Tallennetut muunnokset"},
                    {"Swedish", "Temperaturomvandlare", "Sparade konverteringar"},
                    {"Japanese", "温度変換", "保存された変換履歴"},
                    {"Arabic", "محول درجات الحرارة", "التحويلات المحفوظة"}
            };

            for (String[] language : localizedTitles) {
                controller.changeLanguage(language[0]);
                assertEquals(language[1], ((Label) root.lookup("#titleLabel")).getText());
                assertEquals(language[2], ((Label) root.lookup("#historyTitle")).getText());
            }
            assertEquals("درجة الحرارة:", ((Label) root.lookup("#temperatureLabel")).getText());
        });
    }

    @Test
    void conversionUsesSelectedUnitAndSavesTheRecord() throws Exception {
        runOnFxThread(() -> {
            ((TextField) root.lookup("#temperatureField")).setText("32");
            @SuppressWarnings("unchecked")
            ComboBox<TemperatureUnit> units =
                    (ComboBox<TemperatureUnit>) root.lookup("#unitComboBox");
            units.getSelectionModel().select(1);
            ((Button) root.lookup("#calculateButton")).fire();
        });

        verify(recordDAO).insert(argThat(record ->
                record.getInputValue() == 32
                        && record.getInputUnit().getUnitName().equals("Fahrenheit")
                        && Math.abs(record.getCelsius()) < 0.001));
        assertTrue(((Label) root.lookup("#celsiusResult")).getText().contains("0.00"));
    }

    @Test
    void emptyInputDoesNotSaveARecord() throws Exception {
        runOnFxThread(() -> {
            try (MockedConstruction<javafx.scene.control.Alert> alerts =
                         mockConstruction(javafx.scene.control.Alert.class)) {
                ((Button) root.lookup("#calculateButton")).fire();
            }
        });

        verify(recordDAO, never()).insert(any(TemperatureRecord.class));
    }

    @Test
    void rejectsUnsupportedLanguage() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.changeLanguage("Klingon"));
    }

    private void runOnFxThread(ThrowingRunnable action) throws Exception {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] failure = new Throwable[1];
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable throwable) {
                failure[0] = throwable;
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS), "JavaFX operation timed out");
        if (failure[0] != null) {
            throw new AssertionError(failure[0]);
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
