package syam.FlagGame.Listeners;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.titankingdoms.nodinchan.deathnotifier.events.DeathEvent;

import syam.FlagGame.FlagGame;

public class DeathNotifierListener implements Listener {
	public static final Logger log = FlagGame.log;
	@SuppressWarnings("unused")
	private static final String logPrefix = FlagGame.logPrefix, msgPrefix = FlagGame.msgPrefix;

	private final FlagGame plugin;

	public DeathNotifierListener(final FlagGame plugin){
		this.plugin = plugin;
	}

	// DeathNotifierから受け取るプレイヤー死亡のイベント
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onDeath(final DeathEvent event){
		Player killed = event.getKilled();
		// ゲーム用ワールドでの死亡はメッセージを出さない
		if (killed.getWorld() == Bukkit.getWorld(plugin.getConfigs().gameWorld)){
			event.setDeathMessage(null);
		}
	}
}
