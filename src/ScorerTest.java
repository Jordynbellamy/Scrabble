import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests letter values, word scores, and scoring from board snapshots. */
public class ScorerTest {
    private final Scorer scorer = new Scorer();

    @ParameterizedTest
    @CsvSource({"A,1", "B,3", "C,3", "D,2", "E,1", "F,4", "G,2",
            "H,4", "I,1", "J,8", "K,5", "L,1", "M,3", "N,1",
            "O,1", "P,3", "Q,10", "R,1", "S,1", "T,1", "U,1",
            "V,4", "W,4", "X,8", "Y,4", "Z,10"})
    void usesTheEnglishValueForEveryLetter(char letter, int expected) {
        assertEquals(expected, scorer.getLetterValue(letter));
        assertEquals(expected, scorer.getLetterValue(Character.toLowerCase(letter)));
    }

    @ParameterizedTest
    @ValueSource(chars = {' ', '-', '?', '*', '1', '\u00E9', '\u00DF', '\0'})
    void rejectsUnsupportedTiles(char tile) {
        assertThrows(IllegalArgumentException.class, () -> scorer.getLetterValue(tile));
    }

    @ParameterizedTest
    @CsvSource({"CAT,5", "DOG,5", "HELLO,8", "QUIZ,22", "JAZZ,29",
            "reading,9", "ExAmPlE,18", "A,1"})
    void addsLetterValuesWithoutWordMultipliersOrBonuses(String word, int expected) {
        assertEquals(expected, scorer.calculateWordScore(word));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "CAT!", "ICE CREAM", "CAT1", "\u00E9", "\u00DF"})
    void rejectsMalformedWordScoreInput(String word) {
        assertThrows(IllegalArgumentException.class, () -> scorer.calculateWordScore(word));
    }

    @Test
    void scoresAHorizontalOpeningWithoutADoubleCentreSquare() {
        char[][] before = emptyBoard();
        char[][] after = emptyBoard();
        putWord(after, "CAT", 7, 6, true);
        assertEquals(5, scorer.calculateMoveScore(before, after));
    }

    @Test
    void scoresAVerticalWord() {
        char[][] before = emptyBoard();
        char[][] after = emptyBoard();
        putWord(after, "DOG", 6, 7, false);
        assertEquals(5, scorer.calculateMoveScore(before, after));
    }

    @Test
    void includesExistingLettersWhenExtendingAWord() {
        char[][] before = emptyBoard();
        putWord(before, "CAT", 7, 6, true);
        char[][] after = copy(before);
        after[7][9] = 'S';
        assertEquals(6, scorer.calculateMoveScore(before, after));
    }

    @Test
    void scoresAVerticalWordSharingAnExistingLetter() {
        char[][] before = emptyBoard();
        putWord(before, "CAT", 7, 6, true);
        char[][] after = copy(before);
        putWord(after, "ART", 7, 7, false);
        assertEquals(3, scorer.calculateMoveScore(before, after));
    }

    @Test
    void scoresBothWordsFormedByOneNewTile() {
        char[][] before = emptyBoard();
        putWord(before, "AT", 7, 7, true);
        before[8][7] = 'N';
        char[][] after = copy(before);
        after[8][8] = 'O';
        // The new O creates NO and TO. Existing AT and AN are not rescored.
        assertEquals(4, scorer.calculateMoveScore(before, after));
    }

    @Test
    void scoresTheMainWordAndAllNewCrosswordsOnce() {
        char[][] before = emptyBoard();
        putWord(before, "AT", 7, 7, true);
        char[][] after = copy(before);
        putWord(after, "NO", 8, 7, true);
        // NO (2), AN (2), and TO (2).
        assertEquals(6, scorer.calculateMoveScore(before, after));
    }

    @Test
    void countsIdenticalWordsAtDifferentPositionsSeparately() {
        char[][] before = emptyBoard();
        putWord(before, "IN", 7, 7, true);
        char[][] after = copy(before);
        putWord(after, "NO", 8, 7, true);
        // Horizontal NO, vertical IN, and vertical NO are distinct formed words.
        assertEquals(6, scorer.calculateMoveScore(before, after));
    }

    @Test
    void allowsExistingTilesBetweenNewTiles() {
        char[][] before = emptyBoard();
        before[7][7] = 'A';
        char[][] after = copy(before);
        after[7][6] = 'C';
        after[7][8] = 'T';
        assertEquals(5, scorer.calculateMoveScore(before, after));
    }

    @Test
    void addsTheSevenTileBonusOnce() {
        char[][] before = emptyBoard();
        char[][] after = emptyBoard();
        putWord(after, "READING", 7, 4, true);
        assertEquals(59, scorer.calculateMoveScore(before, after));
    }

    @Test
    void basesTheBonusOnNewTilesRatherThanWordLength() {
        char[][] before = emptyBoard();
        putWord(before, "READIN", 7, 4, true);
        char[][] after = copy(before);
        after[7][10] = 'G';
        assertEquals(9, scorer.calculateMoveScore(before, after));
    }

    @Test
    void awardsTheBonusWhenSevenNewTilesExtendAnExistingWord() {
        char[][] before = emptyBoard();
        putWord(before, "EX", 6, 4, true);
        char[][] after = copy(before);
        putWord(after, "EXCITING", 6, 4, false);
        // EXCITING is 18 points and uses seven new tiles, sharing its E with EX.
        assertEquals(68, scorer.calculateMoveScore(before, after));
    }

    @Test
    void leavesBothInputBoardsUnchanged() {
        char[][] before = emptyBoard();
        char[][] after = emptyBoard();
        putWord(after, "cat", 7, 6, true);
        char[][] originalBefore = copy(before);
        char[][] originalAfter = copy(after);
        assertEquals(5, scorer.calculateMoveScore(before, after));
        assertBoardsEqual(originalBefore, before);
        assertBoardsEqual(originalAfter, after);
    }

    @Test
    void scoresAnUnchangedBoardAsZero() {
        char[][] board = emptyBoard();
        putWord(board, "QUIZ", 7, 5, true);
        assertEquals(0, scorer.calculateMoveScore(board, copy(board)));
        assertEquals(0, scorer.calculateMoveScore(emptyBoard(), emptyBoard()));
    }

    @Test
    void treatsLetterCaseChangesAsTheSameExistingTile() {
        assertEquals(0, scorer.calculateMoveScore(
                new char[][] {{'a', 't'}}, new char[][] {{'A', 'T'}}));
    }

    @Test
    void supportsBothBoardEdgesAndRectangularBoards() {
        assertEquals(5, scorer.calculateMoveScore(
                new char[1][3], new char[][] {{'C', 'A', 'T'}}));
        assertEquals(5, scorer.calculateMoveScore(
                new char[3][1], new char[][] {{'D'}, {'O'}, {'G'}}));
    }

    @Test
    void acceptsSpaceAndDashEmptyMarkers() {
        assertEquals(2, scorer.calculateMoveScore(
                new char[][] {{' ', '-'}}, new char[][] {{'A', 'T'}}));
    }

    @Test
    void rejectsReplacingAnExistingTile() {
        assertThrows(IllegalArgumentException.class, () -> scorer.calculateMoveScore(
                new char[][] {{'A', 'T'}}, new char[][] {{'I', 'T'}}));
    }

    @Test
    void rejectsRemovingAnExistingTile() {
        assertThrows(IllegalArgumentException.class, () -> scorer.calculateMoveScore(
                new char[][] {{'A', 'T'}}, new char[][] {{'A', '\0'}}));
    }

    @Test
    void rejectsMoreThanSevenNewTiles() {
        char[][] after = emptyBoard();
        putWord(after, "READINGS", 7, 3, true);
        assertThrows(IllegalArgumentException.class,
                () -> scorer.calculateMoveScore(emptyBoard(), after));
    }

    @Test
    void rejectsNewTilesInDifferentRowsAndColumns() {
        char[][] after = emptyBoard();
        after[7][7] = 'A';
        after[8][8] = 'T';
        assertThrows(IllegalArgumentException.class,
                () -> scorer.calculateMoveScore(emptyBoard(), after));
    }

    @Test
    void rejectsAGapInAHorizontalMove() {
        char[][] after = emptyBoard();
        after[7][7] = 'A';
        after[7][9] = 'T';
        assertThrows(IllegalArgumentException.class,
                () -> scorer.calculateMoveScore(emptyBoard(), after));
    }

    @Test
    void rejectsAGapInAVerticalMove() {
        char[][] after = emptyBoard();
        after[7][7] = 'A';
        after[9][7] = 'T';
        assertThrows(IllegalArgumentException.class,
                () -> scorer.calculateMoveScore(emptyBoard(), after));
    }

    @Test
    void rejectsAnIsolatedSingleTile() {
        char[][] after = emptyBoard();
        after[7][7] = 'A';
        assertThrows(IllegalArgumentException.class,
                () -> scorer.calculateMoveScore(emptyBoard(), after));
    }

    @Test
    void rejectsNullEmptyRaggedAndNullRowCharacterBoards() {
        char[][] valid = new char[1][2];
        assertThrows(IllegalArgumentException.class,
                () -> scorer.calculateMoveScore((char[][]) null, valid));
        assertThrows(IllegalArgumentException.class,
                () -> scorer.calculateMoveScore(valid, (char[][]) null));
        assertThrows(IllegalArgumentException.class,
                () -> scorer.calculateMoveScore(new char[0][0], valid));
        assertThrows(IllegalArgumentException.class,
                () -> scorer.calculateMoveScore(new char[][] {{}}, valid));
        assertThrows(IllegalArgumentException.class,
                () -> scorer.calculateMoveScore(new char[][] {null}, valid));
        assertThrows(IllegalArgumentException.class,
                () -> scorer.calculateMoveScore(new char[][] {{'A'}, {}}, valid));
        assertThrows(IllegalArgumentException.class,
                () -> scorer.calculateMoveScore(new char[][] {{'A'}, null}, valid));
    }

    @Test
    void rejectsMismatchedBoardDimensions() {
        assertThrows(IllegalArgumentException.class,
                () -> scorer.calculateMoveScore(new char[1][2], new char[2][1]));
    }

    @Test
    void rejectsUnsupportedCharactersInsideBoards() {
        assertThrows(IllegalArgumentException.class, () -> scorer.calculateMoveScore(
                new char[1][2], new char[][] {{'A', '?'}}));
    }

    @Test
    void acceptsTheSuppliedExamplesPaddedStringCells() {
        String[][] before = {{"  -  ", "  -  ", "  -  "}};
        String[][] after = {{"  C  ", "  A  ", "  T  "}};
        assertEquals(5, scorer.calculateMoveScore(before, after));
        assertArrayEquals(new String[] {"  -  ", "  -  ", "  -  "}, before[0]);
        assertArrayEquals(new String[] {"  C  ", "  A  ", "  T  "}, after[0]);
    }

    @Test
    void acceptsNullEmptyAndWhitespaceStringCells() {
        String[][] before = {{null, "", "  "}};
        String[][] after = {{"c", "a", "t"}};
        assertEquals(5, scorer.calculateMoveScore(before, after));
    }

    @Test
    void rejectsInvalidStringCellContents() {
        String[][] before = {{null, null}};
        assertThrows(IllegalArgumentException.class,
                () -> scorer.calculateMoveScore(before, new String[][] {{"AT", "T"}}));
        assertThrows(IllegalArgumentException.class,
                () -> scorer.calculateMoveScore(before, new String[][] {{"?", "T"}}));
    }

    @Test
    void rejectsNullEmptyRaggedAndNullRowStringBoards() {
        String[][] valid = new String[1][2];
        assertThrows(IllegalArgumentException.class,
                () -> scorer.calculateMoveScore((String[][]) null, valid));
        assertThrows(IllegalArgumentException.class,
                () -> scorer.calculateMoveScore(valid, (String[][]) null));
        assertThrows(IllegalArgumentException.class,
                () -> scorer.calculateMoveScore(new String[0][0], valid));
        assertThrows(IllegalArgumentException.class,
                () -> scorer.calculateMoveScore(new String[][] {{}}, valid));
        assertThrows(IllegalArgumentException.class,
                () -> scorer.calculateMoveScore(new String[][] {null}, valid));
        assertThrows(IllegalArgumentException.class,
                () -> scorer.calculateMoveScore(new String[][] {{"A"}, {}}, valid));
        assertThrows(IllegalArgumentException.class,
                () -> scorer.calculateMoveScore(new String[][] {{"A"}, null}, valid));
    }

    private char[][] emptyBoard() {
        return new char[15][15];
    }

    private char[][] copy(char[][] board) {
        char[][] result = new char[board.length][];
        for (int row = 0; row < board.length; row++) {
            result[row] = board[row].clone();
        }
        return result;
    }

    private void putWord(char[][] board, String word, int row, int column, boolean horizontal) {
        for (int index = 0; index < word.length(); index++) {
            board[row][column] = word.charAt(index);
            if (horizontal) {
                column++;
            } else {
                row++;
            }
        }
    }

    private void assertBoardsEqual(char[][] expected, char[][] actual) {
        assertEquals(expected.length, actual.length);
        for (int row = 0; row < expected.length; row++) {
            assertArrayEquals(expected[row], actual[row]);
        }
    }
}
