package application;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import engine.Game;
import engine.board.Board;
import engine.board.Cell;
import engine.board.SafeZone;
import exception.GameException;
import exception.IllegalMovementException;
import exception.InvalidCardException;
import exception.InvalidMarbleException;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import model.card.Card;
import model.card.standard.Ace;
import model.card.standard.Standard;
import model.card.wild.*;
import model.player.Marble;
import model.player.Player;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import model.Colour; 


public class Scene2Controller {
    private Game game;
    private int lastSelectedCard=-1; // to not give not intialized exception
    @FXML private Button playBtn;
    @FXML private Pane marblePane;
    @FXML private Pane cardDropPane;
    @FXML private Pane cardDescriptionZone;
    @FXML private ImageView CardPlayer1;
    @FXML private ImageView CardPlayer2;
    @FXML private ImageView CardPlayer3;
    @FXML private Label PlayerName;
    @FXML private ImageView CardPlayer4;
    @FXML private Circle player0Marble1, player0Marble2, player0Marble3, player0Marble4;
    @FXML private Circle player1Marble1, player1Marble2, player1Marble3, player1Marble4;
    @FXML private Circle player2Marble1, player2Marble2, player2Marble3, player2Marble4;
    @FXML private Circle player3Marble1, player3Marble2, player3Marble3, player3Marble4;
    @FXML private Circle marble_1;  
    @FXML private StackPane boardStack;
    @FXML private Label Cpu1Name;
    @FXML private Label Cpu2Name; 
    @FXML private Label Cpu3Name; 
    private final boolean[] cellOccupied = new boolean[101];
    private final int[] cellOwner = new int[101];
    private final Map<Circle, Integer> marblePositions = new HashMap<>(); 
    private final List<Circle>[] playerMarbles = new List[4]; // For 4 players
    private Circle currentlyHighlighted = null;
    private List<Label> playerLabels;
    private final boolean[] playedSlots = new boolean[4];

public void initData(Game game) throws GameException , InterruptedException {
    this.game = game;
    initializeMarbles();
    LoadPlayerCardsimgs();
    initialize(); // Ensure marbles are initialized first
    intializeCellsBooleans();
    }

//public void startGameLoop() throws GameException , InterruptedException {
//	 	System.out.println("The First turn started");
//	    proceedToNextTurn();
//}

/** Decide whose turn it is, do their move, then schedule the next turn. 
 * @throws GameException 
 * @throws InterruptedException */
private void proceedToNextTurn() throws GameException, InterruptedException {
    if (game.checkWin() != null) {
        showGameOver();
        return;
    }

    int currentPlayerIndex = getCurrentPlayerIndex();

    // 1) remove “highlight” from everyone
    playerLabels.forEach(lbl -> lbl.getStyleClass().remove("highlight"));

    // 2) add “highlight” to the active one
    Label active = playerLabels.get(currentPlayerIndex);
    active.getStyleClass().add("highlight");
    FadeTransition pulse = new FadeTransition(Duration.seconds(0.6), active);
    pulse.setFromValue(1.0);
    pulse.setToValue(0.6);
    pulse.setCycleCount(4);
    pulse.setAutoReverse(true);
    pulse.play();

    System.out.println("This player are going to play: " + currentPlayerIndex);
    if (currentPlayerIndex == 0) {
        enableHumanTurn();
    } else {
        cpuPlayAndAdvance(currentPlayerIndex);
    }
}

private int getCurrentPlayerIndex() {
    Colour activeColor = game.getActivePlayerColour();
    List<Player> players = game.getPlayers();
    for (int i = 0; i < players.size(); i++) {
        if (players.get(i).getColour().equals(activeColor)) {
            return i;
        }
    }
    throw new IllegalStateException("Active player color not found: " + activeColor);
}

private void enableHumanTurn() {
    // Show play button and enable human interaction
    playBtn.setDisable(false);
}


private void cpuPlayAndAdvance(int cpuIndex) throws GameException {
    // 1) Let the engine play the CPU’s turn:
    game.playPlayerTurn();
    game.endPlayerTurn();

    System.out.println("");
    System.out.println("Cpu Number will play: "+cpuIndex);
    // 2) Grab the Card object from the fire‑pit:
    Card played = game.getFirePit().getLast();
    Standard cpuCard = (Standard) played;
    int rank = cpuCard.getRank();

    // 3) Load its image
    String path = imagePathForCard(played);
    InputStream is = getClass().getResourceAsStream(path);
    if (is == null) {
        System.err.println("CPU card image not found: " + path);
        return;
    }
    Image img = new Image(is);

    // 4) Create and show the ImageView
    ImageView iv = new ImageView(img);
    iv.setPreserveRatio(true);
    iv.setFitWidth(130);
    iv.setFitHeight(130);
    addInFirePit(iv); // shows the card

    // 5) Move the marble
    int marbleIndex = 0;  
    moveMarbleBySteps(cpuIndex, marbleIndex, rank);

    // 6) Wait before continuing
    PauseTransition pause = new PauseTransition(Duration.seconds(5));
    pause.setOnFinished(e -> {
        try {
            proceedToNextTurn(); // only proceed *after* the pause
        } catch (GameException | InterruptedException ex) {
            ex.printStackTrace();
        }
    });
    pause.play(); // this returns immediately, doesn’t block UI
}                           

private String imagePathForCard(Card c) {
    if (c instanceof Standard) {
        Standard std = (Standard) c;
        return String.format(
            "/cards/collection/%s/card_%d_%s.png",
            std.getSuit().name().toLowerCase(),
            std.getRank(),
            std.getSuit().name().toLowerCase()
        );
    } else {
        switch (c.getName()) {
            case "MarbleSaver":  return "/cards/collection/special/marble_saver.png";
            case "MarbleBurner": return "/cards/collection/special/marble_burner.png";
            default: throw new IllegalArgumentException("Unknown wild card: " + c.getName());
        }
    }
}

private ImageView getCpuImageView(int cpuIndex) {
    switch(cpuIndex) {
      case 1: return CardPlayer2;
      case 2: return CardPlayer3;
      case 3: return CardPlayer4;
      default: throw new IllegalArgumentException("Invalid CPU index");
    }
}


private void showGameOver() {
    // Implement game over display
}

@FXML
public void setPlayerName(String name) {
    PlayerName.setText(name);
}   

private void intializeCellsBooleans() {
	 Arrays.fill(cellOccupied, false);
}

private void refillHand() {
    // 1) Draw 4 new cards into the model
    //    (replace with your actual draw logic)
//    game.drawCards(game.getPlayers().get(0), 4);

    // 2) Refresh the images
    LoadPlayerCardsimgs();

    // 3) Clear played flags & re-enable all slots
    ImageView[] ivs = { CardPlayer1, CardPlayer2, CardPlayer3, CardPlayer4 };
    for (int i = 0; i < 4; i++) {
        playedSlots[i] = false;
        ivs[i].setDisable(false);
        ivs[i].setOpacity(1.0);
    }
}

private void loadCardIntoView(ImageView iv, Card c) {
    String path;
    if (c instanceof Standard) {
        Standard std = (Standard) c;
        int rank = std.getRank();
        String suit = std.getSuit().name().toLowerCase();
        path = String.format("/cards/collection/%s/card_%d_%s.png", suit, rank, suit);
    } else {
        // Wild cards
        String name = c.getName();
        if ("MarbleSaver".equals(name))      path = "/cards/collection/special/marble_saver.png";
        else if ("MarbleBurner".equals(name)) path = "/cards/collection/special/marble_burner.png";
        else                                  path = null;
    }

    if (path != null) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is != null) {
                iv.setImage(new Image(is));
            } else {
                System.err.println("Image not found: " + path);
                iv.setImage(null);
            }
        } catch (IOException e) {
            e.printStackTrace();
            iv.setImage(null);
        }
    } else {
        iv.setImage(null);
    }
    iv.setFitWidth(130);
    iv.setFitHeight(130);
    iv.setPreserveRatio(true);
}

private void LoadPlayerCardsimgs() {
    List<Card> hand = game.getPlayers().get(0).getHand();
    ImageView[] ivs = { CardPlayer1, CardPlayer2, CardPlayer3, CardPlayer4 };

    
    for (int i = 0; i < ivs.length; i++) { // Always reset all ImageViews
        ImageView iv = ivs[i];
        if (i < hand.size()) {
            Card c = hand.get(i);
            
            if (c instanceof Standard) {
                Standard std = (Standard) c;
                int rank = std.getRank();
                String suit = std.getSuit().name().toLowerCase();
                String path = "";
                    // Default path for standard cards
                    path = String.format(
                        "/cards/collection/%s/card_%d_%s.png",
                        suit, rank, suit
                    );

                try {
                    InputStream is = getClass().getResourceAsStream(path);
                    if (is != null) {
                        Image img = new Image(is);
                        iv.setImage(img);
                    } else {
                        System.err.println("Image not found: " + path);
                        iv.setImage(null); // Clear if image missing
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    iv.setImage(null);
                }
            }
            
            
            else {
                Wild std = (Wild) c;
                String path = "";
            	 if (c.getName().equals("MarbleSaver")) {
                    path = "/cards/collection/special/marble_saver.png";
                 } 
            	 else if (c.getName().equals("MarbleBurner")) {
                    path = "/cards/collection/special/marble_burner.png";
                 }

                 try {
                     InputStream is = getClass().getResourceAsStream(path);
                     if (is != null) {
                         Image img = new Image(is);
                         iv.setImage(img);
                     } else {
                         System.err.println("Image not found: " + path);
                         iv.setImage(null); // Clear if image missing
                     }
                 } catch (Exception e) {
                     e.printStackTrace();
                     iv.setImage(null);
                 }
            }
        } else {
            iv.setImage(null);
        }

        // Set image properties
        iv.setFitWidth(130);
        iv.setFitHeight(130);
        iv.setPreserveRatio(true);
}}

private void initializeMarbles() {
    List<Player> players = game.getPlayers();
    
    // Player 0 (Bottom-Right)
    setMarbleColor(players.get(0).getColour(), 
                  player0Marble1, player0Marble2, player0Marble3, player0Marble4);
    
    // Player 1 (Bottom-Left)
    setMarbleColor(players.get(1).getColour(), 
                  player1Marble1, player1Marble2, player1Marble3, player1Marble4);
    
    // Player 2 (Top-Left)
    setMarbleColor(players.get(2).getColour(), 
                  player2Marble1, player2Marble2, player2Marble3, player2Marble4);
    
    // Player 3 (Top-Right)
    setMarbleColor(players.get(3).getColour(), 
                  player3Marble1, player3Marble2, player3Marble3, player3Marble4);
}

private final HashMap<Colour, Color> colorMap = new HashMap<Colour, Color>() {{
    put(Colour.RED, Color.RED);
    put(Colour.BLUE, Color.BLUE);
    put(Colour.GREEN, Color.GREEN);
    put(Colour.YELLOW, Color.YELLOW);
}};

private void setMarbleColor(Colour colour, Circle... marbles) {
    Color fxColor = colorMap.get(colour);
    //System.out.print("Setting color: " + colour.name());
    
    for(Circle marble : marbles) {
        marble.setFill(fxColor);
        marble.setStroke(Color.BLACK);
    }
}

@FXML
private void selectMarble(MouseEvent e) throws InvalidMarbleException {
    Circle clicked = (Circle) e.getSource();
    String fxId = clicked.getId();

    if (!fxId.startsWith("player0Marble")) return;

    try {
        // Extract marble number safely
        String numberPart = fxId.substring("player0Marble".length());
        int marbleNumber = Integer.parseInt(numberPart);
        int marbleIndex = marbleNumber - 1; // Convert to 0-based index

        // Check if marble is on the board (position > 0)
        int currentPosition = marblePositions.getOrDefault(clicked, 0);
        if (currentPosition == 0) {
            System.out.println("Marble is at home - cannot select!");
            return;
        }

        Player me = game.getPlayers().get(0);
        List<Marble> marbles = me.getMarbles();

        // Validate index bounds
        if (marbleIndex < 0 || marbleIndex >= marbles.size()) {
            System.err.println("Invalid marble index: " + marbleIndex);
            return;
        }

        // Toggle selection logic
        if (clicked.equals(currentlyHighlighted)) {
            game.deselectAll();
            clicked.setStroke(Color.BLACK);
            clicked.setStrokeWidth(1);
            currentlyHighlighted = null;
        } else {
            game.selectMarble(marbles.get(marbleIndex));
            if (currentlyHighlighted != null) {
                currentlyHighlighted.setStroke(Color.BLACK);
                currentlyHighlighted.setStrokeWidth(1);
            }
            clicked.setStroke(Color.PINK);  // Changed to green for better visibility
            clicked.setStrokeWidth(4);
            currentlyHighlighted = clicked;
        }
    } catch (NumberFormatException ex) {
        System.err.println("Invalid marble ID format: " + fxId);
    }
}

private void selectAndHighlight(int cardIndex, ImageView iv, MouseEvent event) throws InvalidCardException {
    // If you clicked the already-selected card, deselect it:
    // cardIndex is 1..4, so slot=cardIndex-1
	
    int slot = cardIndex - 1;
    if (playedSlots[slot]) {
        // ignore clicks on used cards
        return;
    }
    if (lastSelectedCard == cardIndex) {
        // 1) Descale it
        iv.setScaleX(1.0);
        iv.setScaleY(1.0);

        // 3) Reset state
        lastSelectedCard = 0;
        playBtn.setVisible(false);
        playBtn.setManaged(false);
        return;
    }

    switch (lastSelectedCard) {
        case 1: CardPlayer1.setScaleX(1.0); CardPlayer1.setScaleY(1.0); break;
        case 2: CardPlayer2.setScaleX(1.0); CardPlayer2.setScaleY(1.0); break;
        case 3: CardPlayer3.setScaleX(1.0); CardPlayer3.setScaleY(1.0); break;
        case 4: CardPlayer4.setScaleX(1.0); CardPlayer4.setScaleY(1.0); break;
    }

    // 2) Game logic: select the new card
    game.selectCard(game.getPlayers().get(0).getHand().get(cardIndex - 1));
    //System.out.println(game.getPlayers().get(0).getHand().get(cardIndex - 1).getName());

    // 3) Scale this one up
    iv.setScaleX(1.05);
    iv.setScaleY(1.05);

    // 4) Remember which card is selected now
    lastSelectedCard = cardIndex;

    // 5) Show the Play button
    playBtn.setVisible(true);
    playBtn.setManaged(true);
    
    // get your card’s text somehow
    
    String desc = game.getPlayers().getFirst().getSelectedCard().getDescription();;

    // clear any old description
    cardDescriptionZone.getChildren().clear();

    // create and configure label
    Label label = new Label(desc);
    label.setWrapText(true);
    label.setMaxWidth(cardDescriptionZone.getPrefWidth());
    label.getStyleClass().add("card-description");

    // 5) scale‑&‑bounce animation on hover
    ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), label);
    scaleUp.setToX(1.05);
    scaleUp.setToY(1.05);
    scaleUp.setInterpolator(Interpolator.EASE_OUT);

    ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), label);
    scaleDown.setToX(1.0);
    scaleDown.setToY(1.0);
    scaleDown.setInterpolator(Interpolator.EASE_IN);

    label.setOnMouseEntered(e -> {
        scaleDown.stop();
        scaleUp.playFromStart();
    });
    label.setOnMouseExited(e -> {
        scaleUp.stop();
        scaleDown.playFromStart();
    });

    // 6) optional tooltip for extra engagement
    Tooltip tt = new Tooltip("Click to learn more!");
    tt.setShowDelay(Duration.millis(300));
    Tooltip.install(label, tt);
    
    cardDescriptionZone.getChildren().add(label);
}

@FXML 
private void cardSelected4(MouseEvent event) throws InvalidCardException {
    ImageView iv = (ImageView) event.getSource();
	selectAndHighlight(4,iv,event);
}
@FXML
private void cardSelected3(MouseEvent event) throws InvalidCardException {
    ImageView iv = (ImageView) event.getSource();
	selectAndHighlight(3,iv,event);
}
@FXML
private void cardSelected2(MouseEvent event) throws InvalidCardException {
    ImageView iv = (ImageView) event.getSource();
	selectAndHighlight(2,iv,event);
}
@FXML
private void cardSelected1(MouseEvent event) throws InvalidCardException {
    ImageView iv = (ImageView) event.getSource();
	selectAndHighlight(1,iv,event);
}


@FXML
private void PlayCardSelected(MouseEvent event) {
    try {
        // 0) Figure out which slot was selected
        int slot = lastSelectedCard - 1;   // 0..3

        // 1) Immediately grab the rank from the model *before* we modify the UI or engine state
        Player me = game.getPlayers().get(0);
        Standard chosenCard = (Standard) me.getHand().get(slot);
        int rank = chosenCard.getRank();

        // 2) Let the engine process the move
        CardsPlayHandeler(rank);

        // 3) Move the marble in the UI
        moveForRank(0, rank);

        // 4) Now safely update the UI for the played card
        playedSlots[slot] = true;
        ImageView[] ivs = { CardPlayer1, CardPlayer2, CardPlayer3, CardPlayer4 };
        ImageView usedIv = ivs[slot];
        usedIv.setScaleX(1.0);
        usedIv.setScaleY(1.0);
        usedIv.setDisable(true);
        addInFirePit();
        flipPlayedCardOver();
        resetPlayButton();
        lastSelectedCard = 0;

        System.out.println("\nYou played a card with rank " + rank);

        // 5) Delay before next turn
        PauseTransition pause = new PauseTransition(Duration.seconds(5));
        pause.setOnFinished(e -> {
            try {
                proceedToNextTurn();
            } catch (GameException | InterruptedException ex) {
                ex.printStackTrace();
            }
        });
        pause.play();

    } catch (GameException ex) {
        ex.printStackTrace();
    }
}

private void CardsPlayHandeler(int rank) throws GameException {
    if (rank == 2 || rank == 3 || rank == 5 || rank == 6 || rank == 8 || rank == 9) {
        if (currentlyHighlighted != null) {
            // find which player & which marble index was highlighted
            for (int p = 0; p < playerMarbles.length; p++) {
                List<Circle> list = playerMarbles[p];
                int i = list.indexOf(currentlyHighlighted);
                if (i >= 0) {
                    // move that exact marble forward by `rank`
                    moveMarbleBySteps(p, i, rank);
                    game.playPlayerTurn();
                    game.endPlayerTurn();
                    return;
                }
            }
        } else {
            // no marble selected: show a warning or silently ignore
        	game.endPlayerTurn();
            System.out.println("Select a marble before playing a standard card.");
            return;
        }
    }
    // otherwise, special cards:
    SpecialCardsHandeler(rank);	
}

private void moveForRank(int playerIndex, int rank) throws GameException {
    if (Arrays.asList(2,3,5,6,8,9).contains(rank)) {
        if (currentlyHighlighted != null) {
            // find p,i of that Circle
            for (int p = 0; p < playerMarbles.length; p++) {
                int i = playerMarbles[p].indexOf(currentlyHighlighted);
                if (i >= 0) {
                    moveMarbleBySteps(p, i, rank);
                    return;
                }
            }
        }
        return;
    }

    switch(rank) {
      case 1:  // Ace
        if (currentlyHighlighted != null) {
          moveMarbleBySteps(0, playerMarbles[0].indexOf(currentlyHighlighted), 1);
        } else {
          moveMarbleNotOnBoard();
        }
        break;
      case 4:  moveBackSelected(4); break;
      case 10: moveForwardSelected(10); break;
      case 11: handleEleven();             break;
      case 13: moveKing();                 break;
      case 12: moveForwardSelected(12);    break;
      case 14: wildCardHandle(rank);           break;
      case 15: wildCardHandle(rank);       break;
    }
}

private void moveKing() {
	// TODO Auto-generated method stub
	
}

private void handleEleven() {
	// TODO Auto-generated method stub
	
}

private void moveForwardSelected(int i) {
	// TODO Auto-generated method stub
	
}

private void moveBackSelected(int i) {
	// TODO Auto-generated method stub
	
}

private void moveMarbleNotOnBoard() { // To base cell
    for (int i = 0; i < 4; i++) {
        Circle marble = playerMarbles[0].get(i);
        int currentPos = marblePositions.getOrDefault(marble, 0);
        
        // If marble is at home (position 0)
        if (currentPos == 0) {
            // Move it to the first board position (marble_1)
            moveMarbleBySteps(0, i, 1);
            return; // Exit after moving one marble
        }
    }
}


private void SpecialCardsHandeler(int rank) throws GameException {
	Player me = game.getPlayers().get(0);
   
	if (rank == 1) { // Ace
	    if (currentlyHighlighted != null) {
	    	moveMarbleBySteps(0,0,1);
	    	game.playPlayerTurn();
            game.endPlayerTurn();
	    }
	    else
	    	moveMarbleNotOnBoard();
	        game.playPlayerTurn();
	        game.endPlayerTurn();
    }
    // King
	
	else if (rank == 13) { // King
	    if (currentlyHighlighted != null) {
	    	moveMarbleBySteps(0,0,13);
	    	// Destroy marbles in path
	    	game.playPlayerTurn();
	    	game.endPlayerTurn();
	    }
	    else
	    	moveMarbleNotOnBoard();
	        game.playPlayerTurn();
	        game.fieldMarble();
	        game.endPlayerTurn();
	}
	
	// Four
	
	else if (rank ==4) {
		if(currentlyHighlighted != null) {
			moveMarbleBySteps(0,0,-4);	
			game.playPlayerTurn();
			game.endPlayerTurn();
		}
	}
	
	// Five: Moves any marble (yours or opponent's) forward 5 steps
	
	else if(rank==5) {
        if (currentlyHighlighted != null) {
            // find which player & which index
            for (int p = 0; p < playerMarbles.length; p++) {
                List<Circle> list = playerMarbles[p];
                int idx = list.indexOf(currentlyHighlighted);
                if (idx >= 0) {
                    // bingo! move that one
                    // first tell the engine
                    Marble modelMarble = game.getPlayers().get(p).getMarbles().get(idx);
                    game.playPlayerTurn();
                    game.endPlayerTurn();
                    // then update the UI
                    moveMarbleBySteps(p, idx, 5);
                    
                    // end the search
                    break;
                }
	}}
	
	// seven: to be implemented
	
	else if(rank==7) {
		
	}
	
	// Ten: Discard random card from next player + skip turn OR<br>• Standard 10-step move 
	// Here you need to display that the player got discarded
	else if(rank==10) {
	    if (currentlyHighlighted != null) {
	    	moveMarbleBySteps(0,0,12);
	    	game.playPlayerTurn();
	    	game.endPlayerTurn();
	    }
	    else
	    	game.playPlayerTurn();//gui indication is missing
	        game.endPlayerTurn();
	}
        
    // eleven Swap positions with any marble OR<br>• Standard 11-step move |
	else if (rank == 11) {
	    // 1) If they’ve already highlighted one marble and now click a second, do a swap
	    if (firstSwapCandidate != null && currentlyHighlighted != null 
	        && currentlyHighlighted != firstSwapCandidate) {
	        
	        // Find model marbles
	        Marble m1 = findModelMarbleForCircle(firstSwapCandidate);
	        Marble m2 = findModelMarbleForCircle(currentlyHighlighted);
	        // Swap them in the engine
	        game.playPlayerTurn();;
	        game.endPlayerTurn();
	        
	        // Swap their UI positions & tracking
	        swapCirclePositions(firstSwapCandidate, currentlyHighlighted);

	        // Cleanup
	        firstSwapCandidate.setStroke(Color.BLACK);
	        firstSwapCandidate.setStrokeWidth(1);
	        firstSwapCandidate = null;
	        currentlyHighlighted = null;
	        game.playPlayerTurn();
	        game.endPlayerTurn();
	    }
	    // 2) If no first candidate yet but they clicked one, mark it
	    else if (currentlyHighlighted != null && firstSwapCandidate == null) {
	        firstSwapCandidate = currentlyHighlighted;
	        firstSwapCandidate.setStroke(Color.ORANGE);
	        firstSwapCandidate.setStrokeWidth(4);
	        // wait for second click…
	    }
	    // 3) No swap: standard 11-step move if they click once and then hit Play
	    else if (currentlyHighlighted != null && firstSwapCandidate == null) {
	        // find which player/index
	        for (int p = 0; p < playerMarbles.length; p++) {
	            int idx = playerMarbles[p].indexOf(currentlyHighlighted);
	            if (idx >= 0) {
	                // move in engine & UI
	                Marble m = game.getPlayers().get(p).getMarbles().get(idx);
	                game.playPlayerTurn();
	                moveMarbleBySteps(p, idx, 11);
	                break;
	            }
	        }
	        game.playPlayerTurn();
	    }
	    else {
	        // nothing selected yet
	        System.out.println("Click one marble to move 11, or two to swap.");
	    }
	}
       
    // Queen:  Discard random card from random player + skip turn OR<br>• Standard 12-step move |
	else if(rank==12) {
		game.playPlayerTurn();
	}
        
        
	else if(rank==14 || rank == 15) {
		wildCardHandle(rank);
	}
	
}}


private void wildCardHandle(int rank) throws GameException {
	if(rank ==14) { //  Send opponent's marble back to their Home Zone |
		// Very hard to implement gui wise lesa
	}
	
	if(rank==15) { //Move your marble to random empty Safe Zone cell |
		game.playPlayerTurn();
		// Very hard to implement gui wise lesa
	}
}

@FXML
private void initialize() {
    // Initialize player marble lists
    playerMarbles[0] = Arrays.asList(player0Marble1, player0Marble2, player0Marble3, player0Marble4);
    playerMarbles[1] = Arrays.asList(player1Marble1, player1Marble2, player1Marble3, player1Marble4);
    playerMarbles[2] = Arrays.asList(player2Marble1, player2Marble2, player2Marble3, player2Marble4);
    playerMarbles[3] = Arrays.asList(player3Marble1, player3Marble2, player3Marble3, player3Marble4);
    
    // Initialize positions
    for (List<Circle> marbles : playerMarbles) {
        for (Circle marble : marbles) {
            if (marble != null) {
                marblePositions.put(marble, 0);
            }
        }
    }
    playerLabels = Arrays.asList(
            PlayerName,
            Cpu1Name,
            Cpu2Name,
            Cpu3Name
        );
}

// In this method we want to move circle of player0Marble1 to marble_1 
@FXML
private void moveMarbleToCell() {
    // Example: Move player 0's first marble to cell 1 (marble_1)
    Circle playerMarble = player0Marble1; // The marble to move
    Circle targetCell = marble_1;        // The target cell

    // Get the center of the target cell in its local coordinates
    Bounds targetBounds = targetCell.getBoundsInLocal();
    Point2D targetCenterLocal = new Point2D(
        targetBounds.getCenterX(),
        targetBounds.getCenterY()
    );

    // Convert the target cell's center to scene coordinates
    Point2D targetCenterScene = targetCell.localToScene(targetCenterLocal);

    // Convert scene coordinates to the player marble's parent coordinate system
    Parent parent = playerMarble.getParent();
    Point2D targetCenterInParent = parent.sceneToLocal(targetCenterScene);

    // Update the player marble's position
    playerMarble.setLayoutX(targetCenterInParent.getX());
    playerMarble.setLayoutY(targetCenterInParent.getY());

    // Update game state to mark the cell as occupied
    int cellIndex = 1; // Since we're moving to marble_1
    cellOccupied[cellIndex] = true;
    cellOwner[cellIndex] = 0; // Assuming player 0 is the owner
}

//Example usage: Move player 0's first marble 3 steps
//moveMarbleBySteps(0, 0, 3);
public void moveMarbleBySteps(int playerNumber, int marbleIndex, int steps) {
    // Validate inputs
    if (playerNumber < 0 || playerNumber >= 4) return;
    if (marbleIndex < 0 || marbleIndex >= 4) return;
    Circle marble = playerMarbles[playerNumber].get(marbleIndex);
    if (marble == null) return;

    // Get current position
    int currentPosition = marblePositions.getOrDefault(marble, 0);

    // Calculate new position
    int newPosition = currentPosition + steps;
    if (newPosition > 100) newPosition = 100;
    if (newPosition < 0) newPosition = 0;

    // Get target cell
    Circle targetCell = (Circle) boardStack.lookup("#marble_" + newPosition);
    if (targetCell == null) return;

    // Get target cell's coordinates using moveMarbleToCell's logic
    Bounds targetBounds = targetCell.getBoundsInLocal();
    Point2D targetCenterLocal = new Point2D(
        targetBounds.getCenterX(),
        targetBounds.getCenterY()
    );

    // Convert to scene coordinates
    Point2D targetCenterScene = targetCell.localToScene(targetCenterLocal);

    // Convert to the marble's parent coordinate system
    Parent parent = marble.getParent();
    Point2D targetCenterInParent = parent.sceneToLocal(targetCenterScene);

    // Update position (same as moveMarbleToCell)
    marble.setLayoutX(targetCenterInParent.getX());
    marble.setLayoutY(targetCenterInParent.getY());
    marble.toFront();

    // Update game state
    if (currentPosition > 0) {
        cellOccupied[currentPosition] = false;
        cellOwner[currentPosition] = -1;
    }
    if (newPosition > 0) {
        cellOccupied[newPosition] = true;
        cellOwner[newPosition] = playerNumber;
    }

    // Update tracking
    marblePositions.put(marble, newPosition);
}

/** Places the card in the firepit */
private void addInFirePit() {
    ImageView[] ivs = { CardPlayer1, CardPlayer2, CardPlayer3, CardPlayer4 };
    ImageView selectedIv = ivs[lastSelectedCard - 1];

    ImageView placed = new ImageView(selectedIv.getImage());
    placed.setPreserveRatio(true);
    placed.setFitWidth(selectedIv.getFitWidth());
    placed.setFitHeight(selectedIv.getFitHeight());

    cardDropPane.getChildren().clear();
    // Center it in the pane:
    placed.setLayoutX((cardDropPane.getPrefWidth() - placed.getFitWidth()) / 2);
    placed.setLayoutY((cardDropPane.getPrefHeight() - placed.getFitHeight()) / 2);

    cardDropPane.getChildren().add(placed);
}

private void addInFirePit(ImageView ivToDrop) {
    ImageView placed = new ImageView(ivToDrop.getImage());
    placed.setPreserveRatio(true);
    placed.setFitWidth(ivToDrop.getFitWidth());
    placed.setFitHeight(ivToDrop.getFitHeight());

    cardDropPane.getChildren().clear();
    placed.setLayoutX((cardDropPane.getPrefWidth() - placed.getFitWidth()) / 2);
    placed.setLayoutY((cardDropPane.getPrefHeight() - placed.getFitHeight()) / 2);
    cardDropPane.getChildren().add(placed);
}

/** Flips the played card on it's back */
private void flipPlayedCardOver() {
    ImageView[] ivs = { CardPlayer1, CardPlayer2, CardPlayer3, CardPlayer4 };
    ImageView sel  = ivs[lastSelectedCard - 1];
    InputStream backIs = getClass().getResourceAsStream(
        "/cards/backcards/jackaroo.png"
    );
    if (backIs != null) {
        Image back = new Image(backIs);
        sel.setImage(back);
        sel.setFitWidth(130);
        sel.setFitHeight(130);
        sel.setPreserveRatio(true);
    }
    else {
        System.err.println("Could not load backofcard image");
    }
}

/** Hides & disables the play button again. */
private void resetPlayButton() {
    playBtn.setVisible(false);
    playBtn.setManaged(false);
}


// m4 3arf dool b3mlo eh

//Remember to clear this on both successful swap or cancel
private Circle firstSwapCandidate = null;

/** Finds which model-Marble corresponds to a given UI circle. */
private Marble findModelMarbleForCircle(Circle ui) {
 for (int p = 0; p < playerMarbles.length; p++) {
     int idx = playerMarbles[p].indexOf(ui);
     if (idx >= 0) {
         return game.getPlayers().get(p).getMarbles().get(idx);
     }
 }
 return null;
}

/** Swaps two marble-circles both on-screen and in your tracking. */
private void swapCirclePositions(Circle c1, Circle c2) {
 // Swap layout coords
 double x1 = c1.getLayoutX(), y1 = c1.getLayoutY();
 c1.setLayoutX(c2.getLayoutX());
 c1.setLayoutY(c2.getLayoutY());
 c2.setLayoutX(x1);
 c2.setLayoutY(y1);

 // Swap their positions in your map
 int pos1 = marblePositions.get(c1), pos2 = marblePositions.get(c2);
 marblePositions.put(c1, pos2);
 marblePositions.put(c2, pos1);

 c1.toFront();
 c2.toFront();
}
}

