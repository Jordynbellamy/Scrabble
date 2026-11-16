import java.util.EventObject;

public class ScrabbleEvent extends EventObject {
    private final String message;

    public ScrabbleEvent(GameModel model, String message){
        super(model);
        this.message = message;
    }

    public String getMessage(){
        return message;
    }
}
