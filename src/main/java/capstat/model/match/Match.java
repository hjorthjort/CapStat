package capstat.model.match;

import capstat.infrastructure.eventbus.EventBus;
import capstat.infrastructure.eventbus.NotifyEventListener;
import capstat.model.user.User;

import java.time.Instant;

/**
 * A class representing a match of caps. It encapsulates most behavior that
 * is related to such a match, and methods for manipulating the state of the
 * match by only inputting whether the last throw was a hit or not.
 *
 * This class if very tightly couple with the ThrowSequence class, and is
 * separated from it mostly for legibility reasons.
 *
 * A client that wishes to get notifications on events in this match must
 * implement {@link NotifyEventListener} and register to the event bus using
 * the String constant for the event it wishes to subscribe to, to get
 * updates from all matches when they update their state.
 * To get notifications from a specific instance of a match only, the client may
 * use the methods implemented for subscription in the Match interface for
 * convenience. It will then receive notifications with an event string
 * consisting of the constant for the event, e.g. MATCH_ENDED, and the
 * eventId for this match. A match will send both a general MATCH_ENDED
 * event, as well as one with the string MATCH_ENDED + getEventId().
 *
 * @author hjorthjort
 * @reviewed by holmus
 */

/*
 * ToDo: Bad implementation of GameOver, work out better solution
 */
public class Match {

    // Constants

    /**
     * All matches will call the event bus with this key when it starts by a
     * call to startMatch.
     */
    public static final String MATCH_STARTED = "Match started";
    /**
     * All matches will call the event bus with this key when it ends.
     */
    public static final String MATCH_ENDED = "Match ended";
    /**
     * All matches will call the event bus with this key when a round ends.
     */
    public static final String ROUND_ENDED = "Round ended";
    /**
     * All matches will call the event bus with this key when a duel starts.
     */
    public static final String DUEL_STARTED = "Duel started";
    /**
     * All matches will call the event bus with this key when a duel ends.
     */
    public static final String DUEL_ENDED = "Duel ended";
    /**
     * All matches will call the event bus with this key when a new hit is
     * recorded, by a call to recordHit.
     */
    public static final String HIT_RECORDED = "Hit recorded";
    /**
     * All matches will call the event bus with this key when a new hit is
     * recorded, by a call to recordMis.
     */
    public static final String MISS_RECORDED = "Miss recorded";


    //Instance fields

    private static int globalEventId = 0;
    private final int eventId;
    private boolean isOngoing;
    private ThrowSequence throwSequence;
    private Match.Glass[] glasses;
    private User player1, player2, spectator;
    private Player roundWinner, playerWhoseTurnItIs, winner;
    private int p1RoundsWon, p2RoundsWon, roundsToWin, numberOfGlasses;
    private Instant startTime, endTime;


    boolean checkInvariants() {
        if (isOngoing && startTime == null) return false;
        if (glasses.length % 2 == 0) return false;
        if (p1RoundsWon + p2RoundsWon > 0 && roundWinner == null) return false;
        if (playerWhoseTurnItIs == null) return false;
        if (p1RoundsWon < 0 || p2RoundsWon < 0) return false;
        if (p1RoundsWon > roundsToWin || p2RoundsWon > roundsToWin) return
                false;
        if (numberOfGlasses != glasses.length) return false;
        return true;
    }

    //Construction

    /**
     * Creates a new match.
     * @param numberOfGlasses the total number of glasses that should be in
     *                        play each round. Must be an odd number larger
     *                        than 1
     * @param roundsToWin the number of rounds required for a plyer to win
     *                    the game.
     * @throws IllegalArgumentException if
     */
     Match(int numberOfGlasses, int roundsToWin) {
        //Assigns a unique (almost guaranteed) event ID for this specific match.
        this.eventId = globalEventId;
        globalEventId++;

        if (numberOfGlasses < 3 || numberOfGlasses % 2 == 0)
            throw new IllegalArgumentException("Glasses must be an odd number" +
                    " larger than 1");
        if (roundsToWin < 1) {
            throw new IllegalArgumentException("Rounds to win must be set > 1");
        }
        this.numberOfGlasses = numberOfGlasses;
        this.isOngoing = false;
        this.roundsToWin = roundsToWin;
        this.playerWhoseTurnItIs = Player.ONE;
        p1RoundsWon = 0;
        p2RoundsWon = 0;
        createGlasses();
        this.throwSequence = new ThrowSequence(this.glasses, this
                .playerWhoseTurnItIs, false);
    }

    /**
     * Creates the glasses the match will be using
     *
     */
    private void createGlasses() {
        this.glasses = new Glass[this.numberOfGlasses];
        for (int i = 0; i < this.numberOfGlasses; i++) {
            this.glasses[i] = new Match.Glass();
        }
    }

    //Setters

    /**
     * The player that will be returned by calling getPlayer(Match.Player.ONE)
     * @param player1
     */
    public void setPlayer1(User player1) {
        this.player1 = player1;
    }

    /**
     * The player that will be returned by calling getPlayer(Match.Player.TWO)
     * @param player2
     */
    public void setPlayer2(User player2) {
        this.player2 = player2;
    }

    public void setSpectator(User spectator) {
        this.spectator = spectator;
    }

    /**
     * Sets the currently active player.
     * @param player the player, Match.Player.ONE or Match.Player.TWO
     */
    public void setCurrentPlayer(Player player) {
        this.playerWhoseTurnItIs = player;
        //If we are at the beginning of a sequence, just change the player
        // whos turn it is. But if we are not, start a new partial sequence
        if (this.throwSequence.canRewind()) {
            this.throwSequence.updateRecordState(glasses, playerWhoseTurnItIs,
                    throwSequence.lastThrowWasHit());
        } else {
            this.throwSequence.changeStartingPlayerForCurrentSequence(player);
        }
    }

    //Getters

    /**
     * @return true if the match has started but not yet finished. Returns
     * true also if the match is paused. Returns false otherwise.
     */
    public boolean isOngoing() {
        return this.isOngoing;
    }

    /**
     * @return Whether game is currently in a duel or not.
     */
    public boolean isDuelling() {
        return this.isOngoing && this.throwSequence.lastThrowWasHit();
    }

    /**
     * @param player
     * @return the User representation of the Enum player sent as param, could return null if Users are not defined yet.
     */
    public User getPlayer(Player player){
        if(player == null){
            throw new NullPointerException("Player can't be null");
        }
        if(player == Player.ONE){
            return this.player1;
        } else{
            return this.player2;
        }
    }

    /**
     * Method which gets the spectator for the match
     * @return The User who started the match (the logged in user), not necessarily one of the players.
     */
    public User getSpectator() {
        return spectator;
    }

    /**
     * Method to get player1: s score
     * @return player1:s score as an int
     */
    public int getPlayer1Score() {
        int score = 0;
        for (int i = this.glasses.length - 1; i >= this.glasses.length / 2; i--) {
            if (this.glasses[i].isActive()) break;
            else score++;
        }
        return score;
    }
    /**
     * Method to get player2: s score
     * @return player2:s score as an int
     */
    public int getPlayer2Score() {
        int score = 0;
        for (int i = 0; i <= this.glasses.length / 2; i++) {
            if (this.glasses[i].isActive()) break;
            else score++;
        }
        return score;
    }

    /**
     * Method to get rounds won for desired player,
     * @param player enum for player one or player two
     * @return the number of rounds won for the player selected as param
     */
    public int getPlayerRoundsWon(final Player player) {
        int ret = player == Player.ONE ? this.p1RoundsWon : this.p2RoundsWon;
        return ret;
    }

    /**
     * Returns which player whose turn it is
     * @return the player who should do the next Throw
     */
    public Player getPlayerWhoseTurnItIs() {
        if (playerWhoseTurnItIs == Player.ONE) {
            return Player.ONE;
        }
        return Player.TWO;
    }

    /**
     *
     * @return a deep getCopy of the ThrowSequence of this match.
     */
    public ThrowSequence getThrowSequence() {
        return new ThrowSequence(this.throwSequence);
    }

    /**
     * @return the Instant object created at the start of the match. Returns
     * null if tha match has not been started.
     */
    public Instant getStartTime() {
        return startTime;
    }

    /**
     *
     * @return the Instant object created at the end of the match. Returns
     * null if the match has not ended.
     */
    public Instant getEndTime() {
        //This is safe since Instants are immutable
        return endTime;
    }

    /**
     * @return An array representing the glasses in the match, starting with
     * the glasses of Player 1, then the middle glass, and then the glasses
     * of Player 2.
     */
    public Match.Glass[] getGlasses() {
        return this.glasses.clone();
    }

    /**
     *
     * @return the winner of the last round, or null if no round has ended.
     */
    public Player getRoundWinner() {
        return roundWinner;
    }

    /**
     * @return the User who is the winner of the game. If the players have
     * not been set, null will be returned.
     * @throws MatchNotOverException if and only if match is ongoing, which
     *                               can be checked by call to isOngoing.
     */
    public Player getWinner() throws MatchNotOverException {
        if (this.isOngoing) {
            throw new MatchNotOverException();
        }
        return winner;
    }

    //Gameplay Operations

    /**
     * Gives the starting player of a match, according to caps rules. The rules
     * says that the "youngest" player starts. See ChalmersAge.compareTo for
     * more details.
     * @return the starting player of this match
     */
    public Player calculateStartingPlayer() {
        //If player 1 is younger, or if they are equally young, player one
        // gets to start. If player 2 is younger, they get to start.
        // Only change the starting player if player 1 is "older" than player
        // 2. If player 1 is older, the comparison should return a value
        // larger than.
        if (player1 == null || player2 == null)
            return Player.ONE;
        return this.player1.getChalmersAge().compareTo(this.player2
                .getChalmersAge()) >= 0 ? Player.ONE : Player.TWO;
    }

    /**
     * Makes the match start.
     */
    public void startMatch() {
        this.startTime = Instant.now();
        this.isOngoing = true;
        notifyListeners(MATCH_STARTED);
    }


    /**
     * Updates the game with a new Throw, which is a hit.
     */
    public void recordHit() {
        if(isOngoing) {
            if (!this.isDuelling()) {
                this.notifyListeners(DUEL_STARTED);
            }
            this.switchPlayerUpNext();
            this.throwSequence.add(Throw.HIT);
        }
        this.notifyListeners(HIT_RECORDED);
    }

    /**
     * Updates the game with a new throw, which is a miss.
     */
    public void recordMiss() {
        if(isOngoing) {
            if (this.isDuelling()) {
                this.removeGlass();
                this.endRoundIfNecessary();
                this.notifyListeners(DUEL_ENDED);
            } else {
                //Only switch turns if the throw did not end a duel.
                this.switchPlayerUpNext();
            }
            this.throwSequence.add(Throw.MISS);
        }
        this.notifyListeners(MISS_RECORDED);
    }

    /**
     * Allows the spectator to change the state of the game, if registration goes wrong
     * @param glasses an array of glasses telling desired state of glasses
     * @param startingPlayer whose turn it is
     * @param lastThrowWasHit true if last throw  was a hit, used to know if a duel is active
     */
    public void manuallyChangeGameState(Match.Glass[] glasses, Player
            startingPlayer, boolean lastThrowWasHit) {
        this.glasses = glasses;
        this.playerWhoseTurnItIs = startingPlayer;
        this.throwSequence.updateRecordState(glasses, startingPlayer,
                lastThrowWasHit);
    }


    /**
     * If it's player ONE's turn when this is called, turn moves to player
     * TWO. And vice versa.
     */
    private void switchPlayerUpNext() {
        this.playerWhoseTurnItIs = this.playerWhoseTurnItIs == Player.ONE ? Player.TWO :
                Player.ONE;
    }

    /**
     * Checks if middle glass is inactive and ends game if it is.
     */
    private void endRoundIfNecessary() {
        if (!this.glasses[this.glasses.length / 2].isActive) {
            updateRoundWinner();
            endMatchIfNecessary();
            createGlasses();
            notifyListeners(ROUND_ENDED);
        }
    }

    /**
     * Increments the score of whichever player is the round winner
     */
    private void updateRoundWinner() {
        int p1score = this.getPlayer1Score();
        int p2score = this.getPlayer2Score();
        if (p1score == p2score) {
            this.roundWinner = playerWhoseTurnItIs == Player.ONE ? Player.TWO
                    : Player.ONE;
        } else if (p1score > p2score) {
            this.roundWinner = Player.ONE;
        } else {
            this.roundWinner = Player.TWO;
        }
        if (roundWinner == Player.ONE) p1RoundsWon++;
        if (roundWinner == Player.TWO) p2RoundsWon++;
    }

    /**
     * Checks if match should end, and ends it in that case.
     */
    private void endMatchIfNecessary() {
        if (p1RoundsWon == this.roundsToWin || p2RoundsWon == this.roundsToWin) endMatch();
    }

    /**
     * Ends the match closing registration of new Throws, called by endMatchIfNecessary
     */
    private void endMatch() {
        this.endTime = Instant.now();
        this.isOngoing = false;
        if(p1RoundsWon == p2RoundsWon){
            throw new ArithmeticException("We've dun goofed");
        }
        this.winner = p1RoundsWon > p2RoundsWon ? Player.ONE : Player.TWO;
        this.notifyListeners(MATCH_ENDED);
    }

    /**
     * Method to remove the furthest out, currently active glass for the player who just lost a duel
     */
    private void removeGlass() {
        if (this.playerWhoseTurnItIs == Player.ONE) {
            this.removeNextGlassPlayer1();
        } else {
            this.removeNextGlassPlayer2();
        }
    }

    /**
     * Finds next active glass on player 1's side and removes it. Middle
     * glass can be removed.
     */
    private void removeNextGlassPlayer1() {
        for (int i = 0; i <= (this.glasses.length / 2); i++) {
            if (this.removeSpecificGlass(i)) break;
        }
    }

    /**
     * Finds next active glass on player 2's side and removes it. Middle
     * glass can be removed.
     */
    private void removeNextGlassPlayer2() {
        for (int i = this.glasses.length - 1; i >= this.glasses.length / 2; i--) {
            if (this.removeSpecificGlass(i)) break;
        }
    }

    /**
     * Sets specified glass to unactive if it was active, otherwise does
     * nothing.
     *
     * @param i
     * @return true if glass was removed, false otherwise.
     */
    private boolean removeSpecificGlass(final int i) {
        if (glasses[i].isActive()) {
            glasses[i].isActive = false;
            return true;
        }
        return false;
    }

    //Event Handling

    /**
     * Method to get the EventId of this specific match
     * @return this matchs specific EventId.
     */
    public int getEventId() {
        return eventId;
    }

    /**
     * Add an observer that will be notified when this match instance is
     * over.
     *
     * @param event the event string that should be listened for. By
     *              default Match uses only the predefined constants String
     *              constants in the class for updates.
     * @param listener the listener that will get notified by event with the
     *                 specified String.
     */
    public void addNotificationEventListener(final String event, final
    NotifyEventListener listener) {
        EventBus.getInstance().addNotifyEventListener(event + this.eventId,
                listener);
    }

    /**
     * Make the specified listener stop listening to this specific matches
     * notifications of the type specified by the String.
     * @param event
     * @param listener
     */
    public void removeNotificationEventListener(final String event, final
                                           NotifyEventListener listener) {
        EventBus.getInstance().removeNotifyEventListener(event + this.eventId,
                listener);
    }

    /**
     * Sends a notification of a certain event, using the EventBus.
     * @param event the event to notify, as a String
     */
    private void notifyListeners(final String event) {
        //Notify listeners that listen to all events of all matches.
        EventBus.getInstance().notify(event);
        //Notify listeners that listen to this specific match.
        EventBus.getInstance().notify(event + this.eventId);
    }

    //Inner classes

    /**
     * An exception signalizing the match isn't over, when the model expects it to be
     */
    public static class MatchNotOverException extends Exception {
    }

    /**
     * Class representing the glasses in the match. A glass is either active
     * or inactive.
     */
    public static class Glass {
        private boolean isActive = true;

        /**
         * Method to see if a glass is active
         * @return true if the glass is Active, false if not
         */
        public boolean isActive() {
            return isActive;
        }

        /**
         * Method to set a glass to active/inactive.
         * @param active boolean used to set glass to active if true/inactive if false
         */
        public void setActive(boolean active) {
            this.isActive = active;
        }

        /**
         * Method to check if the glass object is the same as object o
         * @param o Object to compare with the glass used to call the method.
         * @return true if o is the same object as the glass used to call the method, otherwise false
         */
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;

            Glass that = (Glass) o;

            if (this.isActive != that.isActive) return false;

            return true;
        }
    }

    /**
     * A class representing a Throw. It in turn has two inner classes: one
     * representing a hit and one representing a miss. These are not shown to
     * the user, and there will always be only one objcet of each class,
     * which can be retrieved by the create-methods in Throw.
     */
    public enum Throw {
        HIT,
        MISS
    }

    public enum Player{
        ONE,
        TWO
    }

    //Other

    /**
     *
     * @return a String representing match as player names with an asterisk
     * at player who's turn it is and a representation of the glasses, where
     * 'O' marks active glass and 'X' marks inactive glass.
     */
    @Override
    public String toString() {
        String p1 = player1 == null ? "Not set" : player1.getNickname();
        String p2 = player2 == null ? "Not set" : player2.getNickname();
        StringBuffer gl = new StringBuffer("  ");
        for (int i = 0; i < this.glasses.length; i++) {
            if (this.glasses[i].isActive) gl = gl.append("O    ");
            else gl = gl.append("X     ");
        }
        int spaces = gl.length() - (p1.length() + p2.length()) - 1;
        StringBuffer sp = new StringBuffer();
        for (int i = 0; i < spaces; i++) {
            sp = sp.append(" ");
        }
        if (this.playerWhoseTurnItIs == Player.ONE) p1 = p1 + "*";
        else p2 = "*" + p2;

        return p1 + sp + p2 + "\n" + gl;
    }
}
