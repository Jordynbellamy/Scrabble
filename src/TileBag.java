import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**Represents the bag containing the tiles used to play
 *
 */
public class TileBag {

    private ArrayList<Tile> tiles;

    /**
     * Creates a full shuffled bag using the official Scrabble distribution
     *
     */
    public TileBag() {
        tiles = new ArrayList<>();

        addTiles('A', 9, 1);
        addTiles('B', 2, 3);
        addTiles('C', 2, 3);
        addTiles('D', 4, 2);
        addTiles('E', 12, 1);
        addTiles('F', 2, 4);
        addTiles('G', 3, 2);
        addTiles('H', 2, 4);
        addTiles('I', 9, 1);
        addTiles('J', 1, 8);
        addTiles('K', 1, 5);
        addTiles('L', 4, 1);
        addTiles('M', 2, 3);
        addTiles('N', 6, 1);
        addTiles('O', 8, 1);
        addTiles('P', 2, 3);
        addTiles('Q', 1, 10);
        addTiles('R', 6, 1);
        addTiles('S', 4, 1);
        addTiles('T', 6, 1);
        addTiles('U', 4, 1);
        addTiles('V', 2, 4);
        addTiles('W', 2, 4);
        addTiles('X', 1, 8);
        addTiles('Y', 2, 4);
        addTiles('Z', 1, 10);
        Collections.shuffle(tiles);
    }

    /**
     * Add the tiles to the bag based on the char, how many times they appear and the points
     *
     * @param letter the letter being added
     * @param numApp how many of the letter is there
     * @param points how many points does the letter hsve
     */
    private void addTiles(char letter, int numApp, int points) {
        for (int i = 0; i < numApp; i++) {
            tiles.add(new Tile(letter, points));
        }
    }
/**
 * Draws up to n tiles at random from the shuffled bag of tiles and returns it as a list
 * @param n the number of tiles to draw
 * @return the drawn tiles in a list
 */
    public List<Tile> draw(int n) {
        List<Tile> drawn = new ArrayList<>();

        for (int i = 0; i < n && !tiles.isEmpty(); i++) {
            Tile drawnTile = tiles.remove(tiles.size() - 1);
            drawn.add(drawnTile);
        }
        return drawn;
    }

    /**Returns a drawn tile from a player back into the tilebag
     *
     * @param returnedTiles a list of the tiles to be returned
     */
    public void returnTiles(List<Tile> returnedTiles ){
        tiles.addAll(returnedTiles);
	Collections.shuffle(tiles);
    }

    /**the size of the tile bag
     *
     * @return the number of tiles in the bag
     */
    public int size(){
        return tiles.size();
    }

    /** Returns whether the tilesbag is empty or not
     *
     * @return A boolean of if the bag is empty or not
     */
    public boolean isEmpty(){
        return tiles.isEmpty();
    }
}

