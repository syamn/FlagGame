package syam.FlagGame.Listeners;

import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import syam.FlagGame.Actions;
import syam.FlagGame.FlagGame;
import syam.FlagGame.Game.Game;
import syam.FlagGame.Game.GameManager;
import syam.FlagGame.Game.GameTeam;

public class FGPlayerListener implements Listener{
	public static final Logger log = FlagGame.log;
	private static final String logPrefix = FlagGame.logPrefix;
	private static final String msgPrefix = FlagGame.msgPrefix;

	private final FlagGame plugin;

	public FGPlayerListener(final FlagGame plugin){
		this.plugin = plugin;
	}

	/* 登録するイベントはここから下に */

	// プレイヤーがブロックをクリックした
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerInteract(final PlayerInteractEvent event){
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();

		if(block != null){
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK && GameManager.isManager(player)){
				if (player.getItemInHand().getTypeId() == plugin.getConfigs().toolID && player.hasPermission("flag.admin")){
					/* 管理モードで特定のアイテムを持ったままブロックを右クリックした */
					Game game = GameManager.getSelectedGame(player);
					if (game == null){
						Actions.message(null, player, "&c先に編集するゲームを選択してください！");
						return;
					}
					Location loc = block.getLocation();

					// 既にフラッグブロックになっているか判定
					if (game.getFlag(loc) == null){
						// 選択
						GameManager.setSelectedBlock(player, block.getLocation());
						Actions.message(null, player, "&aブロックを選択しました！");
					}else{
						// 削除
						game.removeFlag(loc);
						Actions.message(null, player, "&aゲーム'"+game.getName()+"'のフラッグを削除しました！");
					}
				}
			}
		}
	}
}
