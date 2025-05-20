package application;
import model.player.Player;
import model.card.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.Colour;
import engine.board.*;
import exception.GameException;
import exception.InvalidCardException;
import engine.*;
import model.card.standard.*;

import java.io.IOException;
import java.util.ArrayList;

public class MainSceneController {
    @FXML
    private AnchorPane rootPane;
    @FXML
    private TextField playerNameField;
    @FXML
    private ImageView cardSelected;
    private String playerName;
    private Game game;
    

    // 3) onAction for your Confirm button
    @FXML
    public void btnConfirm(ActionEvent event)  throws IOException, GameException,  InterruptedException {
        // read & store the name (default to "Player" if empty)
        playerName = playerNameField.getText().trim();
        if (playerName.isEmpty()) playerName = "User404";
        game = new Game(playerName);
        game.getBoard().getTrack();
        
        ////////////////////////////////////////

        // now switch scenes, preserving window size
        Stage stage = (Stage) rootPane.getScene().getWindow();
        double oldWidth  = stage.getWidth();
        double oldHeight = stage.getHeight();

        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/application/Scene2.fxml")
        );
        AnchorPane newRoot = loader.load();

        Scene2Controller ctrl = loader.getController();
        ctrl.initData(game);
        ctrl.setPlayerName(playerName);
        Scene newScene = new Scene(newRoot, oldWidth, oldHeight);
        stage.setScene(newScene);
        //////////////////////////////////////////////////////
                         

//    @FXML
//    public void cardSelected4(MouseEvent event) throws IOException, InvalidCardException{
//    	//game.getPlayers().get(0).getHand().get(3);
//    	game.selectCard(game.getPlayers().get(0).getHand().get(3));
//    	System.out.print(game.getPlayers().get(0).getHand().get(3).getName());
//    }

        
       
        ////////////////////////////////////////////
//        Player player1 = new Player(playerName,Colour.BLUE); // we need to pass getHand()
//        Deck.loadCardPool(game.getBoard(), game);
//        player1.setHand(Deck.drawCards());
//        // Check if the marbles are there: System.out.println(player1.getMarbles().isEmpty());
//        //Check if the hand is empty: System.out.print(player1.getHand().isEmpty());
//       
//        for(int i = 0; i < player1.getMarbles().size(); i++) {   
//        	System.out.println(player1.getMarbles().get(i).getColour());
//        }
//        // make sure there are 4 cards
        	
//        for(int i = 0; i < game.getPlayers().get(0).getHand().size(); i++) {   
//        	System.out.println(game.getPlayers().get(0).getHand().get(i).getName());
        	//Standard Omar  = (Standard) (game.getPlayers().get(0).getHand().get(i));
        	//System.out.print(Omar.getSuit());
        }

      
    }

