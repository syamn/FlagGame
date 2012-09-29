/**
 * FlagGame - Package: syam.flaggame.command
 * Created: 2012/09/30 1:23:16
 */
package syam.flaggame.command.queue;

import java.util.List;

/**
 * Queueable (Queueable.java)
 * @author syam(syamn)
 */
public interface Queueable {
	void executeQueue(List<String> args);
}
