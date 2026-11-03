/**
 * The board that our Scrabble game is displayed on. It checks to make sure that any given tile can be placed on the board
 * at a specific spot and updates the board when a tile has been added.
 *
 * @author Jordyn Bellamy
 * @version Version 1.0 October 29
 */

import java.util.List;

public class Board {

    //The standard size of a Scrabble board row or column is 15 squares
    public static final int SIZE = 15;

    private int rows;

    private int columns;

    private Tile[][] grid;

    /**
     * The default constructor for the Scrabble board, creates an empty 15 x 15 board
     */
    public Board() {
        this(SIZE, SIZE);
    }

    /**
     * The constructor for the Scrabble board, creates an empty board with a given number of rows and columns
     *
     * @param rows number of rows on the board
     * @param columns number of columns on the board
     */
    public Board(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        this.grid = new Tile[rows][columns];
    }

    /**
     * Checks if a square is inside the board.
     *
     * @param row the row of the square
     * @param col the column of the square
     * @return true if the square is on the board, false if not
     */
    private boolean isOnBoard(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < columns;
    }
    /**
     * Returns the tile at a given square on the board
     *
     * @param row the row of the square
     * @param col the column of the square
     * @return the tile at that square, or null if the square is empty or outside of board dimensions
     */
    public Tile getTile(int row, int col) {
        if (!isOnBoard(row,col)) {
            return null;
        }
        return grid[row][col];
    }

    /**
     * Returns true if board has no tiles, false if has a tile
     * @return true if board is empty
     */
    public boolean isEmpty() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                if (grid[row][col] != null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks if a word can be placed on the board at a given square.
     * As per Scrabble official rules: Placement is valid if the word is not empty,
     * the whole word fits on the board,
     * any tiles already on the board match any letters in the word,
     * or the first word of the game covers the centre square
     *
     * @param word   the word to place
     * @param row    the row of the first letter
     * @param col    the column of the first letter
     * @param isHorizontal true if the word goes left to right, false if it goes top to bottom
     * @return true if the word can be placed there, false otherwise
     */
    public boolean isValidPlacement(String word, int row, int col, boolean isHorizontal) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        boolean coversCentre = false;

        //Check every square the word would use
        for (int i = 0; i < word.length(); i++) {
            int r = row;
            int c = col;
            if (isHorizontal) {
                c = col + i;
            } else {
                r = row + i;
            }

            if (!isOnBoard(r, c)) {
                return false;
            }
            //Tiles are upper case
            word = word.toUpperCase();

            //If a tile is already on this square, it must be the same letter
            Tile existing = grid[r][c];
            if (existing != null && existing.getLetter() != word.charAt(i)) {
                return false;
            }
            if (r == rows / 2 && c == columns / 2) {
                coversCentre = true;
            }
        }
        //The first word of the game must cover the centre square
        if (isEmpty() && !coversCentre) {
            return false;
        }
        return true;
    }

    /**
     * Places tiles on the board starting at any given square.
     *
     * @param tiles the new tiles to be placed on the board
     * @param row the row of the first square
     * @param col the column of the first square
     * @param isHorizontal true if the tiles go left to right, false if they go top to bottom
     */
    public void placeWord(List<Tile> tiles, int row, int col, boolean isHorizontal) {

        for (int i = 0; i < tiles.size(); i++) {
            int r = row;
            int c = col;

            if (isHorizontal) {
                c = col + i;
            } else {
                r = row + i;
            }

            //Stop if the word goes off the board
            if (!isOnBoard(r, c)) {
                return;
            }

            //Only place a tile if the square is empty, tiles already on the board stay
            if (grid[r][c] == null) {
                grid[r][c] = tiles.get(i);
            }
        }
    }

    /**
     * Returns the board as text so it can be printed.
     * Each square is drawn as a box. Empty squares are blank and the centre square
     * is shown as "*" when empty
     *
     * @return the board as a String
     */
    @Override
    public String toString() {
        //Column letters and the line between rows
        String board = "   ";
        String line = "   ";

        for (int col = 0; col < columns; col++) {
            board += String.format("  %-2c", (char) ('A' + col));
            line += "+---";
        }
        board += "\n";
        line += "+\n";

        for (int row = 0; row < rows; row++) {
            board += line;
            board += String.format("%2d ", row + 1); //Row number on the left

            for (int col = 0; col < columns; col++) {
                char letter = ' ';

                if (grid[row][col] != null) {
                    letter = grid[row][col].getLetter();

                } else if (row == rows / 2 && col == columns / 2) {
                    letter = '*';
                }

                board += "| " + letter + " ";
            }
            board += "|\n";
        }
        board += line;

        return board;
    }
}