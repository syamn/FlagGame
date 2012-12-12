/**
 * FlagGame - Package: syam.flaggame.exception Created: 2012/09/25 21:40:49
 */
package syam.flaggame.exception;

/**
 * GameStateException (GameStateException.java)
 * 
 * @author syam(syamn)
 */
public class GameStateException extends FlagGameException {
    private static final long serialVersionUID = -3385340476319991882L;

    public GameStateException(String message) {
        super(message);
    }

    public GameStateException(Throwable cause) {
        super(cause);
    }

    public GameStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
