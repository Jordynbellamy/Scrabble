/**
 * Represents a single Scrabble letter tile with a letter and a point value.
 */
public class Tile {

    private final char letter;
    private final int points;
    /**
     * Creates a tile with the given letter and point value.
     *
     * @param letter the letter on the tile
     * @param points the point value of the tile
     */
    public Tile (char letter, int points){
        this.letter = Character.toUpperCase(letter);
        this.points = points;
    }
    /**
     * Returns the letter on this tile.
     *
     * @return the tile's uppercase letter
     */
    public char getLetter() {
        return letter;
    }
    /**
     * Returns the points on this tile.
     *
     * @return the tile's points
     */
    public int getPoints(){
        return points;
    }

    @Override
    public String toString(){
        return letter + "(" + points + ")";
    }

}
