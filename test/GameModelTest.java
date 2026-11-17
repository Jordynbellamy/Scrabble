import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Milestone 1 and Milestone 2 GameModel integration tests.
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
                new WordList(dictionary), new Scorer());
    }

    @Test
    public void supportsTwoToFourPlayers() throws IOException {
        GameModel game = createGame();
        assertEquals(2, game.getPlayers().size());
        assertEquals(7, game.getCurrentPlayer().getRack().size());
        assertThrows(IllegalArgumentException.class,
                () -> new GameModel(1, game.getBoard(), new TileBag(),
                        new WordList(tempDir.resolve("words.txt")), new Scorer()));
        assertThrows(IllegalArgumentException.class,
                () -> new GameModel(5, game.getBoard(), new TileBag(),
                        new WordList(tempDir.resolve("words.txt")), new Scorer()));
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

    private static class RecordingView implements ScrabbleView{
        List<ScrabbleEvent> events = new ArrayList<>();

        @Override
        public void handleScrabbleUpdate(ScrabbleEvent e) {
            events.add(e);
        }
    }
    @Test
    public void validMoveNotifiesViews() throws IOException{
        GameModel game = createGame();
        RecordingView view = new RecordingView();
        game.addScrabbleView(view);
        assertTrue(game.playWord("8H", "HELLO"), game.getLastError());
        assertEquals(1,view.events.size());
        assertSame(game,view.events.get(0).getSource());
        assertEquals("", view.events.get(0).getMessage());
    }
    @Test
    public void rejectedMoveNotifiesViews() throws IOException{
        GameModel game = createGame();
        RecordingView view = new RecordingView();
        game.addScrabbleView(view);
        assertFalse(game.playWord("9H", "HELLO"));
        assertEquals(1, view.events.size());
        assertFalse(view.events.get(0).getMessage().isEmpty());
        assertEquals(game.getLastError(), view.events.get(0).getMessage());
    }

    @Test
    public void passNotifiesViews() throws IOException{
        GameModel game = createGame();
        RecordingView view = new RecordingView();
        game.addScrabbleView(view);
        game.passTurn();
        assertEquals(1,view.events.size());
    }
    @Test
    public void exchangeNotifiesViews() throws  IOException{
        GameModel game = createGame();
        RecordingView view = new RecordingView();
        game.addScrabbleView(view);
        assertTrue(game.exchangeTiles("HE"),game.getLastError());
        assertEquals(1,view.events.size());
    }

    @Test
    public void removedViewsIsNotNotified() throws IOException{
        GameModel game = createGame();
        RecordingView view = new RecordingView();
        game.addScrabbleView(view);
        game.removeScrabbleView(view);
        game.passTurn();
        assertTrue(view.events.isEmpty());
    }

    /**
     * Creates a two player game with a chosen dictionary and tile bag.
     * Player 1 gets the first 7 letters of the bag and Player 2 gets the next 7
     *
     * @param words   the words in the dictionary
     * @param letters the tiles in the bag, in the order they are drawn
     * @return the new game
     * @throws IOException if the dictionary file can't be written
     */
    private GameModel createGame(List<String> words, String letters) throws IOException {
        Path dictionary = tempDir.resolve("custom-words.txt");
        Files.write(dictionary, words);
        return new GameModel(2, new Board(15, 15), new ScriptedTileBag(letters),
                new WordList(dictionary), new Scorer());
    }

    /**
     * Tests that a valid word adds its score and refills the player's rack from the bag
     */
    @Test
    public void validWordAddsScoreAndRefillsRack() throws IOException {
        GameModel game = createGame();
        Player first = game.getCurrentPlayer();

        assertTrue(game.playWord("8H", "HELLO"), game.getLastError());
        assertEquals(8, first.getScore()); //H4 + E1 + L1 + L1 + O1
        assertEquals(7, first.getRack().size());
        assertEquals('H', game.getBoard().getTile(7, 7).getLetter());
        assertEquals('O', game.getBoard().getTile(7, 11).getLetter());
    }

    /**
     * Tests that a word can go through a tile already on the board, and only the new tiles
     * come from the player's rack
     */
    @Test
    public void wordCanReuseTileOnBoard() throws IOException {
        GameModel game = createGame(Arrays.asList("WORD", "WATCH"), "WORDXXX" + "ATCHXXX" + "QQQQQQQQQQ");

        assertTrue(game.playWord("8H", "WORD"), game.getLastError());
        assertTrue(game.playWord("H8", "WATCH"), game.getLastError());

        assertEquals(8, game.getPlayers().get(0).getScore());  //W4 + O1 + R1 + D2
        assertEquals(13, game.getPlayers().get(1).getScore()); //W4 + A1 + T1 + C3 + H4
        assertEquals('H', game.getBoard().getTile(11, 7).getLetter());
    }

    /**
     * Tests that a move is rejected if it makes a cross-word that is not in the dictionary,
     * and that nothing changes
     */
    @Test
    public void invalidCrossWordIsRejected() throws IOException {
        GameModel game = createGame(Arrays.asList("CAT", "AT"), "CATXXXX" + "ATXXXXX" + "QQQQQQQQQQ");
        assertTrue(game.playWord("8H", "CAT"), game.getLastError());

        //AT under CA makes CA going down, which is not a word
        assertFalse(game.playWord("9H", "AT"));
        assertEquals("Not in the dictionary: CA", game.getLastError());
        assertNull(game.getBoard().getTile(8, 7));
        assertEquals(0, game.getCurrentPlayer().getScore());
        assertEquals(1, game.getCurrentPlayerIndex());
    }

    /**
     * Tests that a word that does not touch any tile on the board is rejected
     */
    @Test
    public void disconnectedWordIsRejected() throws IOException {
        GameModel game = createGame(Arrays.asList("CAT", "AT"), "CATXXXX" + "ATXXXXX" + "QQQQQQQQQQ");
        assertTrue(game.playWord("8H", "CAT"), game.getLastError());

        assertFalse(game.playWord("1A", "AT"));
        assertEquals("Your word must connect to tiles already on the board.", game.getLastError());
        assertNull(game.getBoard().getTile(0, 0));
    }

    /**
     * Tests that a word can't replace a different letter already on the board
     */
    @Test
    public void wordCannotOverwriteTile() throws IOException {
        GameModel game = createGame(Arrays.asList("CAT", "AT"), "CATXXXX" + "ATXXXXX" + "QQQQQQQQQQ");
        assertTrue(game.playWord("8H", "CAT"), game.getLastError());

        //AT starting on the C would put an A where the C is
        assertFalse(game.playWord("8H", "AT"));
        assertEquals("The move conflicts with an existing tile.", game.getLastError());
        assertEquals('C', game.getBoard().getTile(7, 7).getLetter());
    }

    /**
     * Tests that a word needing letters the player doesn't have is rejected
     */
    @Test
    public void wordNeedsLettersFromRack() throws IOException {
        GameModel game = createGame();

        //Player 1 has HELLOWR, so there is no C, A or T
        assertFalse(game.playWord("8H", "CAT"));
        assertEquals("Your rack does not contain the required letters.", game.getLastError());
        assertEquals(0, game.getCurrentPlayerIndex());
    }

    /**
     * Tests that using all seven tiles in one word adds the 50 point bonus
     */
    @Test
    public void allSevenTilesGetsBonus() throws IOException {
        GameModel game = createGame(Arrays.asList("PLAYERS"), "PLAYERS" + "XXXXXXX" + "QQQQQQQQQQ");

        assertTrue(game.playWord("8H", "PLAYERS"), game.getLastError());
        assertEquals(62, game.getPlayers().get(0).getScore());
    }

    /**
     * Tests that tiles can't be exchanged when fewer than seven are left in the bag
     */
    @Test
    public void exchangeNeedsSevenTilesInBag() throws IOException {
        //After both racks are dealt, only 3 tiles are left
        GameModel game = createGame(Arrays.asList("CAT"), "CATXXXX" + "ATXXXXX" + "QQQ");

        assertFalse(game.exchangeTiles("CA"));
        assertEquals("At least seven tiles must remain in the bag to exchange.", game.getLastError());
        assertEquals(0, game.getCurrentPlayerIndex());
    }
}