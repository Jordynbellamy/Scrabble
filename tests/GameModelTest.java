package Scrabble.tests;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Scanner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Milestone 1 GameModel integration tests.
 *
 * These require the implemented Board (not its current placeholders),
 * Player from feature/tiles, and WordList and Scorer from the board branch.
 */
public class GameModelTest {
    @TempDir
    Path tempDir;

    private static class ScriptedTileBag extends TileBag {
        private final Deque<Tile> tiles = new ArrayDeque<>();

        ScriptedTileBag(String letters) {
            for (char letter : letters.toCharArray()) {
                tiles.addLast(new Tile(letter, new Scorer().getLetterValue(letter)));
            }
        }

        @Override
        public List<Tile> draw(int n) {
            List<Tile> drawn = new ArrayList<>();
            while (n-- > 0 && !tiles.isEmpty()) {
                drawn.add(tiles.removeFirst());
            }
            return drawn;
        }

        @Override
        public void returnTiles(List<Tile> returned) {
            tiles.addAll(returned);
        }

        @Override
        public int size() {
            return tiles.size();
        }
    }

    private GameModel createGame() throws IOException {
        Path dictionary = tempDir.resolve("words.txt");
        Files.write(dictionary, Arrays.asList("HELLO", "HELL", "HE", "LO", "CAT", "AT", "TEA", "HAT"));
        // P1 starts with HELLOWR; P2 with CATENDS; bag retains ample tiles.
        ScriptedTileBag bag = new ScriptedTileBag(
                "HELLOWRCATENDS" + "AAAAAAAAAAAAAAAAAAAA");
        return new GameModel(2, new Board(15, 15), bag,
                new WordList(dictionary), new Scorer(), new Scanner(""));
    }

    @Test
    public void supportsTwoToFourPlayers() throws IOException {
        GameModel game = createGame();
        assertEquals(2, game.getPlayers().size());
        assertEquals(7, game.getCurrentPlayer().getRack().size());
        assertThrows(IllegalArgumentException.class,
                () -> new GameModel(1, game.getBoard(), new TileBag(),
                        new WordList(tempDir.resolve("words.txt")), new Scorer(), new Scanner("")));
        assertThrows(IllegalArgumentException.class,
                () -> new GameModel(5, game.getBoard(), new TileBag(),
                        new WordList(tempDir.resolve("words.txt")), new Scorer(), new Scanner("")));
    }

    @Test
    public void invalidOpeningDoesNotChangeState() throws IOException {
        GameModel game = createGame();
        assertFalse(game.playWord("9H", "HELLO"));
        assertEquals(0, game.getCurrentPlayerIndex());
        assertEquals(0, game.getCurrentPlayer().getScore());
        assertNull(game.getBoard().getTile(8, 7));
    }

    @Test
    public void validOpeningPlacesTilesScoresAndAdvances() throws IOException {
        GameModel game = createGame();
        assertTrue(game.playWord("8H", "HELLO"), game.getLastError());
        assertEquals('H', game.getBoard().getTile(7, 7).getLetter());
        assertEquals('O', game.getBoard().getTile(7, 11).getLetter());
        assertEquals(8, game.getPlayers().get(0).getScore());
        assertEquals(1, game.getCurrentPlayerIndex());
        assertEquals(7, game.getPlayers().get(0).getRack().size());
    }

    @Test
    public void cannotReplayEntirelyExistingWord() throws IOException {
        GameModel game = createGame();
        assertTrue(game.playWord("8H", "HELLO"), game.getLastError());
        assertFalse(game.playWord("8H", "HELLO"));
        assertEquals(1, game.getCurrentPlayerIndex());
    }

    @Test
    public void rejectsUnownedLettersAndNonsense() throws IOException {
        GameModel game = createGame();
        assertFalse(game.playWord("8H", "ZZZZ"));
        assertFalse(game.playWord("8H", "HELLOW")); // not in test dictionary
        assertEquals(0, game.getCurrentPlayerIndex());
        assertEquals(0, game.getCurrentPlayer().getScore());
    }

    @Test
    public void passingRotatesWithoutScoring() throws IOException {
        GameModel game = createGame();
        game.passTurn();
        assertEquals(1, game.getCurrentPlayerIndex());
        game.passTurn();
        assertEquals(0, game.getCurrentPlayerIndex());
        assertEquals(0, game.getPlayers().get(0).getScore());
    }

    @Test
    public void exchangeUsesOwnedTilesAndEndsTurn() throws IOException {
        GameModel game = createGame();
        assertTrue(game.exchangeTiles("HE"), game.getLastError());
        assertEquals(1, game.getCurrentPlayerIndex());
        assertEquals(7, game.getPlayers().get(0).getRack().size());
        assertEquals(0, game.getPlayers().get(0).getScore());
    }

    @Test
    public void rejectsIllegalCoordinatesWithoutAdvancing() throws IOException {
        GameModel game = createGame();
        assertFalse(game.playWord("P8", "HELLO"));
        assertFalse(game.playWord("0H", "HELLO"));
        assertFalse(game.playWord("15N", "HELLO"));
        assertEquals(0, game.getCurrentPlayerIndex());
    }
}
