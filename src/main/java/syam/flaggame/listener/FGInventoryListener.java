/**
 * FlagGame - Package: syam.flaggame.listener
 * Created: 2012/10/05 9:07:10
 */
package syam.flaggame.listener;

import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import syam.flaggame.FlagGame;
import syam.flaggame.enums.GameTeam;
import syam.flaggame.game.Game;
import syam.flaggame.manager.GameManager;

/**
 * FGInventoryListener (FGInventoryListener.java)
 * @author syam(syamn)
 */
public class FGInventoryListener implements Listener{
	public static final Logger log = FlagGame.log;
	private static final String logPrefix = FlagGame.logPrefix;
	private static final String msgPrefix = FlagGame.msgPrefix;

	private final FlagGame plugin;

	public FGInventoryListener(final FlagGame plugin){
		this.plugin = plugin;
	}

	/* 登録するイベントはここから下に */
	// プレイヤーがインベントリをクリックした
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onInventoryClick(final InventoryClickEvent event){
		// getSlot() == 39: 装備(頭)インベントリ
		if (event.getSlot() != 39){
			return;
		}

		// プレイヤーインスタンスを持たなければ返す
		if (!(event.getWhoClicked() instanceof Player)){
			return;
		}
		Player player = (Player) event.getWhoClicked();

		for (Game game : GameManager.getGames().values()){
			GameTeam team = game.getPlayerTeam(player);
			if (team != null){
				// ゲーム参加中のプレイヤーはイベントキャンセル
				event.setCurrentItem(new ItemStack(team.getBlockID(), 1, (short)0, team.getBlockData()));

				event.setCancelled(true);
				event.setResult(Result.DENY);
			}
		}
	}
}
