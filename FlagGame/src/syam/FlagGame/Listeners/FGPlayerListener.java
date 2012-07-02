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

					Location loc = block.getLocation();
					if (game == null){
						Actions.message(null, player, "&cゲームが選択されてません！先に選択してください！");
						return;
					}

					// 既にフラッグブロックになっているか判定
					if (game.getDefFlagBlock(loc) == null){
						GameTeam team = GameManager.getSelectedTeam(player);
						// チーム選択済みチェック
						if (team == null){
							Actions.message(null, player, "&cチームが選択されてません！先に選択してください！");
							return;
						}

						// 登録
						game.setDefFlagBlock(loc, team);
						Actions.message(null, player, "&aゲーム'"+game.getName()+"'のフラッグ("+team.name()+")を作りました！");
					}else{
						// 削除
						String tmp = game.getDefFlagBlock(loc).name();
						game.removeFlag(loc);
						Actions.message(null, player, "&aゲーム'"+game.getName()+"'のフラッグ("+tmp+")を削除しました！");
					}
				}
			}
		}
	}
}
