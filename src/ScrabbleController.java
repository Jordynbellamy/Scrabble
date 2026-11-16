import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
/**
 * ScrabbleController clicks in the Scrabble window into moves on the model
 *
 * @author Jordyn Bellamy
 */
public class ScrabbleController implements ActionListener {
    private final GameModel model;
    private final ScrabbleFrame frame;
    private int selectedRow = -1;
    private int selectedCol = -1;

    /**
     * Creates a controller for the Scrabble game and its window
     *
     * @param model the game
     * @param frame the window
     */
    public ScrabbleController(GameModel model, ScrabbleFrame frame) {
        this.model = model;
        this.frame = frame;
    }

    /**
     * Does the move for the button that was clicked
     *
     * @param e the click
     */
    public void actionPerformed(ActionEvent e) {
        String[] command = e.getActionCommand().split(" "); //"SQUARE 7 7" becomes ["SQUARE", "7", "7"]

        if (command[0].equals("SQUARE")) {
            selectedRow = Integer.parseInt(command[1]);
            selectedCol = Integer.parseInt(command[2]);
            frame.selectSquare(selectedRow, selectedCol);

        } else if (command[0].equals("PLAY")) {
            if (selectedRow == -1) {
                frame.setStatus("Click the square your word starts on first");
            } else if (model.playWord(frame.getLetters(), selectedRow, selectedCol, frame.isAcross())) {
                selectedRow = -1; //The word was played, so the next player starts with no square selected
            }

        } else if (command[0].equals("EXCHANGE")) {
            if (model.exchangeTiles(frame.getLetters())) {
                selectedRow = -1;
            }

        } else if (command[0].equals("PASS")) {
            model.passTurn();
            selectedRow = -1;
        }
    }
}