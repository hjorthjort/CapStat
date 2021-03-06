package capstat.view;

import capstat.application.MatchController;
import capstat.infrastructure.eventbus.EventBus;
import capstat.infrastructure.eventbus.NotifyEventListener;
import capstat.model.match.Match;
import capstat.model.match.MatchFactory;
import capstat.model.user.UserFactory;
import capstat.model.user.UserLedger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Jakob on 18/05/15.
 * @author Jakob
 * Class to update and initialize the MatchView
 */
public class MatchViewController implements NotifyEventListener, Initializable {
    EventBus eb = EventBus.getInstance();
    Match match = MatchFactory.createDefaultMatch();
    MatchController mc = new MatchController(match);
    String winner = "";
    @FXML Button hitButton, missButton,startRankedMatchButton, startUnrankedMatchButton, menuButton;
    @FXML Circle glass1, glass2, glass3, glass4, glass5, glass6, glass7;
    @FXML Pane p1Pane, p2Pane, mainPane, matchOverPane, preMatchPane;
    @FXML Label hitLabel, missLabel, duelLabel, p1Name, p2Name, p1Rank, p2Rank, p1Rounds, p2Rounds, winnerLabel, nickname1Label, nickname2Label;
    @FXML TextField setPlayer1Field, setPlayer2Field;
    Background activeBackground = new Background(new BackgroundFill(Color.CORNFLOWERBLUE, CornerRadii.EMPTY, Insets.EMPTY));
    Background inactiveBackground = new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY));

    /**
     * Sets the default behaviour and display of elements initializing the view.
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources){
        mainPane.setBackground(new Background(new BackgroundFill(Color
                .LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        eb.addNotifyEventListener(Match.HIT_RECORDED, this);
        eb.addNotifyEventListener(Match.MISS_RECORDED,this);
        eb.addNotifyEventListener(Match.DUEL_ENDED, this);
        eb.addNotifyEventListener(Match.ROUND_ENDED, this);
        eb.addNotifyEventListener(Match.MATCH_ENDED, this);
        nickname1Label.setVisible(false);
        nickname2Label.setVisible(false);
        hitLabel.setVisible(false);
        missLabel.setVisible(false);
        duelLabel.setVisible(false);
        matchOverPane.setVisible(false);
        p1Pane.setVisible(false);
        p2Pane.setVisible(false);
        mainPane.setVisible(false);
        hitButton.setFocusTraversable(false);
        missButton.setFocusTraversable(false);
        preMatchPane.setVisible(true);
        startRankedMatchButton.setDisable(false);
        startUnrankedMatchButton.setDisable(false);
        Platform.runLater(() -> {
            missButton.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.G), () -> {
                missPressed();
            });
        });
        Platform.runLater(() -> {
            hitButton.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.SPACE), () -> {
                hitPressed();
            });
        });
        Platform.runLater(() -> {
            menuButton.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.ESCAPE), () -> {
                returnToMenu();
            });
        });
    }

    /**
     * Sends a notification the EventBus when the Menu-Button is pressed.
     *
     */
    @FXML private void returnToMenu(){
        this.tearDown();
        eb.notify(TopWindow.SETSCENE_MAIN);
    }

    /**
     * Tells the match to register a miss when the Miss-button is pressed, using the MatchController.
     * Also sets the correct labels to display
     */
    @FXML private void missPressed() {
        if(match.isOngoing()) {
            hitLabel.setVisible(false);
            missLabel.setVisible(true);
            duelLabel.setVisible(false);
            mc.recordMiss();
        }
    }

    /**
     * Tells the match to register a hit when the Hit-button is pressed, using the MatchController.
     * Also sets the correct labels to display
     */
    @FXML private void hitPressed(){
        if(match.isOngoing()) {
            missLabel.setVisible(false);
            hitLabel.setVisible(true);
            duelLabel.setVisible(true);
            mc.recordHit();
        }
    }

    /**
     * Tells the MatchController to save the game when the save result-button is pressed.
     * Sends notifications to the EventBus
     */
    @FXML private void saveResultPressed(){
        this.mc.saveGame();
        eb.notify(TopWindow.SETSCENE_MAIN);
        eb.notify(TopWindow.MATCH_REGISTERED);

    }

    /**
     * Starts a unranked match using the MatchController. Sets view to "match-mode", displaying correct elements.
     * Triggers when start unranked match is pressed.
     */
    @FXML private void startUnrankedMatchPressed(){
        this.mc.setEndGameStrategy(this.mc.UNRANKED);
        startMatch();
    }
    /**
     * Starts a ranked match using the MatchController. Sets view to "match-mode", displaying correct elements.
     * Triggers when start ranked match is pressed.
     */
    @FXML private void startRankedMatchPressed(){
        this.mc.setEndGameStrategy(this.mc.RANKED);
        startMatch();
    }

    private void startMatch() {
        nickname1Label.setVisible(false);
        nickname2Label.setVisible(false);
        if(setPlayer1Field.getText().isEmpty() || setPlayer2Field.getText().isEmpty()){
            if(setPlayer1Field.getText().isEmpty()){
                nickname1Label.setVisible(true);
            }
            if(setPlayer2Field.getText().isEmpty()){
                nickname2Label.setVisible(true);
            }
            return;
        }else{
            //Get strings inputted in text fields
            String p1Nickname = setPlayer1Field.getText().toLowerCase();
            mc.setPlayer1(p1Nickname);
            String p2Nickname = setPlayer2Field.getText().toLowerCase();
            mc.setPlayer2(p2Nickname);
            mc.setStartingPlayerBasedOnChalmersAge();
            resetGlasses();
            updatePlayer();

            //Only show ranking if user is registered.
            boolean p1Exists = UserLedger.getInstance().doesUserExist
                    (p1Nickname);
            if (p1Exists) {
                p1Rank.setText("" + match.getPlayer(Match.Player.ONE).getRanking().getPoints());
            } else {
                p1Rank.setText("(Not registered)");
                match.setPlayer1(UserFactory.createGuestUser());
            }

            boolean p2Exists = UserLedger.getInstance().doesUserExist
                    (p2Nickname) && !p1Nickname.equals(p2Nickname);
            if (p2Exists){
                p2Rank.setText("" + match.getPlayer(Match.Player.TWO).getRanking().getPoints());
            } else {
                p2Rank.setText("(Not registered)");
                match.setPlayer2(UserFactory.createGuestUser());
            }

            p1Name.setText(match.getPlayer(Match.Player.ONE).getNickname());
            p2Name.setText(match.getPlayer(Match.Player.TWO).getNickname());
            p1Pane.setVisible(true);
            p2Pane.setVisible(true);
            mainPane.setVisible(true);
            preMatchPane.setVisible(false);
            startRankedMatchButton.setDisable(true);
            startUnrankedMatchButton.setDisable(true);
            mc.startMatch();
        }
    }

    /**
     * Deals with incoming events from the EventBus, then takes proper action depending on event.
     * @param event the key of the event that occured
     */

    @Override
    public void notifyEvent(String event) {
        switch (event) {
            case Match.MISS_RECORDED:
                updatePlayer();
                break;
            case Match.HIT_RECORDED:
                updatePlayer();
                break;
            case Match.DUEL_ENDED:
                updateGlasses();
                break;
            case Match.ROUND_ENDED:
                p1Rounds.setText("" + match.getPlayerRoundsWon(Match.Player.ONE));
                p2Rounds.setText("" + match.getPlayerRoundsWon(Match.Player
                        .TWO));
                resetGlasses();
                break;
            case Match.MATCH_ENDED:
                disableReg();
                break;
            default:
                //Do nothing
        }
    }

    /**
     * Horrible way to update glasses according to matchstate.
     * Called when a duel is over
     */

    private void updateGlasses(){
        Match.Glass[] glasses = match.getGlasses();
        if(!glasses[0].isActive()){
            glass1.setFill(Color.LIGHTGRAY);
        }
        if(!glasses[1].isActive()){
            glass2.setFill(Color.LIGHTGRAY);
        }
        if(!glasses[2].isActive()){
            glass3.setFill(Color.LIGHTGRAY);
        }
        if(!glasses[3].isActive()){
            glass4.setFill(Color.LIGHTGRAY);
        }
        if(!glasses[4].isActive()){
            glass5.setFill(Color.LIGHTGRAY);
        }
        if(!glasses[5].isActive()){
            glass6.setFill(Color.LIGHTGRAY);
        }
        if(!glasses[6].isActive()){
            glass7.setFill(Color.LIGHTGRAY);
        }
    }

    /**
     * Changes the background color to properly display whose turn it is to Throw.
     *
     */
    private void updatePlayer(){
        if(match.getPlayerWhoseTurnItIs().equals(Match.Player.ONE)){
            p1Pane.setBackground(activeBackground);
            p2Pane.setBackground(inactiveBackground);
        } else {
            p1Pane.setBackground(inactiveBackground);
            p2Pane.setBackground(activeBackground);
        }
    }

    /**
     * Disables registration of new hits/misses, activates the "match over"-view
     *
     */
    private void disableReg(){
        if(Match.Player.ONE.equals(match.getRoundWinner())){
            winner = p1Name.getText();
        } else {
            winner = p2Name.getText();
        }
        winnerLabel.setText("The winner is: " + winner);
        mainPane.setVisible(false);
        p1Pane.setBackground(inactiveBackground);
        p2Pane.setBackground(inactiveBackground);
        p1Pane.setVisible(false);
        p2Pane.setVisible(true);
        matchOverPane.setVisible(true);

    }


    private void tearDown() {
        eb.removeNotifyEventListener(Match.HIT_RECORDED, this);
        eb.removeNotifyEventListener(Match.MISS_RECORDED, this);
        eb.removeNotifyEventListener(Match.DUEL_ENDED, this);
        eb.removeNotifyEventListener(Match.ROUND_ENDED, this);
        eb.removeNotifyEventListener(Match.MATCH_ENDED, this);
    }

    /**
     * Displays all glasses as active
     *
     */
    private void resetGlasses(){
        glass1.setFill(Color.ORANGE);
        glass2.setFill(Color.ORANGE);
        glass3.setFill(Color.ORANGE);
        glass4.setFill(Color.ORANGE);
        glass5.setFill(Color.ORANGE);
        glass6.setFill(Color.ORANGE);
        glass7.setFill(Color.ORANGE);
    }

}
