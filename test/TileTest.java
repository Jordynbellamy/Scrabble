import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TileTest {

    @Test
    public void testGetLetter(){
        Tile tile= new Tile('b', 3);
        assertEquals('B', tile.getLetter());
    }

    @Test
    public void testGetPoints(){
        Tile tile = new Tile('e', 1);
        assertEquals(1, tile.getPoints());
    }
    @Test
    public void testToString(){
        Tile tile= new Tile('b', 3);
        assertEquals("B(3)", tile.toString());
    }
}