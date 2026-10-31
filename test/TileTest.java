import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to check each tile is initialized correctly
 */
public class TileTest {

    /**
     * Test that a tile has the correct letter
     */
    @Test
    public void testGetLetter(){
        Tile tile= new Tile('b', 3);
        assertEquals('B', tile.getLetter());
    }

    /**
     * Test that a tile has the correct points
     */
    @Test
    public void testGetPoints(){
        Tile tile = new Tile('e', 1);
        assertEquals(1, tile.getPoints());
    }

    /**
     * Test that the toString is correct
     */
    @Test
    public void testToString(){
        Tile tile= new Tile('b', 3);
        assertEquals("B(3)", tile.toString());
    }
}