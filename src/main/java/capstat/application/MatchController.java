package capstat.application;

import capstat.infrastructure.NotifyEventListener;
import capstat.model.*;

/**
 * Created by Jakob on 20/05/15.
 *
 * Class to control the match in the model-layer
 *
 */
public class MatchController implements NotifyEventListener {

    private Match match;
    private UserLedger ul = UserLedger.getInstance();
    private EndGameStrategy endGameStrategy;

    /**
     * Creates a new Match using the MatchFactory.
     * @return default instance of a match, created in MatchFactory.
     */
    public static Match createNewMatch(){
        return MatchFactory.createDefaultMatch();
    }

    /**
     * Creates a new MatchController with a match.
     * @param match
     */
    public MatchController(Match match){
        this.match = match;
        this.match.addNotificationEventListener(Match.MATCH_ENDED, this);
        this.endGameStrategy = new UnrankedStrategy();
    }

    /**
     * Starts the match in the Model-layer
     */
    public void startMatch() {
        match.startMatch();
    }

    /**
     * Registers the latest Throw as a Miss
     */
    public void recordMiss() {
        match.recordMiss();
    }

    /**
     * Registers the latest Throw as a Hit
     */
    public void recordHit() {
        match.recordHit();
    }

    public void setPlayer1(String text) {
        final User p1 = ul.getUserByNickname(text);
        match.setPlayer1(p1);
    }
    public void setPlayer2(String text) {
        final User p2 = ul.getUserByNickname(text);
        match.setPlayer2(p2);
    }

    public void setEndGameStrategy(EndGameStrategy strategy) {
        this.endGameStrategy = strategy;
    }

    @Override
    public void notifyEvent(final String event) {
        if (event.equals(Match.MATCH_ENDED))
            this.endGameStrategy.endGame();
    }

    /**
     * @author hjorthjort
     */
    public static interface EndGameStrategy {
        void endGame();
    }

    public class UnrankedStrategy implements EndGameStrategy {
        @Override
        public void endGame() {
            //do nothing
        }
    }

    public class RankedStrategy implements EndGameStrategy {
        @Override
        public void endGame() {
            Match.Player winningPlayer = null;
            try {
                if (!match.isOngoing()) {
                    winningPlayer = match.getWinner();
                }
            } catch (Match.MatchNotOverException e) {
                throw new RuntimeException("Match not over " +
                        "exception was thrown even though mathc reported " +
                        "being over.");
            }
            Match.Player losingPlayer = winningPlayer == Match.Player.ONE ?
                    Match.Player.TWO : Match.Player.ONE;
            //At this point, a new exception will have been thrown if
            // winningPlayer has not been declared to somehting other than null.
            User winner = match.getPlayer(winningPlayer);
            User loser = match.getPlayer(losingPlayer);
            double[] newRanking = ELORanking.calculateNewRanking(winner.getRanking(),
                    loser.getRanking(), match.getPlayerRoundsWon(winningPlayer), match
                            .getPlayerRoundsWon(losingPlayer));
            winner.setRanking(newRanking[0]);
            loser.setRanking(newRanking[1]);
        }
    }
}
