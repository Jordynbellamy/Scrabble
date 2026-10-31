import java.util.ArrayList;
import java.util.List;

/** Represents a Scrabble player with a name, score and rack of tiles
 *
 */
public class Player {
    private final String name;
    private int score;
    public static final int RACK_SIZE = 7;
    private ArrayList<Tile> rack;

    /** Constructor initializing the player class
     *
     * @param name the player's name
     */
    public Player(String name){
        this.name = name;
        score = 0;
        rack = new ArrayList<>();
    }

    /**Get the players name
     *
     * @return the name of the player
     */
    public String getName(){
        return name;
    }

    /**Get the players score
     *
     * @return the players score
     */
    public int getScore(){
        return score;
    }

    /**Get the list of tiles the player has in their rack
     *
     * @return the rack list
     */
    public List<Tile> getRack(){
        return new ArrayList<>(rack);
    }

    /** Refill the rack when it doesn't have up to 7 tiles
     *
     * @param bag the tilebag to refill the players tiles
     */
    public void refillRack(TileBag bag){
        int neededTiles = RACK_SIZE - rack.size();
        rack.addAll(bag.draw(neededTiles));
    }

    /**
     *Checks whether this player's rack contains all the given letters.
     * Each tile can only be used once
     * @param letters the letters to check for
     * @return true if the rack has every letter, false otherwise
     */
    public boolean hasLetters(String letters){
        letters = letters.toUpperCase();
        List<Tile> copy = new ArrayList<>(rack);
        for(int i=0; i < letters.length(); i++){
            char c = letters.charAt(i);
            boolean found = false;

            for(int n =0; n < copy.size(); n++) {
                if (c == copy.get(n).getLetter()){
                    copy.remove(n);
                    found = true;
                    break;
                }
            }
            if (!found){
                return false;
            }
        }
        return true;
    }

    /**
     * Add a single tile to the player's rack
     * @param tile the tile to add
     */
    public void addTile(Tile tile){
        rack.add(tile);
    }

    /**Remove the found letters from the players tiles and return the removed list, return an empty list if nothing is found
     * @param letters the letters to find
     * @return the removed list
     */
    public List<Tile> removeLetters(String letters){
        letters = letters.toUpperCase();
        List<Tile> removedList = new ArrayList<>();

        if(!hasLetters(letters)){
            return removedList;
        }

        for(int i =0; i < letters.length(); i++) {
            char c = letters.charAt(i);
            for (int n = 0; n < rack.size(); n++) {
                if (c == rack.get(n).getLetter()) {
                    Tile removed = rack.remove(n);
                    removedList.add(removed);
                    break;
                }
            }

        }
        return removedList;
    }

    /**
     * Exchanges the given tiles for new ones from the bag, ending the player's turn.
     * New tiles are drawn before the old ones are returned.
     * @param letters the letters to exchange
     * @param bag the tilebag
     * @return true if the exchange was completed, false otherwise
     */
    public boolean exchange(String letters, TileBag bag){
        if(!hasLetters(letters)){
            return false;
        }
        if(bag.size() < RACK_SIZE){
            return false;
        }
        List<Tile> oldTiles = removeLetters(letters);
        refillRack(bag);
        bag.returnTiles(oldTiles);

        return true;
    }

    /**Add to the players score after each word is played
     *
     * @param points the points to add
     */
    public void addScore(int points){
        score += points;
    }

    /**Create a string containing the players information
     *
     * @return the players information
     */
    @Override
    public String toString() {
        return name + " (" + score +"): " + rack  ;
    }
}
