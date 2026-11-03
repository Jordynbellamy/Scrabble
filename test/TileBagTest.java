import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the TileBag class.
 */
public class TileBagTest {

    private TileBag bag;

    @BeforeEach
    public void setUp(){
        bag = new TileBag();
    }

    /**
     * Test if a new bag has correct tiles
     */
    @Test
    public void testBagHasCorrectTiles(){
        List<Tile> allTiles = bag.draw(98);

        int count = 0;
        for(Tile tile: allTiles){
            if(tile.getLetter() =='Z'){
                count++ ;
            }
        }
        assertEquals(1, count);
    }

    /**
     * Test if a tile has the correct points
     */

    @Test
    public void hasCorrectPoints(){
        List<Tile> allTiles = bag.draw(98);

        int point = 0;
        for(Tile tile: allTiles){
            if(tile.getLetter()== 'G'){
                point = tile.getPoints();
            }
        }
        assertEquals(2, point);
    }

    /**
     * Test if a tile drawn from a play is added to a new tile list
     */
    @Test
    public void testDraw(){
        List <Tile> drawn1 = bag.draw(7);
        assertEquals(7, drawn1.size());

        List<Tile> drawn2 = bag.draw(0);
        assertTrue(drawn2.isEmpty());
    }
    @Test
    public void testReturnTiles(){
        List <Tile> takenTiles = bag.draw(7);
        int bagSize = bag.size();

        bag.returnTiles(takenTiles);
        assertEquals(bagSize+7, bag.size());
    }

    /**
     * Test if a new tile size is correct
     */
    @Test
    public void testSize(){
        assertEquals(98, bag.size());
    }

    /**
     * Test if isEmpty shows the correct bool.
     */
    @Test
    public void testIsEmpty(){
        assertFalse(bag.isEmpty());

        List<Tile> allTiles = bag.draw(98);
        assertTrue(bag.isEmpty());
    }

}
