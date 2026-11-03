import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests the team's Scorer API independently of unfinished Board/Tile methods. */
public class ScorerTest {
    private final Scorer scorer = new Scorer();

    @ParameterizedTest
    @CsvSource({"A,1","B,3","C,3","D,2","E,1","F,4","G,2","H,4",
        "I,1","J,8","K,5","L,1","M,3","N,1","O,1","P,3","Q,10",
        "R,1","S,1","T,1","U,1","V,4","W,4","X,8","Y,4","Z,10"})
    /** Verifies scores every new letter. */
    void scoresEveryNewLetter(char letter, int value) {
        assertEquals(value + 1, scorer.scoreMove(new TestBoard(), "A" + letter, 7, 7, true));
        assertEquals(value + 1, scorer.scoreMove(new TestBoard(),
                "a" + Character.toLowerCase(letter), 7, 7, true));
    }

    /** Verifies horizontal opening. */
    @Test void horizontalOpening() {
        TestBoard board = new TestBoard();
        assertEquals(Arrays.asList("CAT"), scorer.getWordsFormed(board, "cat", 7, 6, true));
        assertEquals(5, scorer.scoreMove(board, "cat", 7, 6, true));
        assertNull(board.getTile(7, 6));
    }
    /** Verifies vertical opening. */
    @Test void verticalOpening() {
        assertEquals(Arrays.asList("DOG"), scorer.getWordsFormed(new TestBoard(), "DOG", 6, 7, false));
        assertEquals(5, scorer.scoreMove(new TestBoard(), "DOG", 6, 7, false));
    }
    /** Verifies extends existing word. */
    @Test void extendsExistingWord() {
        TestBoard board = new TestBoard();
        board.word("CAT", 7, 6, true);
        assertEquals(Arrays.asList("CATS"), scorer.getWordsFormed(board, "S", 7, 9, true));
        assertEquals(6, scorer.scoreMove(board, "S", 7, 9, true));
    }
    /** Verifies includes prefix and suffix outside input. */
    @Test void includesPrefixAndSuffixOutsideInput() {
        TestBoard board = new TestBoard();
        board.tile(7, 6, 'C', 3); board.tile(7, 8, 'T', 1);
        assertEquals(Arrays.asList("CAT"), scorer.getWordsFormed(board, "A", 7, 7, true));
        assertEquals(5, scorer.scoreMove(board, "A", 7, 7, true));
    }
    /** Verifies reuses existing letters. */
    @Test void reusesExistingLetters() {
        TestBoard board = new TestBoard(); board.word("CAT", 7, 6, true);
        assertEquals(Arrays.asList("ART"), scorer.getWordsFormed(board, "ART", 7, 7, false));
        assertEquals(3, scorer.scoreMove(board, "ART", 7, 7, false));
        assertEquals('A', board.getTile(7, 7).getLetter());
        assertNull(board.getTile(8, 7));
    }
    /** Verifies collects main and multiple crosswords. */
    @Test void collectsMainAndMultipleCrosswords() {
        TestBoard board = new TestBoard(); board.word("AT", 7, 7, true);
        assertEquals(Arrays.asList("NO", "AN", "TO"), scorer.getWordsFormed(board, "NO", 8, 7, true));
        assertEquals(6, scorer.scoreMove(board, "NO", 8, 7, true));
    }
    /** Verifies collects vertical main and horizontal crosswords. */
    @Test void collectsVerticalMainAndHorizontalCrosswords() {
        TestBoard board = new TestBoard(); board.word("AT", 7, 7, false);
        assertEquals(Arrays.asList("NO", "AN", "TO"), scorer.getWordsFormed(board, "NO", 7, 8, false));
        assertEquals(6, scorer.scoreMove(board, "NO", 7, 8, false));
    }
    /** Verifies single tile forms two words. */
    @Test void singleTileFormsTwoWords() {
        TestBoard board = new TestBoard(); board.word("AT", 7, 7, true);
        board.tile(8, 7, 'N', 1);
        assertEquals(Arrays.asList("NO", "TO"), scorer.getWordsFormed(board, "O", 8, 8, true));
        assertEquals(4, scorer.scoreMove(board, "O", 8, 8, true));
        assertEquals(4, scorer.scoreMove(board, "O", 8, 8, false));
    }
    /** Verifies single tile only forms perpendicular word. */
    @Test void singleTileOnlyFormsPerpendicularWord() {
        TestBoard board = new TestBoard(); board.tile(7, 7, 'A', 1);
        assertEquals(Arrays.asList("AT"), scorer.getWordsFormed(board, "T", 8, 7, true));
        assertEquals(2, scorer.scoreMove(board, "T", 8, 7, true));
    }
    /** Verifies identical words are counted at different positions. */
    @Test void identicalWordsAreCountedAtDifferentPositions() {
        TestBoard board = new TestBoard(); board.word("IN", 7, 7, true);
        assertEquals(Arrays.asList("NO", "IN", "NO"), scorer.getWordsFormed(board, "NO", 8, 7, true));
        assertEquals(6, scorer.scoreMove(board, "NO", 8, 7, true));
    }
    /** Verifies reads existing tile point values. */
    @Test void readsExistingTilePointValues() {
        TestBoard board = new TestBoard(); board.tile(7, 7, 'A', 4);
        assertEquals(5, scorer.scoreMove(board, "T", 7, 8, true));
    }
    /** Verifies bingo counts new tiles. */
    @Test void bingoCountsNewTiles() {
        assertEquals(59, scorer.scoreMove(new TestBoard(), "READING", 7, 4, true));
        TestBoard board = new TestBoard(); board.word("READIN", 7, 4, true);
        assertEquals(9, scorer.scoreMove(board, "READING", 7, 4, true));
    }
    /** Verifies seven new tiles can make an eight letter word. */
    @Test void sevenNewTilesCanMakeAnEightLetterWord() {
        TestBoard board = new TestBoard(); board.word("EX", 6, 4, true);
        assertEquals(68, scorer.scoreMove(board, "EXCITING", 6, 4, false));
    }
    /** Verifies bingo is added once with crossword. */
    @Test void bingoIsAddedOnceWithCrossword() {
        TestBoard board = new TestBoard(); board.tile(6, 4, 'A', 1);
        assertEquals(61, scorer.scoreMove(board, "READING", 7, 4, true));
    }
    /** Verifies reaches last row and column. */
    @Test void reachesLastRowAndColumn() {
        assertEquals(5, scorer.scoreMove(new TestBoard(), "CAT", 14, 12, true));
        assertEquals(5, scorer.scoreMove(new TestBoard(), "CAT", 12, 14, false));
    }
    /** Verifies handles first row and column. */
    @Test void handlesFirstRowAndColumn() {
        assertEquals(5, scorer.scoreMove(new TestBoard(), "CAT", 0, 0, true));
        assertEquals(5, scorer.scoreMove(new TestBoard(), "CAT", 0, 0, false));
    }
    /** Verifies output lists do not affect later calls. */
    @Test void outputListsDoNotAffectLaterCalls() {
        scorer.getWordsFormed(new TestBoard(), "CAT", 7, 7, true).clear();
        assertEquals(Arrays.asList("DOG"), scorer.getWordsFormed(new TestBoard(), "DOG", 7, 7, true));
    }
    @ParameterizedTest @NullAndEmptySource
    @ValueSource(strings={" ","A1","A?","A-B","ice cream","C\u00c1T","\u00dfA","ABCDEFGHIJKLMNOP"})
    /** Verifies rejects malformed words. */
    void rejectsMalformedWords(String word) {
        assertThrows(IllegalArgumentException.class, () -> scorer.scoreMove(new TestBoard(), word, 7, 7, true));
        assertThrows(IllegalArgumentException.class, () -> scorer.getWordsFormed(new TestBoard(), word, 7, 7, true));
    }
    @ParameterizedTest
    @CsvSource({"-1,0,true","0,-1,true","15,0,true","0,15,false","14,14,true","14,14,false","2147483647,0,true"})
    /** Verifies rejects invalid coordinates. */
    void rejectsInvalidCoordinates(int row, int col, boolean across) {
        assertThrows(IllegalArgumentException.class, () -> scorer.scoreMove(new TestBoard(), "AT", row, col, across));
    }
    /** Verifies rejects null board. */
    @Test void rejectsNullBoard() {
        assertThrows(IllegalArgumentException.class, () -> scorer.scoreMove(null, "AT", 7, 7, true));
    }
    /** Verifies rejects overwrite. */
    @Test void rejectsOverwrite() {
        TestBoard board = new TestBoard(); board.word("CAT", 7, 6, true);
        assertThrows(IllegalArgumentException.class, () -> scorer.scoreMove(board, "DOG", 7, 6, true));
        assertEquals('C', board.getTile(7, 6).getLetter());
    }
    /** Verifies rejects no new tiles. */
    @Test void rejectsNoNewTiles() {
        TestBoard board = new TestBoard(); board.word("CAT", 7, 6, true);
        assertThrows(IllegalArgumentException.class, () -> scorer.scoreMove(board, "CAT", 7, 6, true));
    }
    /** Verifies rejects eight new tiles. */
    @Test void rejectsEightNewTiles() {
        assertThrows(IllegalArgumentException.class, () -> scorer.scoreMove(new TestBoard(), "READINGS", 7, 3, true));
    }
    /** Verifies rejects single isolated tile. */
    @Test void rejectsSingleIsolatedTile() {
        assertThrows(IllegalArgumentException.class, () -> scorer.scoreMove(new TestBoard(), "A", 7, 7, true));
    }
    /** Verifies rejects invalid existing tile. */
    @Test void rejectsInvalidExistingTile() {
        TestBoard board = new TestBoard(); board.tile(7, 7, '?', 0);
        assertThrows(IllegalArgumentException.class, () -> scorer.scoreMove(board, "AT", 6, 6, true));
    }
    /** Verifies rejects negative existing points. */
    @Test void rejectsNegativeExistingPoints() {
        TestBoard board = new TestBoard(); board.tile(7, 7, 'A', -1);
        assertThrows(IllegalArgumentException.class, () -> scorer.scoreMove(board, "AT", 6, 6, true));
    }

    /** Test double: the repository's main Board implementation is still a stub. */
    private static class TestBoard extends Board {
        private final Tile[][] cells = new Tile[SIZE][SIZE];
        @Override public Tile getTile(int row, int col) { return cells[row][col]; }
        /** Verifies tile. */
        void tile(int row, int col, char letter, int points) { cells[row][col] = new TestTile(letter, points); }
        /** Verifies word. */
        void word(String text, int row, int col, boolean across) {
            String groups = "AEILNORSTU|DG|BCMP|FHVWY|K|JX|QZ";
            int[] values = {1,2,3,4,5,8,10};
            String[] letters = groups.split("\\|");
            for (int i = 0; i < text.length(); i++) {
                char letter = text.charAt(i);
                int value = 0;
                for (int group = 0; group < letters.length; group++) {
                    if (letters[group].indexOf(letter) >= 0) value = values[group];
                }
                tile(row, col, letter, value);
                if (across) col++; else row++;
            }
        }
    }
    /** Supplies real letter/point data even while main's Tile class is a stub. */
    private static class TestTile extends Tile {
        private final char letter;
        private final int points;
        TestTile(char letter, int points) { super(letter, points); this.letter = letter; this.points = points; }
        @Override public char getLetter() { return letter; }
        @Override public int getPoints() { return points; }
    }
}
