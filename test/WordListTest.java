import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the WordList class using the project's wordlist.txt file.
 */
public class WordListTest {
    private WordList wordList;

    /**
     * Loads the word list before each test.
     */
    @BeforeEach
    public void setUp() {
        wordList = new WordList();
    }

    /**
     * Tests that a word in the dictionary is accepted.
     */
    @Test
    public void testValidWord() {
        assertTrue(wordList.isValidWord("CAT"));
    }

    /**
     * Tests that a word can be entered in lowercase.
     */
    @Test
    public void testLowercaseWord() {
        assertTrue(wordList.isValidWord("cat"));
    }

    /**
     * Tests that a word outside the dictionary is rejected.
     */
    @Test
    public void testInvalidWord() {
        assertFalse(wordList.isValidWord("ZZZZZZZZZZZZZZZ"));
    }

    /**
     * Tests that null and punctuation are rejected.
     */
    @Test
    public void testInvalidInput() {
        assertFalse(wordList.isValidWord(null));
        assertFalse(wordList.isValidWord("CAT!"));
    }

    /**
     * Tests that the dictionary loaded some words.
     */
    @Test
    public void testSize() {
        assertTrue(wordList.size() > 0);
    }
}
