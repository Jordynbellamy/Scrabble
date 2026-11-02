/**
 * Calculates English Scrabble scores for milestone 1. Every square has its
 * ordinary letter value; blank tiles and premium squares are not supported.
 * Player totals are stored by the game's Player class, not by this class.
 *
 * @author Iniodu Jackson
 */
public class Scorer {
    private static final int RACK_SIZE = 7;
    private static final int BINGO_BONUS = 50;

    /**
     * Creates a scorer with no player or board state.
     */
    public Scorer() {
    }

    /**
     * Returns an English tile's face value. Lowercase letters are accepted.
     *
     * @param letter an ASCII letter from A to Z or a to z
     * @return the letter's value, from 1 to 10
     * @throws IllegalArgumentException if letter is not an ASCII letter
     */
    public int getLetterValue(char letter) {
        switch (letter) {
            case 'A': case 'a': case 'E': case 'e': case 'I': case 'i':
            case 'L': case 'l': case 'N': case 'n': case 'O': case 'o':
            case 'R': case 'r': case 'S': case 's': case 'T': case 't':
            case 'U': case 'u':
                return 1;
            case 'D': case 'd': case 'G': case 'g':
                return 2;
            case 'B': case 'b': case 'C': case 'c': case 'M': case 'm':
            case 'P': case 'p':
                return 3;
            case 'F': case 'f': case 'H': case 'h': case 'V': case 'v':
            case 'W': case 'w': case 'Y': case 'y':
                return 4;
            case 'K': case 'k':
                return 5;
            case 'J': case 'j': case 'X': case 'x':
                return 8;
            case 'Q': case 'q': case 'Z': case 'z':
                return 10;
            default:
                throw new IllegalArgumentException("Tile must be a letter from A to Z.");
        }
    }

    /**
     * Adds the face values in a word. This method does not check the dictionary
     * or award a seven-tile bonus, since word length does not identify new tiles.
     *
     * @param word a nonempty sequence of ASCII letters
     * @return the sum of the letter values
     * @throws IllegalArgumentException if word is null, empty, or has nonletters
     */
    public int calculateWordScore(String word) {
        if (word == null || word.isEmpty()) {
            throw new IllegalArgumentException("Word must not be null or empty.");
        }
        int score = 0;
        for (int index = 0; index < word.length(); index++) {
            score += getLetterValue(word.charAt(index));
        }
        return score;
    }

    /**
     * Scores the words created or extended between two board snapshots. Each
     * horizontal or vertical word containing a new tile is counted once, with
     * existing letters included. Playing seven new tiles adds 50 points once.
     * An unchanged board scores zero, for example when a player exchanges tiles.
     * Neither input board is modified.
     *
     * The caller must validate dictionary membership, board connectivity, the
     * opening centre square, and ownership of the played tiles before awarding
     * this score. This method checks snapshot shape, tile preservation, and that
     * new tiles form one unbroken row or column.
     *
     * @param before the board before the move; NUL, space, or '-' means empty
     * @param after the candidate board after the move, using the same format
     * @return the turn score, including any seven-tile bonus
     * @throws IllegalArgumentException for malformed boards, changed old tiles,
     *         more than seven new tiles, a broken line, or no formed word
     */
    public int calculateMoveScore(char[][] before, char[][] after) {
        char[][] oldBoard = normalizeBoard(before);
        char[][] newBoard = normalizeBoard(after);
        if (oldBoard.length != newBoard.length
                || oldBoard[0].length != newBoard[0].length) {
            throw new IllegalArgumentException("Board dimensions must match.");
        }

        int placed = 0;
        int firstRow = -1;
        int firstColumn = -1;
        int lastRow = -1;
        int lastColumn = -1;
        boolean sameRow = true;
        boolean sameColumn = true;
        for (int row = 0; row < oldBoard.length; row++) {
            for (int column = 0; column < oldBoard[row].length; column++) {
                char oldLetter = oldBoard[row][column];
                char newLetter = newBoard[row][column];
                if (oldLetter != '\0' && oldLetter != newLetter) {
                    throw new IllegalArgumentException("Existing tiles cannot be changed or removed.");
                }
                if (oldLetter == '\0' && newLetter != '\0') {
                    if (placed == 0) {
                        firstRow = row;
                        firstColumn = column;
                    }
                    sameRow = sameRow && row == firstRow;
                    sameColumn = sameColumn && column == firstColumn;
                    lastRow = row;
                    lastColumn = column;
                    placed++;
                }
            }
        }
        if (placed == 0) {
            return 0;
        }
        if (placed > RACK_SIZE) {
            throw new IllegalArgumentException("A turn cannot place more than seven tiles.");
        }
        if (!sameRow && !sameColumn) {
            throw new IllegalArgumentException("New tiles must share one row or column.");
        }
        if (sameRow) {
            for (int column = firstColumn; column <= lastColumn; column++) {
                if (newBoard[firstRow][column] == '\0') {
                    throw new IllegalArgumentException("A word cannot contain an empty square.");
                }
            }
        } else {
            for (int row = firstRow; row <= lastRow; row++) {
                if (newBoard[row][firstColumn] == '\0') {
                    throw new IllegalArgumentException("A word cannot contain an empty square.");
                }
            }
        }

        int score = scoreLines(oldBoard, newBoard, true)
                + scoreLines(oldBoard, newBoard, false);
        if (score == 0) {
            throw new IllegalArgumentException("A move must form a word of at least two letters.");
        }
        if (placed == RACK_SIZE) {
            score += BINGO_BONUS;
        }
        return score;
    }

    /**
     * Scores a move using String cells, as returned by getBoard() in the supplied
     * example. Cells may contain one letter with optional surrounding spaces.
     * Null, empty, whitespace-only, and '-' cells are treated as empty.
     * The same validation and scoring rules as the char board overload apply.
     *
     * @param before the board before the move
     * @param after the candidate board after the move
     * @return the turn score
     * @throws IllegalArgumentException if either board or the move is malformed
     */
    public int calculateMoveScore(String[][] before, String[][] after) {
        return calculateMoveScore(toCharBoard(before), toCharBoard(after));
    }

    /**
     * Scores complete runs containing at least one newly placed tile. Visiting
     * each run once prevents repeated scoring when several new tiles share it.
     *
     * @param before the normalized original board
     * @param after the normalized candidate board
     * @param horizontal true to scan rows; false to scan columns
     * @return the score of the affected words in this direction
     */
    private int scoreLines(char[][] before, char[][] after, boolean horizontal) {
        int lineCount = horizontal ? after.length : after[0].length;
        int lineLength = horizontal ? after[0].length : after.length;
        int total = 0;
        for (int line = 0; line < lineCount; line++) {
            int position = 0;
            while (position < lineLength) {
                int length = 0;
                int wordScore = 0;
                boolean hasNewTile = false;
                while (position < lineLength) {
                    int row = horizontal ? line : position;
                    int column = horizontal ? position : line;
                    char letter = after[row][column];
                    if (letter == '\0') {
                        break;
                    }
                    length++;
                    wordScore += getLetterValue(letter);
                    hasNewTile = hasNewTile || before[row][column] == '\0';
                    position++;
                }
                if (length > 1 && hasNewTile) {
                    total += wordScore;
                }
                position++; // Move past the empty cell or the end of the run.
            }
        }
        return total;
    }

    /**
     * Validates a rectangular board and makes an uppercase copy.
     *
     * @param board a board containing letters and supported empty markers
     * @return a new board with NUL for empty cells
     * @throws IllegalArgumentException if the board shape or a cell is invalid
     */
    private char[][] normalizeBoard(char[][] board) {
        if (board == null || board.length == 0
                || board[0] == null || board[0].length == 0) {
            throw new IllegalArgumentException("Board must be a nonempty rectangle.");
        }
        int columns = board[0].length;
        char[][] normalized = new char[board.length][columns];
        for (int row = 0; row < board.length; row++) {
            if (board[row] == null || board[row].length != columns) {
                throw new IllegalArgumentException("Board must be rectangular.");
            }
            for (int column = 0; column < columns; column++) {
                char letter = board[row][column];
                if (letter == '\0' || letter == ' ' || letter == '-') {
                    normalized[row][column] = '\0';
                } else {
                    getLetterValue(letter); // Reject unsupported tile characters.
                    normalized[row][column] = Character.toUpperCase(letter);
                }
            }
        }
        return normalized;
    }

    /**
     * Converts the example's padded String cells into a separate character board.
     *
     * @param board the String board
     * @return a new character board
     * @throws IllegalArgumentException if the board or any cell is malformed
     */
    private char[][] toCharBoard(String[][] board) {
        if (board == null || board.length == 0
                || board[0] == null || board[0].length == 0) {
            throw new IllegalArgumentException("Board must be a nonempty rectangle.");
        }
        int columns = board[0].length;
        char[][] converted = new char[board.length][columns];
        for (int row = 0; row < board.length; row++) {
            if (board[row] == null || board[row].length != columns) {
                throw new IllegalArgumentException("Board must be rectangular.");
            }
            for (int column = 0; column < columns; column++) {
                String cell = board[row][column];
                if (cell == null || cell.trim().isEmpty() || cell.trim().equals("-")) {
                    continue;
                }
                String letter = cell.trim();
                if (letter.length() != 1) {
                    throw new IllegalArgumentException("Each occupied cell must contain one letter.");
                }
                converted[row][column] = letter.charAt(0);
            }
        }
        return converted;
    }
}
