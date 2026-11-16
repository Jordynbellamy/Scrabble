/**
 * Event sent by the GameModel to its views whenever the game state changes.
 *  * The source of the event is the GameModel, so a view can read the updated board, players and scores from it
 */

import java.util.EventObject;

public class ScrabbleEvent extends EventObject {
    private final String message;

    /**
     * Creates an event for a change in the game.
     *
     * @param model   the GameModel that changed
     * @param message the error from the last move, or an empty string if it succeeded
     */
    public ScrabbleEvent(GameModel model, String message){
        super(model);
        this.message = message;
    }
    /**
     * Returns the message for this update.
     *
     * @return the reason the last move was rejected, or an empty string if it succeeded
     */
    public String getMessage(){
        return message;
    }
}
