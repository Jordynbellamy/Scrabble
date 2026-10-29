import java.util.List;

public class Board {
    public static final int SIZE = 15;
    public Board() {}
    public Tile getTile(int row, int col) { return null; }
    public boolean isEmpty() { return true; }
    public boolean isValidPlacement(String word, int row, int col, boolean across) { return false; }
    public void placeWord(List<Tile> tiles, int row, int col, boolean across) {}
    @Override
    public String toString() { return ""; }
}
