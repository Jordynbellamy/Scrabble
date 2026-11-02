import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Loads the game's dictionary and checks words without regard to letter case.
 * Only words of 2 to 15 ASCII letters are stored. The dictionary is loaded once
 * per instance and is not changed while a game is running.
 *
 * @author Iniodu Jackson
 */
public class WordList {
    private static final String DEFAULT_FILE = "wordlist.txt";
    private static final int MIN_WORD_LENGTH = 2;
    private static final int MAX_WORD_LENGTH = 15;

    private final Set<String> words = new HashSet<>();

    /**
     * Loads wordlist.txt from the classpath, or from the working directory if
     * the resource is absent. This constructor matches the supplied example.
     *
     * @throws IllegalStateException if the dictionary cannot be read or contains
     *         no usable words
     */
    public WordList() {
        try (InputStream input = openDefaultDictionary();
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(input, StandardCharsets.UTF_8))) {
            loadWords(reader);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not load wordlist.txt. Put it on the classpath "
                            + "or in the working directory.", exception);
        }
    }

    /**
     * Loads a UTF-8 dictionary with one word per line from a selected file.
     * Duplicate, blank, and malformed entries are ignored.
     *
     * @param dictionaryFile the dictionary file
     * @throws IllegalArgumentException if dictionaryFile is null
     * @throws IOException if the file cannot be read or has no usable words
     */
    public WordList(Path dictionaryFile) throws IOException {
        if (dictionaryFile == null) {
            throw new IllegalArgumentException("Dictionary path must not be null.");
        }
        try (BufferedReader reader = Files.newBufferedReader(
                dictionaryFile, StandardCharsets.UTF_8)) {
            loadWords(reader);
        }
    }

    /**
     * Checks whether a word is in the loaded dictionary. Surrounding whitespace
     * is ignored; spaces or punctuation within a word are not accepted.
     *
     * @param word the word to check
     * @return true if the normalized word occurs in the dictionary; false for
     *         null, malformed, or unknown words
     */
    public boolean isValidWord(String word) {
        String normalized = normalize(word);
        return normalized != null && words.contains(normalized);
    }

    /**
     * Returns the number of distinct usable entries loaded from the dictionary.
     *
     * @return the number of stored words
     */
    public int size() {
        return words.size();
    }

    /**
     * Opens the packaged resource before trying a file in the working directory.
     *
     * @return the dictionary stream, which the caller must close
     * @throws IOException if neither dictionary source can be opened
     */
    private InputStream openDefaultDictionary() throws IOException {
        InputStream resource = WordList.class.getResourceAsStream("/" + DEFAULT_FILE);
        if (resource != null) {
            return resource;
        }
        return Files.newInputStream(Paths.get(DEFAULT_FILE));
    }

    /**
     * Reads and normalizes the entries in a dictionary.
     *
     * @param reader the open dictionary reader
     * @throws IOException if reading fails or no usable entries are found
     */
    private void loadWords(BufferedReader reader) throws IOException {
        String line;
        boolean firstLine = true;
        while ((line = reader.readLine()) != null) {
            if (firstLine && line.startsWith("\uFEFF")) {
                line = line.substring(1); // Accept an optional UTF-8 byte order mark.
            }
            firstLine = false;
            String normalized = normalize(line);
            if (normalized != null) {
                words.add(normalized);
            }
        }
        if (words.isEmpty()) {
            throw new IOException("The dictionary contains no usable words.");
        }
    }

    /**
     * Normalizes an ASCII word after checking its length and characters.
     *
     * @param word the candidate word
     * @return the uppercase word, or null if its format is invalid
     */
    private String normalize(String word) {
        if (word == null) {
            return null;
        }
        String trimmed = word.trim();
        if (trimmed.length() < MIN_WORD_LENGTH || trimmed.length() > MAX_WORD_LENGTH) {
            return null;
        }
        for (int index = 0; index < trimmed.length(); index++) {
            char letter = trimmed.charAt(index);
            if (!(letter >= 'A' && letter <= 'Z')
                    && !(letter >= 'a' && letter <= 'z')) {
                return null;
            }
        }
        return trimmed.toUpperCase(Locale.ROOT);
    }
}
