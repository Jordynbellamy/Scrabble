import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TileTest {

    @Test
    public void testgetLetter(){
        Tile tile= new Tile('b', 3);
        assertEquals('B', tile.getLetter());
    }

    @Test
    public void testgetPoints(){
        Tile tile = new Tile('e', 1);
        assertEquals(1, tile.getPoints());
    }
    @Test
    public void testtoString(){
        Tile tile= new Tile('b', 3);
        assertEquals("B(3)", tile.toString());
    }
}