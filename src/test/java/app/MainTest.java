package app;

import app.controllers.MainController;
import app.db.ConversionRecordDAO;
import app.db.MeasurementUnitDAO;
import app.model.ConversionRecord;
import app.model.MeasurementUnit;
import app.model.UnitConverter;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class MainTest {

    private MeasurementUnitDAO unitDAO;
    private ConversionRecordDAO recordDAO;
    private MainController controller;
    private Parent root;

    @Start
    void start(Stage stage) throws Exception {
        unitDAO = mock(MeasurementUnitDAO.class);
        recordDAO = mock(ConversionRecordDAO.class);
        when(unitDAO.findAll(anyString())).thenAnswer(invocation -> units(invocation.getArgument(0)));
        when(recordDAO.findLatest(anyString(), anyInt())).thenReturn(List.of());

        FXMLLoader loader = new FXMLLoader(
                Main.class.getResource("/fxml/main.fxml"),
                ResourceBundle.getBundle("i18n.MessagesBundle", Locale.US));
        loader.setControllerFactory(type -> {
            if (type == MainController.class) {
                return new MainController(new UnitConverter(), unitDAO, recordDAO);
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

    private static List<MeasurementUnit> units(String language) {
        boolean japanese = "ja".equals(language);
        return List.of(
                new MeasurementUnit(1, "celsius", "temperature", 1,
                        japanese ? "摂氏" : "Celsius", "desc"),
                new MeasurementUnit(2, "fahrenheit", "temperature", 1,
                        japanese ? "華氏" : "Fahrenheit", "desc"),
                new MeasurementUnit(8, "kilometer", "distance", 1000,
                        japanese ? "キロメートル" : "kilometre", "desc"),
                new MeasurementUnit(9, "mile", "distance", 1609.344,
                        japanese ? "マイル" : "mile", "desc"));
    }

    @Test
    void loadsScreenComponentsAndLocalizedDataFromTheDatabase() throws Exception {
        assertNotNull(root.lookup("#valueField"));
        assertNotNull(root.lookup("#convertButton"));
        assertNotNull(root.lookup("#historyTable"));
        verify(unitDAO).findAll("en");
        verify(recordDAO).findLatest("en", MainController.HISTORY_LIMIT);
    }

    @Test
    void changingLanguageUpdatesBundleAndDatabaseLanguage() throws Exception {
        runOnFxThread(() -> {
            String[][] localized = {
                    {"English", "en", "Unit Converter"},
                    {"Finnish", "fi", "Yksikkömuunnin"},
                    {"Swedish", "sv", "Enhetsomvandlare"},
                    {"Japanese", "ja", "単位変換"},
                    {"Arabic", "ar", "محول الوحدات"}
            };

            for (String[] language : localized) {
                controller.changeLanguage(language[0]);
                assertEquals(language[1], controller.getLanguageCode());
                assertEquals(language[2], ((Label) root.lookup("#titleLabel")).getText());
            }
            assertEquals("القيمة:", ((Label) root.lookup("#valueLabel")).getText());
        });

        verify(unitDAO).findAll("ja");
        verify(unitDAO).findAll("ar");
        verify(recordDAO).findLatest("ar", MainController.HISTORY_LIMIT);
    }

    @Test
    void arabicSwitchesToRightToLeft() throws Exception {
        runOnFxThread(() -> {
            controller.changeLanguage("Arabic");
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, root.getNodeOrientation());
            controller.changeLanguage("English");
            assertEquals(NodeOrientation.LEFT_TO_RIGHT, root.getNodeOrientation());
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    void targetUnitsFollowTheSourceCategory() throws Exception {
        runOnFxThread(() -> {
            ComboBox<MeasurementUnit> from =
                    (ComboBox<MeasurementUnit>) root.lookup("#fromUnitComboBox");
            ComboBox<MeasurementUnit> to =
                    (ComboBox<MeasurementUnit>) root.lookup("#toUnitComboBox");
            from.setValue(from.getItems().get(2));
            assertEquals(List.of("kilometer", "mile"),
                    to.getItems().stream().map(MeasurementUnit::getUnitKey).toList());
            assertEquals("mile", to.getValue().getUnitKey());
        });
    }

    @Test
    void conversionSavesTheRecordAndShowsLocalizedResult() throws Exception {
        runOnFxThread(() -> {
            ((TextField) root.lookup("#valueField")).setText("100");
            ((Button) root.lookup("#convertButton")).fire();
        });

        verify(recordDAO).insert(argThat(record ->
                record.getInputValue() == 100
                        && record.getFromUnit().getUnitKey().equals("celsius")
                        && record.getToUnit().getUnitKey().equals("fahrenheit")
                        && Math.abs(record.getResultValue() - 212) < 0.001
                        && record.getLanguageCode().equals("en")));
        assertEquals("100 Celsius = 212 Fahrenheit",
                ((Label) root.lookup("#resultLabel")).getText());

        runOnFxThread(() -> controller.changeLanguage("Japanese"));
        assertEquals("100 摂氏 = 212 華氏",
                ((Label) root.lookup("#resultLabel")).getText());
    }

    @Test
    void emptyInputDoesNotSaveARecord() throws Exception {
        runOnFxThread(() -> {
            try (MockedConstruction<javafx.scene.control.Alert> alerts =
                         mockConstruction(javafx.scene.control.Alert.class)) {
                ((Button) root.lookup("#convertButton")).fire();
            }
        });

        verify(recordDAO, never()).insert(any(ConversionRecord.class));
    }

    @Test
    void invalidInputDoesNotSaveARecord() throws Exception {
        runOnFxThread(() -> {
            try (MockedConstruction<javafx.scene.control.Alert> alerts =
                         mockConstruction(javafx.scene.control.Alert.class)) {
                ((TextField) root.lookup("#valueField")).setText("abc");
                ((Button) root.lookup("#convertButton")).fire();
            }
        });

        verify(recordDAO, never()).insert(any(ConversionRecord.class));
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
