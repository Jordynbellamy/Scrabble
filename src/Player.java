import java.util.List;

public class Player {
    public Player(String name) {}
    public String getName() { return null; }
    public int getScore() { return 0; }
    public void addScore(int points) {}
    public List<Tile> getRack() { return null; }
    public boolean hasLetters(String letters) { return false; }
    public List<Tile> removeLetters(String letters) { return null; }
    public void refillRack(TileBag bag) {}
}
