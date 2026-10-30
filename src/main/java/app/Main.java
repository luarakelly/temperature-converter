package app;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import app.controllers.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        ResourceBundle messages = ResourceBundle.getBundle("i18n.MessagesBundle", Locale.US);
        FXMLLoader loader = new FXMLLoader(
                Main.class.getResource("/fxml/main.fxml"), messages);
        Parent root = loader.load();

        MainController controller = loader.getController();
        stage.titleProperty().bind(controller.titleProperty());
        stage.setScene(new Scene(root, 850, 700));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
