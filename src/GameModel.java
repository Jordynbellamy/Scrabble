import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;

/**
 * Coordinates a text-based Scrabble game for two to four players.
 *@author Ojediran Peculiar
 *
 * 
 */
public class GameModel {
    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 4;
    private static final int RACK_SIZE = 7;

    private final Board board;
    private final TileBag tileBag;
    private final WordList wordList;
    private final Scorer scorer;
    private final List<Player> players;
    private final Scanner input;
    private int currentPlayerIndex;
    private String lastError = "";

    /** Creates a game and deals seven tiles to each player. */
    public GameModel(int numPlayers) {
        this(numPlayers, new Board(Board.SIZE, Board.SIZE), new TileBag(),
                new WordList(), new Scorer(), new Scanner(System.in));
    }

    /**
     * Dependency-injection constructor to permit deterministic unit tests.
     * The supplied board should initially be empty.
     *
     * @param numPlayers number of players (2 through 4)
     * @param board      game board
     * @param tileBag    tile bag
     * @param wordList   loaded dictionary
     * @param scorer     move scorer
     * @param input      console input source
     */
    public GameModel(int numPlayers, Board board, TileBag tileBag,
            WordList wordList, Scorer scorer, Scanner input) {
        if (numPlayers < MIN_PLAYERS || numPlayers > MAX_PLAYERS) {
            throw new IllegalArgumentException("Scrabble requires 2 to 4 players.");
        }
        this.board = Objects.requireNonNull(board, "board");
        this.tileBag = Objects.requireNonNull(tileBag, "tileBag");
        this.wordList = Objects.requireNonNull(wordList, "wordList");
        this.scorer = Objects.requireNonNull(scorer, "scorer");
        this.input = Objects.requireNonNull(input, "input");
        this.players = new ArrayList<>();
        for (int i = 0; i < numPlayers; i++) {
            Player player = new Player("Player " + (i + 1));
            player.refillRack(tileBag);
            players.add(player);
        }
    }

    /** @return the model's board */
    public Board getBoard() {
        return board;
    }

    /** @return an unmodifiable list of players, in turn order */
    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    /** @return player whose turn it is */
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    /** @return zero-based index of the current player */
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    /** @return the explanation for the last rejected move, or an empty string */
    public String getLastError() {
        return lastError;
    }

    /**
     * Tries to play a word using standard coordinate order: 8H is horizontal,
     * beginning in row 8, column H; H8 is vertical, beginning at H8.
     *
     * @param notation position such as "8H" or "H8"
     * @param word     the full visible word, including existing overlapping letters
     * @return true if accepted; false with an explanation from getLastError()
     */
    public boolean playWord(String notation, String word) {
        if (notation == null) {
            return reject("Enter a coordinate, for example 8H or H8.");
        }
        String location = notation.trim().toUpperCase(Locale.ROOT);
        if (location.matches("\\d{1,2}[A-O]")) {
            int row = Integer.parseInt(location.substring(0, location.length() - 1)) - 1;
            int col = location.charAt(location.length() - 1) - 'A';
            return playWord(word, row, col, true);
        }
        if (location.matches("[A-O]\\d{1,2}")) {
            int col = location.charAt(0) - 'A';
            int row = Integer.parseInt(location.substring(1)) - 1;
            return playWord(word, row, col, false);
        }
        return reject("Use coordinates such as 8H (across) or H8 (down), rows 1-15 and columns A-O.");
    }

    /**
     * Validates, scores, and commits a placement. Invalid moves do not remove
     * tiles, modify scores, or advance the turn.
     *
     * @param word   full main word, including matching letters on the board
     * @param row    zero-based starting row
     * @param col    zero-based starting column
     * @param across true for left-to-right, false for top-to-bottom
     * @return whether the move was accepted
     */
    public boolean playWord(String word, int row, int col, boolean across) {
        lastError = "";
        if (word == null || !word.trim().matches("[A-Za-z]{2,15}")) {
            return reject("A word must have 2 to 15 letters.");
        }
        String upper = word.trim().toUpperCase(Locale.ROOT);
        if (row < 0 || col < 0 || row >= Board.SIZE || col >= Board.SIZE
                || (across && col + upper.length() > Board.SIZE)
                || (!across && row + upper.length() > Board.SIZE)) {
            return reject("The word does not fit on the board.");
        }

        char[][] before = snapshot();
        char[][] after = copyOf(before);
        boolean opening = isEmpty(before);
        boolean coversCentre = false;
        boolean connected = false;
        StringBuilder neededLetters = new StringBuilder();
        List<int[]> newPositions = new ArrayList<>();
        List<Tile> proposedTiles = new ArrayList<>();
        List<Tile> unusedRack = new ArrayList<>(getCurrentPlayer().getRack());

        for (int i = 0; i < upper.length(); i++) {
            int r = row + (across ? 0 : i);
            int c = col + (across ? i : 0);
            char letter = upper.charAt(i);
            coversCentre |= r == Board.SIZE / 2 && c == Board.SIZE / 2;
            Tile existing = board.getTile(r, c);
            if (existing != null) {
                if (Character.toUpperCase(existing.getLetter()) != letter) {
                    return reject("The move conflicts with an existing tile.");
                }
                proposedTiles.add(existing); // Reuse rather than replacing this tile.
                connected = true;
            } else {
                Tile chosen = takeMatchingTile(unusedRack, letter);
                if (chosen == null) {
                    return reject("Your rack does not contain the required letters.");
                }
                proposedTiles.add(chosen);
                neededLetters.append(letter);
                newPositions.add(new int[] { r, c });
                after[r][c] = letter;
                if (neighboursOldTile(before, r, c)) {
                    connected = true;
                }
            }
        }

        if (newPositions.isEmpty()) {
            return reject("Place at least one new tile.");
        }
        if (opening && !coversCentre) {
            return reject("The first word must cover the centre square H8.");
        }
        if (!opening && !connected) {
            return reject("Your word must connect to tiles already on the board.");
        }

        Set<String> formedWords = findFormedWords(after, newPositions);
        if (formedWords.isEmpty()) {
            return reject("The move must form a word of at least two letters.");
        }
        for (String formed : formedWords) {
            if (!wordList.isValidWord(formed)) {
                return reject("Not in the dictionary: " + formed);
            }
        }

        final int points;
        try {
            points = scorer.calculateMoveScore(before, after);
        } catch (IllegalArgumentException error) {
            return reject(error.getMessage());
        }

        // Board.placeWord must accept one Tile per position of the FULL word,
        // including identical existing tiles. The board implementation must
        // leave those existing tiles unchanged.
        board.placeWord(proposedTiles, row, col, across);
        if (!same(snapshot(), after)) {
            return reject("Board.placeWord did not apply the validated move. Board implementation is incomplete.");
        }
        List<Tile> spentTiles = getCurrentPlayer().removeLetters(neededLetters.toString());
        if (spentTiles == null || spentTiles.size() != neededLetters.length()) {
            throw new IllegalStateException("Player.removeLetters is not yet implemented correctly.");
        }
        getCurrentPlayer().addScore(points);
        getCurrentPlayer().refillRack(tileBag);
        advanceTurn();
        return true;
    }

    /**
     * Exchanges one or more owned tiles. At least seven tiles must remain in
     * the bag before an exchange, as required by standard Scrabble rules.
     *
     * @param letters letters to exchange
     * @return true if the tiles were exchanged and the turn advanced
     */
    public boolean exchangeTiles(String letters) {
        lastError = "";
        if (letters == null || !letters.trim().matches("[A-Za-z]{1,7}")) {
            return reject("Enter 1 to 7 letters to exchange.");
        }
        String upper = letters.trim().toUpperCase(Locale.ROOT);
        if (!getCurrentPlayer().hasLetters(upper)) {
            return reject("Those letters are not all in your rack.");
        }
        if (tileBag.size() < RACK_SIZE) {
            return reject("At least seven tiles must remain in the bag to exchange.");
        }
        List<Tile> returned = getCurrentPlayer().removeLetters(upper);
        if (returned == null || returned.size() != upper.length()) {
            throw new IllegalStateException("Player.removeLetters is not yet implemented correctly.");
        }
        getCurrentPlayer().refillRack(tileBag); // Draw BEFORE returning old tiles.
        tileBag.returnTiles(returned);
        advanceTurn();
        return true;
    }

    /** Passes without placing or exchanging tiles. */
    public void passTurn() {
        lastError = "";
        advanceTurn();
    }

    /** Runs the console interface for Milestone 1 until the user chooses QUIT. */
    public void play() {
        System.out.println("SCRABBLE - Milestone 1 (no premium squares or blank tiles)");
        System.out.println("PLAY <coordinate> <word>  e.g. PLAY 8H HELLO");
        System.out.println("8H = across on row 8 from column H; H8 = down from H8");
        System.out.println("EXCHANGE <letters>  |  PASS  |  QUIT");
        while (true) {
            printState();
            System.out.print(getCurrentPlayer().getName() + "> ");
            if (!input.hasNextLine()) {
                break;
            }
            String line = input.nextLine().trim();
            if (line.equalsIgnoreCase("QUIT")) {
                break;
            }
            if (line.equalsIgnoreCase("PASS")) {
                passTurn();
                continue;
            }
            String[] parts = line.split("\\s+");
            boolean accepted;
            if (parts.length == 3 && parts[0].equalsIgnoreCase("PLAY")) {
                accepted = playWord(parts[1], parts[2]);
            } else if (parts.length == 2 && parts[0].equalsIgnoreCase("EXCHANGE")) {
                accepted = exchangeTiles(parts[1]);
            } else {
                accepted = reject("Try PLAY 8H HELLO, EXCHANGE AE, PASS, or QUIT.");
            }
            if (!accepted) {
                System.out.println("Move rejected: " + lastError);
            }
        }
        System.out.println("Game ended. Final scores:");
        for (Player player : players) {
            System.out.println(player.getName() + ": " + player.getScore());
        }
    }

    /** Prints the board, scores, and current player's rack. */
    public void printState() {
        System.out.println("\n" + board);
        for (Player player : players) {
            System.out.println(player.getName() + ": " + player.getScore()
                    + " points, rack: " + player.getRack());
        }
        System.out.println("Next player: " + getCurrentPlayer().getName());
        System.out.println("Rack: " + getCurrentPlayer().getRack());
        System.out.println("Tiles in bag: " + tileBag.size());
    }

    private void advanceTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    private boolean reject(String message) {
        lastError = message;
        return false;
    }

    private char[][] snapshot() {
        char[][] state = new char[Board.SIZE][Board.SIZE];
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Tile tile = board.getTile(row, col);
                if (tile != null) {
                    state[row][col] = Character.toUpperCase(tile.getLetter());
                }
            }
        }
        return state;
    }

    private static char[][] copyOf(char[][] board) {
        char[][] copy = new char[board.length][];
        for (int i = 0; i < board.length; i++) {
            copy[i] = board[i].clone();
        }
        return copy;
    }

    private static boolean same(char[][] a, char[][] b) {
        for (int r = 0; r < a.length; r++) {
            for (int c = 0; c < a[r].length; c++) {
                if (a[r][c] != b[r][c]) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isEmpty(char[][] board) {
        for (char[] row : board) {
            for (char letter : row) {
                if (letter != '\0') {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean neighboursOldTile(char[][] before, int row, int col) {
        return (row > 0 && before[row - 1][col] != '\0')
                || (row + 1 < Board.SIZE && before[row + 1][col] != '\0')
                || (col > 0 && before[row][col - 1] != '\0')
                || (col + 1 < Board.SIZE && before[row][col + 1] != '\0');
    }

    private static Tile takeMatchingTile(List<Tile> unused, char letter) {
        for (int i = 0; i < unused.size(); i++) {
            if (Character.toUpperCase(unused.get(i).getLetter()) == letter) {
                return unused.remove(i);
            }
        }
        return null;
    }

    /** Finds all horizontal and vertical words containing at least one new tile. */
    private static Set<String> findFormedWords(char[][] board, List<int[]> positions) {
        Set<String> words = new LinkedHashSet<>();
        for (int[] position : positions) {
            String horizontal = readWord(board, position[0], position[1], true);
            String vertical = readWord(board, position[0], position[1], false);
            if (horizontal.length() >= 2) {
                words.add(horizontal);
            }
            if (vertical.length() >= 2) {
                words.add(vertical);
            }
        }
        return words;
    }

    private static String readWord(char[][] board, int row, int col, boolean across) {
        int dr = across ? 0 : 1;
        int dc = across ? 1 : 0;
        int r = row;
        int c = col;
        while (r - dr >= 0 && c - dc >= 0 && board[r - dr][c - dc] != '\0') {
            r -= dr;
            c -= dc;
        }
        StringBuilder word = new StringBuilder();
        while (r < Board.SIZE && c < Board.SIZE && board[r][c] != '\0') {
            word.append(board[r][c]);
            r += dr;
            c += dc;
        }
        return word.toString();
    }

    /** Entry point for a console game. */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Number of players (2-4): ");
        if (!scanner.hasNextInt()) {
            System.out.println("Enter a number between 2 and 4.");
            return;
        }
        int count = scanner.nextInt();
        scanner.nextLine(); // Consume the rest of the first input line.
        try {
            new GameModel(count, new Board(Board.SIZE, Board.SIZE), new TileBag(),
                    new WordList(), new Scorer(), scanner).play();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            System.out.println("Cannot start game: " + exception.getMessage());
        }
    }
}
