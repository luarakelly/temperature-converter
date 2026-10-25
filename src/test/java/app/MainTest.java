package app;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import org.mockito.MockedConstruction;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class MainTest {

    private TemperatureConverter converter;
    private TemperatureUnitDAO unitDAO;
    private TemperatureRecordDAO recordDAO;
    private Main mainApp;

    @Start
    void start(Stage stage) throws Exception {
        converter = new TemperatureConverter();
        unitDAO = mock(TemperatureUnitDAO.class);
        recordDAO = mock(TemperatureRecordDAO.class);

        when(unitDAO.findAll()).thenReturn(List.of());
        when(recordDAO.findAll()).thenReturn(List.of());

        mainApp = new Main(converter, unitDAO, recordDAO);

        try (MockedConstruction<ChoiceDialog> dialogs = mockConstruction(ChoiceDialog.class,
                (dialog, context) -> when(dialog.showAndWait())
                        .thenReturn(Optional.of("English")))) {
            mainApp.start(stage);
        }
    }

    @BeforeEach
    void verifyApplicationCreated() {
        assertNotNull(mainApp);
    }

    private Object getField(String name) throws Exception {
        Field field = Main.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(mainApp);
    }

    private void setField(String name, Object value) throws Exception {
        Field field = Main.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(mainApp, value);
    }

    private void invoke(String methodName) throws Exception {
        Method method = Main.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(mainApp);
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

        assertTrue(latch.await(10, TimeUnit.SECONDS),
                "JavaFX operation timed out");

        if (failure[0] != null) {
            throw new AssertionError(failure[0]);
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private void runWithMockedAlerts(ThrowingRunnable action)
            throws Exception {
        try (MockedConstruction<Alert> alerts = mockConstruction(Alert.class)) {
            action.run();
        }
    }

    private void setInput(String input) throws Exception {
        ((TextField) getField("temperatureField")).setText(input);
    }

    private void selectUnit(String unitName) throws Exception {
        ComboBox<TemperatureUnit> combo = (ComboBox<TemperatureUnit>) getField("unitComboBox");

        TemperatureUnit unit = new TemperatureUnit(1, unitName);
        combo.getItems().setAll(unit);
        combo.getSelectionModel().select(unit);
    }

    @Test
    void applicationLoadsUnitsAndRecords() throws Exception {
        verify(unitDAO).findAll();
        verify(recordDAO).findAll();
    }

    @Test
    void loadUnitsHandlesEmptyList() throws Exception {
        when(unitDAO.findAll()).thenReturn(List.of());

        runOnFxThread(() -> invoke("loadUnits"));

        ComboBox<?> combo = (ComboBox<?>) getField("unitComboBox");

        assertTrue(combo.getItems().isEmpty());
        assertNull(combo.getSelectionModel().getSelectedItem());
    }

    @Test
    void loadUnitsHandlesDatabaseException() throws Exception {
        when(unitDAO.findAll())
                .thenThrow(new RuntimeException("Database unavailable"));

        runOnFxThread(() -> runWithMockedAlerts(() -> invoke("loadUnits")));
    }

    @Test
    void loadRecordsHandlesDatabaseException() throws Exception {
        when(recordDAO.findAll())
                .thenThrow(new RuntimeException("Database unavailable"));

        runOnFxThread(() -> runWithMockedAlerts(() -> invoke("loadRecords")));
    }

    @Test
    void emptyInputDisplaysWarning() throws Exception {
        runOnFxThread(() -> {
            setInput("   ");

            runWithMockedAlerts(() -> invoke("calculateAndSave"));

            verify(recordDAO, never()).insert(any());
        });
    }

    @Test
    void invalidNumericInputDisplaysWarning() throws Exception {
        runOnFxThread(() -> {
            setInput("not-a-number");

            runWithMockedAlerts(() -> invoke("calculateAndSave"));

            verify(recordDAO, never()).insert(any());
        });
    }

    @Test
    void missingSelectedUnitDisplaysWarning() throws Exception {
        runOnFxThread(() -> {
            setInput("25");

            ComboBox<?> combo = (ComboBox<?>) getField("unitComboBox");

            combo.getSelectionModel().clearSelection();

            runWithMockedAlerts(() -> invoke("calculateAndSave"));

            verify(recordDAO, never()).insert(any());
        });
    }

    @Test
    void infiniteInputIsRejected() throws Exception {
        runOnFxThread(() -> {
            setInput("Infinity");
            selectUnit("Celsius");

            runWithMockedAlerts(() -> invoke("calculateAndSave"));

            verify(recordDAO, never()).insert(any());
        });
    }

    @Test
    void nanInputIsRejected() throws Exception {
        runOnFxThread(() -> {
            setInput("NaN");
            selectUnit("Celsius");

            runWithMockedAlerts(() -> invoke("calculateAndSave"));

            verify(recordDAO, never()).insert(any());
        });
    }

    @Test
    void negativeKelvinIsRejected() throws Exception {
        runOnFxThread(() -> {
            setInput("-1");
            selectUnit("Kelvin");

            runWithMockedAlerts(() -> invoke("calculateAndSave"));

            verify(recordDAO, never()).insert(any());
        });
    }

    @Test
    void validCelsiusInputIsSaved() throws Exception {
        when(recordDAO.findAll()).thenReturn(List.of());

        runOnFxThread(() -> {
            setInput("25");
            selectUnit("Celsius");

            invoke("calculateAndSave");

            Label result = (Label) getField("celsiusResult");
            assertTrue(result.getText().contains("25.00"));

            verify(recordDAO).insert(any(TemperatureRecord.class));
        });
    }

    @Test
    void validFahrenheitInputIsSaved() throws Exception {
        when(recordDAO.findAll()).thenReturn(List.of());

        runOnFxThread(() -> {
            setInput("32");
            selectUnit("Fahrenheit");

            invoke("calculateAndSave");

            Label result = (Label) getField("celsiusResult");
            assertTrue(result.getText().contains("0.00"));

            verify(recordDAO).insert(any(TemperatureRecord.class));
        });
    }

    @Test
    void validKelvinInputIsSaved() throws Exception {
        when(recordDAO.findAll()).thenReturn(List.of());

        runOnFxThread(() -> {
            setInput("273.15");
            selectUnit("Kelvin");

            invoke("calculateAndSave");

            Label result = (Label) getField("celsiusResult");
            assertTrue(result.getText().contains("0.00"));

            verify(recordDAO).insert(any(TemperatureRecord.class));
        });
    }

    @Test
    void savingFailureDisplaysError() throws Exception {
        doThrow(new RuntimeException("Insert failed"))
                .when(recordDAO).insert(any(TemperatureRecord.class));

        runOnFxThread(() -> {
            setInput("25");
            selectUnit("Celsius");

            runWithMockedAlerts(() -> invoke("calculateAndSave"));
        });
    }

    @Test
    void unknownUnitIsRejectedByCanonicalUnit() throws Exception {
        runOnFxThread(() -> {
            Method method = Main.class.getDeclaredMethod(
                    "canonicalUnit", String.class);
            method.setAccessible(true);

            assertThrows(
                    java.lang.reflect.InvocationTargetException.class,
                    () -> method.invoke(mainApp, "Unknown"));
        });
    }

    @Test
    void localizedUnitHandlesKnownAndUnknownUnits() throws Exception {
        Method method = Main.class.getDeclaredMethod(
                "localizedUnit", String.class);
        method.setAccessible(true);

        runOnFxThread(() -> {
            assertNotNull(method.invoke(mainApp, "Celsius"));
            assertNotNull(method.invoke(mainApp, "Fahrenheit"));
            assertNotNull(method.invoke(mainApp, "Kelvin"));
            assertEquals("Unknown", method.invoke(mainApp, "Unknown"));
        });
    }
}
