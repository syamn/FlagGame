/**
 * FlagGame - Package: syam.flaggame.exception Created: 2012/10/07 8:21:30
 */
package syam.flaggame.exception;

/**
 * CommandException (CommandException.java)
 * 
 * @author syam(syamn)
 */
public class CommandException extends Exception {
    private static final long serialVersionUID = 2061557577557212591L;

    public CommandException(String message) {
        super(message);
    }

    public CommandException(Throwable cause) {
        super(cause);
    }

    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
