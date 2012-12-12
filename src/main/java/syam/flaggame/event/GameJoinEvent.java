/**
 * FlagGame - Package: syam.flaggame.event Created: 2012/10/13 21:37:38
 */
package syam.flaggame.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import syam.flaggame.enums.GameTeam;

/**
 * GameJoinEvent (GameJoinEvent.java)
 * 
 * @author syam(syamn)
 */
public class GameJoinEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled = false;

    private Player player;
    private double entryFee;

    private GameTeam team = null;

    public GameJoinEvent(Player player, double entryFee) {
        this.player = player;
        this.entryFee = entryFee;
    }

    public Player getPlayer() {
        return this.player;
    }

    public double getEntryFee() {
        return this.entryFee;
    }

    public void setEntryFee(double cost) {
        this.entryFee = cost;
    }

    public void setGameTeam(GameTeam team) {
        this.team = team;
    }

    public GameTeam getGameTeam() {
        return this.team;
    }

    public boolean isCancelled() {
        return this.isCancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }

    /* ******************** */
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
