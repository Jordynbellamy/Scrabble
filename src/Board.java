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
     * @param across true if the word goes left to right, false if it goes top to bottom
     * @return true if the word can be placed there, false otherwise
     */
    public boolean isValidPlacement(String word, int row, int col, boolean across) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        boolean coversCentre = false;

        //Check every square the word would use
        for (int i = 0; i < word.length(); i++) {
            int r = row;
            int c = col;
        }
        return true;
    }
    public void placeWord(List<Tile> tiles, int row, int col, boolean across) {}
    @Override
    public String toString() { return ""; }
}
