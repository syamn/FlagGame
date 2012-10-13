/**
 * FlagGame - Package: syam.flaggame.event
 * Created: 2012/10/13 21:20:50
 */
package syam.flaggame.event;

import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import syam.flaggame.game.Stage;

/**
 * GameStartEvent (GameStartEvent.java)
 * @author syam(syamn)
 */
public class GameStartEvent extends Event implements Cancellable{
	private static final HandlerList handlers = new HandlerList();
	private boolean isCancelled = false;

	private Stage stage;
	private boolean random;
	private CommandSender sender;
	private Set<String> redTeam;
	private Set<String> blueTeam;

	/**
	 * コンストラクタ
	 * @param stage
	 * @param random
	 * @param sender
	 * @param redTeam
	 * @param blueTeam
	 */
	public GameStartEvent(Stage stage, boolean random, CommandSender sender, Set<String> redTeam, Set<String> blueTeam){
		this.stage = stage;
		this.random = random;
		this.sender = sender;

		this.redTeam = redTeam;
		this.blueTeam = blueTeam;
	}

	public Stage getStage(){
		return this.stage;
	}
	public boolean isRandom(){
		return this.random;
	}
	public CommandSender getSender(){
		return this.sender;
	}

	public Set<String> getRedTeam(){
		return this.redTeam;
	}
	public Set<String> getBlueTeam(){
		return this.blueTeam;
	}

	public boolean isCancelled(){
		return this.isCancelled;
	}
	public void setCancelled(boolean cancelled){
		this.isCancelled = cancelled;
	}

	/* ******************** */
	@Override
	public HandlerList getHandlers(){
		return handlers;
	}

	public static HandlerList getHandlerList(){
		return handlers;
	}
}
