package syam.FlagGame.Listeners;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;

import syam.FlagGame.Actions;
import syam.FlagGame.FlagGame;
import syam.FlagGame.Game.Flag;
import syam.FlagGame.Game.Game;

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
		Collection<Game> col = plugin.games.values();
		for (Game game : col){
			// 開始状態チェック
			if (!game.isStarting())
				continue;
			// フラッグチェック
			Flag flag = game.getFlag(loc);
			if (flag == null)
				continue;

			// フラッグ破壊
			Actions.broadcastMessage(msgPrefix+ "&f'&6" + player.getName() +"&f'&cが"+flag.getTypeName()+"フラッグを破壊しました！");
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

			// フラッグ破壊
			Actions.broadcastMessage(msgPrefix+ "&f'&6" + player.getName() +"&f'&cが"+flag.getTypeName()+"フラッグを獲得しました！");
			return;
		}

		// ワールド保護チェック
		if (plugin.getConfigs().isProtected){
			event.setCancelled(true);
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
