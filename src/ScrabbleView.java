/** Listener interface for the Scrabble game. Any class that implements it can subscribe to GameModel and will be nnotified
 *  any events or whenever the game state changes.
 *  @author Kosi Chukwuezi
 */
public interface ScrabbleView {
    /**Called by the GameModel after any change to the game, such as a word
     * being played, tiles exchanged, a turn passed, or a move rejected.
     *
     * @param e the event describing the update
     **/
    void handleScrabbleUpdate(ScrabbleEvent e);
}
