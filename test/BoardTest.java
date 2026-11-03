import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Board class. Checks that tiles can be placed on and read from the board, that the board
 * knows when it is empty, that word placements are checked properly, that words are placed
 * in the right squares/order and that the board prints correctly.
 *  @author Jordyn Bellamy
 *  @version Version 1.0 November 2
 */

public class BoardTest {

    //The centre of a standard 15 x 15 board is the square at row 7, column 7
    private static final int CENTRE = 7;

    private Board board;

    /**
     * Creates a new empty standard size board before each test
     */
    @BeforeEach
    public void setUp() {

        board = new Board(Board.SIZE, Board.SIZE);
    }

    /**
     * Helper method makeTiles. Takes a given word and makes a list of tiles, one tile per letter.
     * All tiles are currently worth 1 point since points aren't being tested.
     *
     * @param letters the letters to be turned into tiles
     * @return a list of the tiles in the same order as the word
     */
    private List<Tile> makeTiles(String letters) {
        List<Tile> tiles = new ArrayList<>();
        for (int i = 0; i < letters.length(); i++) {
            tiles.add(new Tile(letters.charAt(i), 1)); //Get letter at index and assign it 1 point
        }
        return tiles;
    }

    /**
     * Tests that a new board is empty with no tiles on it, and that it is
     * no longer empty once a word has been placed
     */
    @Test
    public void testIsEmpty() {
        assertTrue(board.isEmpty());

        board.placeWord(makeTiles("TREE"), CENTRE, CENTRE, true);
        assertFalse(board.isEmpty());
    }

    /**
     * Tests that getTile returns null for empty squares and squares outside of the board,
     * and returns the right tile once one has been placed
     */
    @Test
    public void testGetTile() {
        //Test default board
        Board standard = new Board();
        assertTrue(standard.isEmpty());
        assertNull(standard.getTile(Board.SIZE - 1, Board.SIZE - 1));

        //Empty squares return null
        assertNull(board.getTile(0, 0));
        assertNull(board.getTile(CENTRE, CENTRE));

        //Squares outside of the board return null
        assertNull(board.getTile(-1, 0));
        assertNull(board.getTile(0, -1));
        assertNull(board.getTile(Board.SIZE, 0));
        assertNull(board.getTile(0, Board.SIZE));

        //A square with a tile on it returns tile letter
        board.placeWord(makeTiles("A"), CENTRE, CENTRE, true);
        assertNotNull(board.getTile(CENTRE, CENTRE));
        assertEquals('A', board.getTile(CENTRE, CENTRE).getLetter());
    }

    /**
     * Tests that a null word or an empty word can never be placed
     */
    @Test
    public void testInvalidNullOrEmptyWord() {
        assertFalse(board.isValidPlacement(null, CENTRE, CENTRE, true));
        assertFalse(board.isValidPlacement("", CENTRE, CENTRE, true));
    }

    /**
     * Tests that the first word of the game is valid when it covers the centre square,
     * going both left to right and top to bottom.
     * First word placed is not valid if it doesn't cover the centre square.
     */
    @Test
    public void testFirstWordCentre() {
        // Covers the centre square
        assertTrue(board.isValidPlacement("DOG", CENTRE, CENTRE, true));
        assertTrue(board.isValidPlacement("DOG", CENTRE, 6, true));
        assertTrue(board.isValidPlacement("DOG", 5, CENTRE, false));

        //misses the centre square
        assertFalse(board.isValidPlacement("BLUE", 0, 0, true));
        assertFalse(board.isValidPlacement("BLUE", CENTRE, 8, true));
    }

    /**
     * Tests that a word is not valid if it starts outside of the board or
     * goes past the parameters of the board
     */
    @Test
    public void testInvalidWordOffBoard() {
        //word starts off of the board, should be false
        assertFalse(board.isValidPlacement("TEN", -1, CENTRE, true));
        assertFalse(board.isValidPlacement("TEN", CENTRE, Board.SIZE, true));

        //place a first word so the centre rule won't affect the next test
        board.placeWord(makeTiles("TEN"), CENTRE, CENTRE, true);

        //If word runs off the right edge or the bottom edge should be false
        assertFalse(board.isValidPlacement("TEN", 0, 13, true));
        assertFalse(board.isValidPlacement("TEN", 13, 0, false));
    }

    /**
     * Tests that a word can go through a tile already on the board when the letters match
     * lowercase or uppercase, and cannot cross through it when the letters are different
     */
    @Test
    public void testWordThroughExistingTile() {
        board.placeWord(makeTiles("BAR"), CENTRE, 6, true);//A is at the centre of the board which is row 7, column 7

        assertTrue(board.isValidPlacement("CAR", 6, CENTRE, false));//The A in CAR lands on the A of BAR
        assertTrue(board.isValidPlacement("car", 6, CENTRE, false));//Lowercase still matches
        assertFalse(board.isValidPlacement("BIT", 6, CENTRE, false));// I of BIT would not land on the A
    }

    /**
     * Tests that placeWord puts letters in the right squares going left to right and top to bottom,
     * skips squares that already have a tile, and stops at the edge of the board instead of crashing
     */
    @Test
    public void testPlaceWord() {
        //Left to right: each letter goes in the next column of the same row
        board.placeWord(makeTiles("BAR"), CENTRE, 6, true);
        assertEquals('B', board.getTile(CENTRE, 6).getLetter());
        assertEquals('A', board.getTile(CENTRE, 7).getLetter());
        assertEquals('R', board.getTile(CENTRE, 8).getLetter());
        assertNull(board.getTile(CENTRE, 9));//Nothing placed after the last letter

        //top to bottom through the A of BAR
        board.placeWord(makeTiles("BAR"), 6, CENTRE, false);
        assertEquals('B', board.getTile(6, CENTRE).getLetter());
        assertEquals('A', board.getTile(7, CENTRE).getLetter());//The A from CAT is still there
        assertEquals('R', board.getTile(8, CENTRE).getLetter());

        //Near the right edge: only C and A fit
        board.placeWord(makeTiles("CAR"), 0, 13, true);
        assertEquals('C', board.getTile(0, 13).getLetter());
        assertEquals('A', board.getTile(0, 14).getLetter());
    }

    /**
     * Tests that an empty board prints the centre square marker and the right number of lines,
     * that placed letters show up, and the centre marker goes away once a tile covers the centre square
     */
    @Test
    public void testToString() {
        String text = board.toString();
        assertTrue(text.contains("*"));
        assertEquals(1 + Board.SIZE * 2 + 1, text.split("\n").length);

        board.placeWord(makeTiles("BAR"), CENTRE, 6, true);
        text = board.toString();
        assertTrue(text.contains("| B | A | R |"));
        assertFalse(text.contains("*"));
    }
}
