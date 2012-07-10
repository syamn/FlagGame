package syam.FlagGame.Listeners;

import java.util.Collection;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.SignChangeEvent;

import syam.FlagGame.FlagGame;
import syam.FlagGame.Enum.GameTeam;
import syam.FlagGame.Game.Flag;
import syam.FlagGame.Game.Game;
import syam.FlagGame.Util.Actions;

public class FGBlockListener implements Listener{
	public static final Logger log = FlagGame.log;
	private static final String logPrefix = FlagGame.logPrefix;
	private static final String msgPrefix = FlagGame.msgPrefix;

	private final FlagGame plugin;

	public FGBlockListener(final FlagGame plugin){
		this.plugin = plugin;
	}

	/* 登録するイベントはここから下に */

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockBreak(final BlockBreakEvent event){
		Block block = event.getBlock();
		// ゲーム用ワールドでなければ返す
		if (block.getWorld() != Bukkit.getWorld(plugin.getConfigs().gameWorld)){
			return;
		}

		Location loc = block.getLocation();
		Player player = event.getPlayer();

		// フラッグブロックかチェックする
		for (Game game : plugin.games.values()){
			// 開始状態チェック
			if (!game.isStarting())
				continue;

			// フラッグチェック
			Flag flag = game.getFlag(loc);
			if (flag == null)
				continue;

			// プレイヤーと壊されたブロックのチーム取得
			GameTeam pTeam = game.getPlayerTeam(player);
			GameTeam bTeam = null;
			int id = block.getTypeId();
			byte data = block.getData();
			for (GameTeam gt : GameTeam.values()){
				if (gt.getBlockID() == id && gt.getBlockData() == data){
					bTeam = gt;
				}
			}

			// フラッグブロックの位置だが、影響のないブロックは何もしない
			if (bTeam == null)
				continue;

			// 自分のチームのフラッグは破壊させない
			if (bTeam == pTeam){
				event.setCancelled(true);
				Actions.message(null, player, "&cこれは自分のチームのフラッグです！");
				continue;
			}

			// フラッグ破壊 各チームへメッセージ表示
			if (bTeam == GameTeam.BLUE){
				// 青チームのブロックが破壊された
				game.message(GameTeam.RED, msgPrefix+ "&f'&6" + player.getName() +"&f'&aが相手の"+flag.getTypeName()+"フラッグを破壊しました！");
				game.message(GameTeam.BLUE, msgPrefix+ "&f'&6" + player.getName() +"&f'&cに"+flag.getTypeName()+"フラッグを破壊されました！");
			}else if (bTeam == GameTeam.RED){
				// 赤チームのブロックが破壊された
				game.message(GameTeam.BLUE, msgPrefix+ "&f'&6" + player.getName() +"&f'&aが相手の"+flag.getTypeName()+"フラッグを破壊しました！");
				game.message(GameTeam.RED, msgPrefix+ "&f'&6" + player.getName() +"&f'&cに"+flag.getTypeName()+"フラッグを破壊されました！");
			}
			game.log(" Player "+player.getName()+" Break "+flag.getFlagType().name()+" Flag!");
			return;
		}

		// 権限チェック
		if (player.hasPermission("flag.ignoreWorldProtect")){
			return;
		}

		// ワールド保護チェック
		if (plugin.getConfigs().isProtected){
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockPlace(final BlockPlaceEvent event){
		Block block = event.getBlock();
		// ゲーム用ワールドでなければ返す
		if (block.getWorld() != Bukkit.getWorld(plugin.getConfigs().gameWorld)){
			return;
		}

		Location loc = block.getLocation();
		Player player = event.getPlayer();

		// フラッグブロックかチェックする
		Collection<Game> col = plugin.games.values();
		for (Game game : col){
			// 開始状態チェック
			if (!game.isStarting())
				continue;

			// フラッグチェック
			Flag flag = game.getFlag(loc);
			if (flag == null)
				continue;

			// プレイヤーと設置したブロックのチーム取得
			GameTeam pTeam = game.getPlayerTeam(player);
			GameTeam bTeam = null;
			int id = block.getTypeId();
			byte data = block.getData();
			for (GameTeam gt : GameTeam.values()){
				if (gt.getBlockID() == id && gt.getBlockData() == data){
					bTeam = gt;
				}
			}

			// フラッグブロックの位置だが、影響のないブロックは何もしない
			if (bTeam == null)
				continue;

			// 相手のチームのフラッグは設置させない
			if (bTeam != pTeam){
				event.setCancelled(true);
				Actions.message(null, player, "&c相手チームのフラッグは設置できません！");
				continue;
			}

			// フラッグ設置 各チームへメッセージ表示
			if (bTeam == GameTeam.BLUE){
				// 青チームのブロックが設置された
				game.message(GameTeam.BLUE, msgPrefix+ "&f'&6" + player.getName() +"&f'&aが"+flag.getTypeName()+"フラッグを獲得しました！");
				game.message(GameTeam.RED, msgPrefix+ "&f'&6" + player.getName() +"&f'&cに"+flag.getTypeName()+"フラッグを獲得されました！");
			}else if (bTeam == GameTeam.RED){
				// 赤チームのブロックが設置された
				game.message(GameTeam.RED, msgPrefix+ "&f'&6" + player.getName() +"&f'&aが"+flag.getTypeName()+"フラッグを獲得しました！");
				game.message(GameTeam.BLUE, msgPrefix+ "&f'&6" + player.getName() +"&f'&cに"+flag.getTypeName()+"フラッグを獲得されました！");
			}
			game.log(" Player "+player.getName()+" Get "+flag.getFlagType().name()+" Flag!");
			return;
		}

		// 権限チェック
		if (player.hasPermission("flag.ignoreWorldProtect")){
			return;
		}

		// ワールド保護チェック
		if (plugin.getConfigs().isProtected){
			event.setCancelled(true);
		}
	}

	// 看板設置
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onSignChange(final SignChangeEvent event){
		Player player = event.getPlayer();
		Block block = event.getBlock();
		BlockState state = event.getBlock().getState();

		if (state instanceof Sign){
			Sign sign = (Sign)state;

			/* 特殊看板設置 */
			if(event.getLine(0).trim().toLowerCase().endsWith("[flaggame]")){
				// 権限チェック
				if (!player.hasPermission("flag.admin.sign")){
					event.setLine(0, "Denied!");
					Actions.message(null, player, "&cYou don't have permission to do this!");
					return;
				}
				event.setLine(0, "&a[FlagGame]");
			}
		}
	}

	/* 以下ワールド保護 */

	// 葉の消滅を抑制
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onLeavesDecay(final LeavesDecayEvent event){
		// ゲーム用ワールドでなければ返す
		if (event.getBlock().getWorld() != Bukkit.getWorld(plugin.getConfigs().gameWorld))
			return;

		// ワールド保護チェック
		if (plugin.getConfigs().isProtected){
			event.setCancelled(true);
		}
	}

	// 氷 → 水 抑制
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockFade(final BlockFadeEvent event){
		// ゲーム用ワールドでなければ返す
		if (event.getBlock().getWorld() != Bukkit.getWorld(plugin.getConfigs().gameWorld))
			return;

		// ワールド保護チェック
		if (plugin.getConfigs().isProtected){
			event.setCancelled(true);
		}
	}

	// 水 → 氷 抑制
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockForm(final BlockFormEvent event){
		// ゲーム用ワールドでなければ返す
		if (event.getBlock().getWorld() != Bukkit.getWorld(plugin.getConfigs().gameWorld))
			return;

		// ワールド保護チェック
		if (plugin.getConfigs().isProtected){
			event.setCancelled(true);
		}
	}
}
