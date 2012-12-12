package syam.flaggame.listener;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.SignChangeEvent;

import syam.flaggame.FlagGame;
import syam.flaggame.enums.GameTeam;
import syam.flaggame.game.Flag;
import syam.flaggame.game.Game;
import syam.flaggame.game.Stage;
import syam.flaggame.manager.StageManager;
import syam.flaggame.permission.Perms;
import syam.flaggame.player.PlayerManager;
import syam.flaggame.util.Actions;
import syam.flaggame.util.Cuboid;

public class FGBlockListener implements Listener{
	public static final Logger log = FlagGame.log;
	private static final String logPrefix = FlagGame.logPrefix;
	private static final String msgPrefix = FlagGame.msgPrefix;

	private final FlagGame plugin;

	public FGBlockListener(final FlagGame plugin){
		this.plugin = plugin;
	}

	/* 登録するイベントはここから下に */
	/**
	 * ブロックを破壊した
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockBreak(final BlockBreakEvent event){
		if (onBlockChange(event, event.getPlayer())){
			event.setCancelled(true);
		}
	}

	/**
	 * ブロックを設置した
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockPlace(final BlockPlaceEvent event){
		if (onBlockChange(event, event.getPlayer())){
			event.setCancelled(true);
		}
	}

	// フラッグが壊されたか取得した
	public boolean onBlockChange(final BlockEvent event, final Player player){
		final Block block = event.getBlock();
		// ゲーム用ワールドでなければ返す
		if (block.getWorld() != Bukkit.getWorld(plugin.getConfigs().getGameWorld())){
			return false;
		}

		boolean placeEvent = false, breakEvent = false, cancel = false;
		if (event instanceof BlockPlaceEvent){ placeEvent = true; }
		else if (event instanceof BlockBreakEvent){ breakEvent = true; }
		else{
			return false;
		}

		final Location loc = block.getLocation();

		// フラッグブロックかチェックする
		for (Stage stage : StageManager.getStages().values()){
			Flag flag = null;

			if (stage.isFlag(loc)){
				flag = stage.getFlag(loc);
			}else{
				if (stage.hasStage()){
					Cuboid stageArea = stage.getStage();
					if (stageArea.isIn(loc)){
						if (stage.isStageProtected()){
							cancel = true;
						}
						return cancel; // ステージエリアの被りが無い前提で返す
					}
				}
				continue; // フラッグでもなく、エリア内でも無ければ次のゲームステージを走査する
			}

			// 開始されていないステージ
			if (!stage.isUsing() || stage.getGame() == null){
				cancel = stage.isStageProtected(); // フラッグ保護、ステージ保護設定に依存
				continue;
			}

			Game game = stage.getGame();

			/* ゲーム中のステージでフラッグを設置/破壊した */

			// プレイヤーと設置/破壊したブロックのチーム取得
			GameTeam pTeam = game.getPlayerTeam(player);
			if (pTeam == null){
				Actions.message(player, "&cあなたはこのゲームに参加していません！");
				cancel = true;
				return cancel;
			}
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

			// メッセージ -> カウント -> エフェクト
			if (placeEvent){ // 設置
				if (bTeam != pTeam){
					cancel = true; // 相手のチームのフラッグは設置させない
					Actions.message(player, "&c相手チームのフラッグは設置できません！");
					continue;
				}

				game.message(pTeam, msgPrefix+ "&f'&6" + player.getName() +"&f'&aが"+flag.getTypeName()+"フラッグを獲得しました！");
				game.message(pTeam.getAgainstTeam(), msgPrefix+ "&f'&6" + player.getName() +"&f'&cに"+flag.getTypeName()+"フラッグを獲得されました！");
				game.log(" Player "+player.getName()+" Get "+flag.getFlagType().name()+" Flag: "+Actions.getBlockLocationString(block.getLocation()));

				PlayerManager.getProfile(player.getName()).addPlace();
				stage.getProfile().addPlace();

				if (plugin.getConfigs().getUseFlagEffects()){
					loc.getWorld().playEffect(loc, Effect.ENDER_SIGNAL, 0, 10);
					loc.getWorld().playEffect(loc, Effect.SMOKE, 4, 2);
				}
			}
			else if (breakEvent){ // 破壊
				if (bTeam == pTeam){
					cancel = true; // 自分のチームのフラッグは破壊させない
					Actions.message(player, "&cこれは自分のチームのフラッグです！");
					continue;
				}
				game.message(pTeam, msgPrefix+ "&f'&6" + player.getName() +"&f'&aが相手の"+flag.getTypeName()+"フラッグを破壊しました！");
				game.message(pTeam.getAgainstTeam(), msgPrefix+ "&f'&6" + player.getName() +"&f'&cに"+flag.getTypeName()+"フラッグを破壊されました！");
				game.log(" Player "+player.getName()+" Break "+flag.getFlagType().name()+" Flag: "+Actions.getBlockLocationString(block.getLocation()));

				PlayerManager.getProfile(player.getName()).addBreak();
				stage.getProfile().addBreak();

				if (plugin.getConfigs().getUseFlagEffects()){
					loc.getWorld().createExplosion(loc, 0F, false);
					loc.getWorld().playEffect(loc, Effect.ENDER_SIGNAL, 0, 10);
				}
			}

			return cancel;
		}

		// 権限チェック
		if (Perms.IGNORE_PROTECT.has(player)){
			return cancel;
		}

		// ワールド保護チェック
		if (plugin.getConfigs().isProtected()){
			cancel = true;
		}

		return cancel;
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
			if(event.getLine(0).toLowerCase().indexOf("[flaggame]") != -1){
				// 権限チェック
				if (!Perms.SIGN.has(player)){
					event.setLine(0, "Denied!");
					Actions.message(player, "&cYou don't have permission to do this!");
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
		if (event.getBlock().getWorld() != Bukkit.getWorld(plugin.getConfigs().getGameWorld()))
			return;

		// ワールド保護チェック
		if (plugin.getConfigs().isProtected()){
			event.setCancelled(true);
		}
	}

	// 氷 → 水 抑制
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockFade(final BlockFadeEvent event){
		// ゲーム用ワールドでなければ返す
		if (event.getBlock().getWorld() != Bukkit.getWorld(plugin.getConfigs().getGameWorld()))
			return;

		// ワールド保護チェック
		if (plugin.getConfigs().isProtected()){
			event.setCancelled(true);
		}
	}

	// 水 → 氷 抑制
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockForm(final BlockFormEvent event){
		// ゲーム用ワールドでなければ返す
		if (event.getBlock().getWorld() != Bukkit.getWorld(plugin.getConfigs().getGameWorld()))
			return;

		// ワールド保護チェック
		if (plugin.getConfigs().isProtected()){
			event.setCancelled(true);
		}
	}
}
