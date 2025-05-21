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

public class GameOverLose extends Application{

	    private Label gameOver;
	    private Label loser;
	    private Pane emojiPane = new Pane();
	    private Random random = new Random();

	    @Override
	    public void start(Stage primaryStage) {
	        // Background image
	        Image backgroundImage = new Image(getClass().getResource("BackgroundGameMainScene.png").toExternalForm());
	        ImageView imageView = new ImageView(backgroundImage);
	        imageView.setPreserveRatio(false);
	        imageView.setSmooth(true);

	        // Labels
	        gameOver = new Label("GAME OVER");
	        gameOver.setFont(Font.font("Elephant", 60));
	        gameOver.setStyle("-fx-text-fill: #E97451;");

	        loser = new Label("YOU LOSE 😞");
	        loser.setFont(Font.font("Elephant", 50));
	        loser.setStyle("-fx-text-fill: #E97451;");

	        VBox textBox = new VBox(20, gameOver, loser);
	        textBox.setAlignment(Pos.CENTER);

	        // Root layout
	        StackPane root = new StackPane(imageView, emojiPane, textBox);

	        Scene scene = new Scene(root);
	        imageView.fitWidthProperty().bind(scene.widthProperty());
	        imageView.fitHeightProperty().bind(scene.heightProperty());
	        emojiPane.prefWidthProperty().bind(scene.widthProperty());
	        emojiPane.prefHeightProperty().bind(scene.heightProperty());

	        // Emoji animation on hover
	        loser.setOnMouseEntered(e -> launchSadEmojis());

	        primaryStage.setScene(scene);
	        primaryStage.setTitle("Game Over - You Lose");
	        primaryStage.show();
	    }

	    private void launchSadEmojis() {
	    	for (int i = 0; i < 20; i++) {
	    	    Image emojiImage = new Image(getClass().getResource("emoji.png").toExternalForm());
	    	    ImageView emoji = new ImageView(emojiImage);
	    	    emoji.setFitWidth(30);
	    	    emoji.setFitHeight(30);
	    	    emoji.setLayoutX(random.nextDouble() * emojiPane.getWidth());
	    	    emoji.setLayoutY(-30);
	    	    emojiPane.getChildren().add(emoji);

	    	    TranslateTransition fall = new TranslateTransition(Duration.seconds(2 + random.nextDouble()), emoji);
	    	    fall.setFromY(0);
	    	    fall.setToY(emojiPane.getHeight() + 50);
	    	    fall.setOnFinished(e -> emojiPane.getChildren().remove(emoji));
	    	    fall.play();
	    	}
	    }
}
