import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Tests for the Player class.
 */

public class PlayerTest {

    private Player player1;
    private Player player2;
    private TileBag bag;

    /**
     * Create a bag and two new players before each test
     */

    @BeforeEach
    public void SetUp(){
        bag = new TileBag();
        player1 = new Player("Jack");
        player2 = new Player("Mary");
    }
    /**
     * Gives a player a rack with the given letters, points are auto set to 1
     *
     * @param player  the player to give tiles to
     * @param letters the letters to put on the rack
     */
    private void giveTestRack(Player player, String letters){
        for (char c: letters.toCharArray()){
            player.addTile(new Tile(c,1));
        }
    }

    /**
     * Tests that each players name is set correctly
     */
    @Test
    public void testGetName(){
        assertEquals("Jack", player1.getName());
        assertEquals("Mary", player2.getName());
    }
    /**
     * Tests that each player starts with a score of 0
     */
    @Test
    public void testGetScore(){
        assertEquals(0, player1.getScore());
        assertEquals(0,player2.getScore());
    }
    /**
     * Tests that hasLetters handles repeated letters (two E's needed for EEL).
     */
    @Test
    public void testHasLetters(){
        String letters = "EEL";
        giveTestRack(player1, "ELECYBZ");
        giveTestRack(player2, "ELPCYBZ");

        assertTrue(player1.hasLetters(letters));
        assertFalse(player2.hasLetters(letters));
    }
    /**
     * Tests that removeLetters takes the given letters off the rack.
     */

    @Test
    public void testRemoveLetters(){
        String letters = "EEL";
        giveTestRack(player1, "ELECYBZ");
        giveTestRack(player2, "ELPCYBZ");

        // player1 has every letter, so E, E and L should be gone
        player1.removeLetters(letters);
        assertFalse(player1.hasLetters("E"));
        assertFalse(player1.hasLetters("L"));

        player2.removeLetters(letters);
        assertTrue(player2.hasLetters("E"));
        assertTrue(player2.hasLetters("L"));
    }
    /**
     * Tests that Exchange takes the given letters off the rack, making sure it satisfies all conditions before exchanging
     */
    @Test
    public void testExchange1(){
        String letters = "EEL";
        giveTestRack(player1, "ELECYBZ");
        assertTrue(player1.exchange(letters,bag));
        assertEquals(7,player1.getRack().size());

        // player2 only has one E, so the exchange should fail
        giveTestRack(player2, "ELPCYBZ");
        assertFalse(player2.exchange(letters, bag));

        // empty the bag so there aren't enough tiles to exchange
        bag.draw(98);
        assertFalse(player1.exchange("CY",bag));
    }

    /**
     * Tests that RefillRack refills an empty or partly filled rack
     */
    @Test
    public void testRefillRack(){
        player1.refillRack(bag);
        assertEquals(7, player1.getRack().size());

        // remove 4 tiles, then refill should bring it back to 7
        giveTestRack(player2, "ELPCYBE");
        player2.removeLetters("PEEL");
        player2.refillRack(bag);
        assertEquals(7,player2.getRack().size());
    }
    /**
     * Tests that addScore adds to the existing score
     */
    @Test
    public void testScore(){
        player1.addScore(5);
        player1.addScore(10);
        assertEquals(15, player1.getScore());
    }

    /**
     * Tests that toString shows the player's name, score and rack.
     */
    @Test
    public void testToString(){
        giveTestRack(player1,"ELECYBZ");
        player1.addScore(10);
        assertEquals("Jack (10): [E(1), L(1), E(1), C(1), Y(1), B(1), Z(1)]", player1.toString());
    }

}