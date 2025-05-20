package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // byft7 el FXML file bas mn scenebuilder 3ltol hena
        Parent root = FXMLLoader.load(getClass().getResource("/application/MainScene.fxml"));

        // Creates and sets the scene
        Scene scene = new Scene(root, 1200,700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Jackaroo");

        // Shows the stage
        primaryStage.show();
        
        //da ya basha bygeb el css (styling) lel main windows
        scene.getStylesheets()
        .add(getClass().getResource("/application/loginPage.css").toExternalForm());
     
    }

    public static void main(String[] args) {
        launch(args);
    }
}
