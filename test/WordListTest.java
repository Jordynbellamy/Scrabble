import static org.junit.jupiter.api.Assertions.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests the WordList(String) and isValid(String) interfaces used by the team. */
public class WordListTest {
    @TempDir Path directory;
    @ParameterizedTest @ValueSource(strings={"CAT","cat","CaT"," cat ","\tcat\t"})
    /** Verifies normalizes lookup. */
    void normalizesLookup(String candidate) throws Exception {
        assertTrue(dictionary("cat", "dog").isValid(candidate));
    }
    @ParameterizedTest @NullAndEmptySource
    @ValueSource(strings={" ","A","C AT","CAT!","CAT1","C\u00c1T","\u00df","ABCDEFGHIJKLMNOP","MOUSE"})
    /** Verifies rejects malformed and absent words. */
    void rejectsMalformedAndAbsentWords(String candidate) throws Exception {
        assertFalse(dictionary("CAT", "DOG", "SS").isValid(candidate));
    }
    /** Verifies skips malformed lines and duplicates. */
    @Test void skipsMalformedLinesAndDuplicates() throws Exception {
        WordList words = dictionary("cat", "CAT", " dog ", "a", "", "ice cream", "123", "C\u00c1T");
        assertTrue(words.isValid("cat")); assertTrue(words.isValid("DOG"));
        assertFalse(words.isValid("ice cream"));
    }
    /** Verifies accepts byte order mark. */
    @Test void acceptsByteOrderMark() throws Exception {
        assertTrue(dictionary("\uFEFFcat", "dog").isValid("CAT"));
    }
    /** Verifies accepts length boundaries. */
    @Test void acceptsLengthBoundaries() throws Exception {
        WordList words = dictionary("AT", "ABCDEFGHIJKLMNO");
        assertTrue(words.isValid("AT")); assertTrue(words.isValid("abcdefghijklmno"));
    }
    /** Verifies missing file fails clearly. */
    @Test void missingFileFailsClearly() {
        assertThrows(IllegalStateException.class, () -> new WordList(directory.resolve("missing.txt").toString()));
    }
    /** Verifies empty file fails clearly. */
    @Test void emptyFileFailsClearly() throws Exception {
        Path file = directory.resolve("empty.txt"); Files.write(file, new byte[0]);
        assertThrows(IllegalStateException.class, () -> new WordList(file.toString()));
    }
    /** Verifies unusable file fails clearly. */
    @Test void unusableFileFailsClearly() {
        assertThrows(IllegalStateException.class, () -> dictionary("", "a", "123"));
    }
    @ParameterizedTest @NullAndEmptySource @ValueSource(strings={"  "})
    /** Verifies invalid filename fails clearly. */
    void invalidFilenameFailsClearly(String filename) {
        assertThrows(IllegalArgumentException.class, () -> new WordList(filename));
    }
    /** Verifies lookup is independent of locale. */
    @Test void lookupIsIndependentOfLocale() throws Exception {
        Locale old = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr", "TR"));
            assertTrue(dictionary("in").isValid("in"));
        } finally { Locale.setDefault(old); }
    }
    /** Verifies loads bundled dictionary. */
    @Test void loadsBundledDictionary() {
        WordList words = new WordList("wordlist.txt");
        assertTrue(words.isValid("hello")); assertTrue(words.isValid("cat"));
        assertFalse(words.isValid("ZZZZZZZZZZZZZZZ"));
    }
    /** Verifies dictionary is loaded once. */
    @Test void dictionaryIsLoadedOnce() throws Exception {
        Path file = directory.resolve("once.txt");
        Files.write(file, Arrays.asList("cat"), StandardCharsets.UTF_8);
        WordList words = new WordList(file.toString());
        Files.delete(file);
        assertTrue(words.isValid("CAT"));
    }
    /** Verifies dictionary. */
    private WordList dictionary(String... entries) throws Exception {
        Path file = directory.resolve("words.txt");
        Files.write(file, Arrays.asList(entries), StandardCharsets.UTF_8);
        return new WordList(file.toString());
    }
}
