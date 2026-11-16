import javax.swing.*;
import java.awt.*;
/**
 * The window for the Scrabble game. It shows the board, the scores, the current
 * player's tiles, and the controls for each move
 *
 * @author Jordyn Bellamy
 */
public class ScrabbleFrame extends JFrame implements ScrabbleView {
    private GameModel model;
    private final JButton[][] squares = new JButton[Board.SIZE][Board.SIZE];
    private final JLabel scoresLabel = new JLabel();
    private final JLabel rackLabel = new JLabel();
    private final JLabel statusLabel = new JLabel();
    private final JTextField wordField = new JTextField(10);
    private final JRadioButton acrossButton = new JRadioButton("Across", true);
    private final JRadioButton downButton = new JRadioButton("Down");

    /**
     * Builds the window and connects it to the model and controller
     *
     * @param model the game to show
     */
    public ScrabbleFrame(GameModel model) {
        super("Scrabble");
        this.model = model;
        ScrabbleController controller = new ScrabbleController(model, this);
        //Board: a 16x16 grid so there is room for the column letters and row numbers
        JPanel boardPanel = new JPanel(new GridLayout(Board.SIZE + 1, Board.SIZE + 1));
        boardPanel.add(new JLabel(""));

        for (int col = 0; col < Board.SIZE; col++) {
            boardPanel.add(new JLabel("" + (char) ('A' + col), SwingConstants.CENTER));
        }

        for (int row = 0; row < Board.SIZE; row++) {
            boardPanel.add(new JLabel("" + (row + 1), SwingConstants.CENTER));

            for (int col = 0; col < Board.SIZE; col++) {
                JButton square = new JButton();
                square.setPreferredSize(new Dimension(40, 40));
                square.setMargin(new Insets(0, 0, 0, 0));
                square.setActionCommand("SQUARE " + row + " " + col); //Tells the controller which square was clicked
                square.addActionListener(controller);
                squares[row][col] = square;
                boardPanel.add(square);
            }
        }
        //Across and Down are in a group so only one can be picked
        ButtonGroup direction = new ButtonGroup();
        direction.add(acrossButton);
        direction.add(downButton);

        JButton playButton = new JButton("Play Word");
        JButton exchangeButton = new JButton("Exchange");
        JButton passButton = new JButton("Pass");
        playButton.setActionCommand("PLAY");
        exchangeButton.setActionCommand("EXCHANGE");
        passButton.setActionCommand("PASS");
        playButton.addActionListener(controller);
        exchangeButton.addActionListener(controller);
        passButton.addActionListener(controller);

        JPanel controls = new JPanel();
        controls.add(new JLabel("Letters:"));
        controls.add(wordField);
        controls.add(acrossButton);
        controls.add(downButton);
        controls.add(playButton);
        controls.add(exchangeButton);
        controls.add(passButton);

        //Bottom of the window: the tiles, then the controls, then messages
        JPanel bottom = new JPanel(new GridLayout(3, 1));
        bottom.add(rackLabel);
        bottom.add(controls);
        bottom.add(statusLabel);

        add(scoresLabel, BorderLayout.NORTH);
        add(boardPanel, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        model.addScrabbleView(this);
        redraw();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }
    /** Shows the board, scores, and current player's tiles from the model */
    public void redraw() {
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Tile tile = model.getBoard().getTile(row, col);
                if (tile != null) {
                    squares[row][col].setText("" + tile.getLetter());
                } else if (row == Board.SIZE / 2 && col == Board.SIZE / 2) {
                    squares[row][col].setText("*");
                } else {
                    squares[row][col].setText("");
                }
                squares[row][col].setBackground(null); //Removes the highlight from a selected square
            }
        }

        String scores = "";
        for (Player player : model.getPlayers()) {
            scores += player.getName() + ": " + player.getScore() + "     ";
        }
        scoresLabel.setText(scores);

        Player current = model.getCurrentPlayer();
        rackLabel.setText(current.getName() + "'s tiles: " + current.getRack());
        statusLabel.setText("Click a square to start your word on, or exchange or pass");
        wordField.setText("");
    }

    /**
     * Highlights the square a word will start on
     *
     * @param row the row of the square
     * @param col the column of the square
     */
    public void selectSquare(int row, int col) {
        redraw(); //Clears the last highlighted square
        squares[row][col].setBackground(Color.YELLOW);
        statusLabel.setText("Type your word, pick Across or Down, then click Play Word");
    }

    /** @return the letters typed in the text box */
    public String getLetters() {
        return wordField.getText();
    }

    /** @return true if Across is picked, false if Down is picked */
    public boolean isAcross() {
        return acrossButton.isSelected();
    }

    /**
     * Shows a message at the bottom of the window
     *
     * @param message the message to show
     */
    public void setStatus(String message) {
        statusLabel.setText(message);
    }


    /**
     * Called by the model after every move. An empty message means the move worked,
     * so the window is redrawn. Otherwise the message says why the move was rejected
     *
     * @param e the event from the model
     */
    @Override
    public void handleScrabbleUpdate(ScrabbleEvent e) {
        if (e.getMessage().isEmpty()) {
            redraw();
        } else {
            statusLabel.setText("Move rejected: " + e.getMessage());
        }
    }

    /**
     * Asks how many players there are, then opens the game
     *
     * @param args not used
     */
    public static void main(String[] args) {
        Integer[] options = {2, 3, 4};
        Integer players = (Integer) JOptionPane.showInputDialog(null, "Number of players:",
                "Scrabble", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (players != null) { //null means the dialog was closed
            new ScrabbleFrame(new GameModel(players));
        }
    }
}
