import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Scorer class.
 */
public class ScorerTest {
    private Scorer scorer;

    /**
     * Creates a new scorer before each test.
     */
    @BeforeEach
    public void setUp() {
        scorer = new Scorer();
    }

    /**
     * Tests the point value of a letter.
     */
    @Test
    public void testGetLetterValue() {
        assertEquals(1, scorer.getLetterValue('A'));
        assertEquals(3, scorer.getLetterValue('C'));
        assertEquals(10, scorer.getLetterValue('Q'));
    }

    /**
     * Tests the total score for a word.
     */
    @Test
    public void testCalculateWordScore() {
        assertEquals(5, scorer.calculateWordScore("CAT"));
    }

    /**
     * Tests the score of a word placed on an empty board.
     */
    @Test
    public void testCalculateMoveScore() {
        char[][] before = new char[15][15];
        char[][] after = new char[15][15];
        after[7][6] = 'C';
        after[7][7] = 'A';
        after[7][8] = 'T';

        assertEquals(5, scorer.calculateMoveScore(before, after));
    }

    /**
     * Tests that an existing letter is included when a word is extended.
     */
    @Test
    public void testExtendWord() {
        char[][] before = new char[15][15];
        char[][] after = new char[15][15];
        before[7][7] = 'A';
        after[7][7] = 'A';
        after[7][8] = 'T';

        assertEquals(2, scorer.calculateMoveScore(before, after));
    }

    /**
     * Tests the bonus for using seven new tiles.
     */
    @Test
    public void testSevenTileBonus() {
        char[][] before = new char[15][15];
        char[][] after = new char[15][15];
        String word = "READING";
        for (int i = 0; i < word.length(); i++) {
            after[7][4 + i] = word.charAt(i);
        }

        assertEquals(59, scorer.calculateMoveScore(before, after));
    }
}
