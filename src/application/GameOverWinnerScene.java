package application;

import java.util.Random;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GameOverWinnerScene extends Application{
	 private Label gameOver;
	    private Label winner;
	    private Pane confettiPane = new Pane();
	    private Random random = new Random();

public void start(Stage primaryStage) {
    // Background Image
    Image backgroundImage = new Image(getClass().getResource("BackgroundGameMainScene.png").toExternalForm());
    ImageView imageView = new ImageView(backgroundImage);
    imageView.setPreserveRatio(false);
    imageView.setSmooth(true);

    // Labels
    gameOver = new Label("GAME OVER");
    gameOver.setFont(Font.font("Elephant", 60));
    gameOver.setStyle("-fx-text-fill: #E97451;");

    winner = new Label("CONGRATULATIONS");
    winner.setFont(Font.font("Elephant", 50));
    winner.setStyle("-fx-text-fill: #E97451;");

    VBox textBox = new VBox(20, gameOver, winner);
    textBox.setAlignment(Pos.CENTER);

    // StackPane layout
    StackPane root = new StackPane(imageView, confettiPane, textBox);

    Scene scene = new Scene(root);
    imageView.fitWidthProperty().bind(scene.widthProperty());
    imageView.fitHeightProperty().bind(scene.heightProperty());
    confettiPane.prefWidthProperty().bind(scene.widthProperty());
    confettiPane.prefHeightProperty().bind(scene.heightProperty());

    // Hover event to start confetti
    gameOver.setOnMouseEntered(e -> launchConfetti());
    winner.setOnMouseEntered(e -> launchConfetti());

    primaryStage.setScene(scene);	
    primaryStage.setTitle("Game Over with Confetti Animation");
    primaryStage.show();
}

private void launchConfetti() {
    for (int i = 0; i < 30; i++) {
        Circle confetti = new Circle(5);
        confetti.setFill(Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
        confetti.setLayoutX(random.nextDouble() * confettiPane.getWidth());
        confetti.setLayoutY(-10); // Start above the view
        confettiPane.getChildren().add(confetti);

        TranslateTransition fall = new TranslateTransition(Duration.seconds(2 + random.nextDouble()), confetti);
        fall.setFromY(0);
        fall.setToY(confettiPane.getHeight() + 20);
        fall.setOnFinished(e -> confettiPane.getChildren().remove(confetti));
        fall.play();
    }
}
}
