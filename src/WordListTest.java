import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
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

/** Tests dictionary loading, normalization, and lookup. */
public class WordListTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void loadsTheDictionaryIncludedWithTheProject() {
        WordList dictionary = new WordList();
        assertEquals(9969, dictionary.size());
        assertTrue(dictionary.isValidWord("hello"));
        assertTrue(dictionary.isValidWord("word"));
        assertFalse(dictionary.isValidWord("ZZZZZZZZZZZZZZZ"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"cat", "CAT", "CaT", " cat ", "\tcat\t"})
    void lookupIgnoresCaseAndSurroundingWhitespace(String candidate) throws IOException {
        WordList dictionary = dictionaryWith("CAT", "DOG");
        assertTrue(dictionary.isValidWord(candidate));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "A", "C AT", "CAT!", "CAT1", "CAT-DOG",
            "C\u00C1T", "\u00DF", "ABCDEFGHIJKLMNOP", "MOUSE"})
    void rejectsMalformedOrAbsentWords(String candidate) throws IOException {
        WordList dictionary = dictionaryWith("CAT", "DOG", "SS");
        assertFalse(dictionary.isValidWord(candidate));
    }

    @Test
    void removesDuplicatesAndSkipsMalformedEntries() throws IOException {
        WordList dictionary = dictionaryWith(" cat ", "CAT", "dog", "", "a",
                "ice cream", "can't", "C3PO", "\u00E9t\u00E9", "ABCDEFGHIJKLMNOP");
        assertEquals(2, dictionary.size());
        assertTrue(dictionary.isValidWord("DOG"));
    }

    @Test
    void acceptsAnOptionalByteOrderMarkOnTheFirstLine() throws IOException {
        WordList dictionary = dictionaryWith("\uFEFFcat", "dog");
        assertEquals(2, dictionary.size());
        assertTrue(dictionary.isValidWord("cat"));
    }

    @Test
    void acceptsTwoAndFifteenLetterEntries() throws IOException {
        WordList dictionary = dictionaryWith("AT", "ABCDEFGHIJKLMNO");
        assertTrue(dictionary.isValidWord("at"));
        assertTrue(dictionary.isValidWord("abcdefghijklmno"));
        assertEquals(2, dictionary.size());
    }

    @Test
    void lookupDoesNotChangeTheDictionary() throws IOException {
        WordList dictionary = dictionaryWith("CAT");
        assertFalse(dictionary.isValidWord("DOG"));
        assertEquals(1, dictionary.size());
    }

    @Test
    void reportsAMissingFile() {
        Path missing = temporaryDirectory.resolve("missing.txt");
        assertThrows(IOException.class, () -> new WordList(missing));
    }

    @Test
    void rejectsANullPath() {
        assertThrows(IllegalArgumentException.class, () -> new WordList((Path) null));
    }

    @Test
    void reportsAnEmptyDictionary() throws IOException {
        Path file = temporaryDirectory.resolve("empty.txt");
        Files.write(file, new byte[0]);
        assertThrows(IOException.class, () -> new WordList(file));
    }

    @Test
    void reportsADictionaryWithNoUsableEntries() throws IOException {
        Path file = temporaryDirectory.resolve("invalid.txt");
        Files.write(file, Arrays.asList("", "a", "123", "two words"), StandardCharsets.UTF_8);
        assertThrows(IOException.class, () -> new WordList(file));
    }

    @Test
    void normalizationDoesNotDependOnTheSystemLocale() throws IOException {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr", "TR"));
            WordList dictionary = dictionaryWith("in");
            assertTrue(dictionary.isValidWord("in"));
            assertTrue(dictionary.isValidWord("IN"));
        } finally {
            Locale.setDefault(previous);
        }
    }

    private WordList dictionaryWith(String... words) throws IOException {
        Path file = temporaryDirectory.resolve("dictionary.txt");
        Files.write(file, Arrays.asList(words), StandardCharsets.UTF_8);
        return new WordList(file);
    }
}
