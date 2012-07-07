package syam.FlagGame.Listeners;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import syam.FlagGame.FlagGame;
import syam.FlagGame.Game.Game;
import syam.FlagGame.Game.GameManager;
import syam.FlagGame.Game.GameTeam;
import syam.FlagGame.Util.Actions;

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

					// ゲーム用ワールドでなければ返す
					if (loc.getWorld() != Bukkit.getWorld(plugin.getConfigs().gameWorld)){
						Actions.message(null, player, "&cここはゲーム用ワールドではありません！");
						return;
					}

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

	// プレイヤーがリスポーンした
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerRespawn(final PlayerRespawnEvent event){
		Player player = event.getPlayer();
		for (Game game : plugin.games.values()){
			// 開始されていないゲームはチェックしない
			if (!game.isStarting()) continue;

			// チームに所属していれば、チームのスポーン地点へ移動させる
			GameTeam team = game.getPlayerTeam(player);
			if (team != null){
				Location loc = game.getSpawnLocation(team);
				if (loc == null){
					// 所属チームのスポーン地点設定なし
					Actions.message(null, player, msgPrefix+ "&cあなたのチームのスポーン地点が設定されていません");
					log.warning(logPrefix+ "Player "+player.getName()+" died, But undefined spawn-location. Game: " + game.getName() + " Team: " +team.name());
					return;
				}else{
					// 設定あり
					event.setRespawnLocation(loc);
					Actions.message(null, player, msgPrefix+ "&6このゲームはあと "+game.getRemainTime()+"秒 残っています！");
				}
				return; // 複数ゲーム所属はあり得ないのでここで返す
			}
		}
	}

	// プレイヤーがログアウトした
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerQuit(final PlayerQuitEvent event){
		Player player = event.getPlayer();

		for (Game game : plugin.games.values()){
			if (!game.isStarting()) continue;

			GameTeam team = game.getPlayerTeam(player);

			// チームに所属していてこの設定が有効なら、アナウンスしてHPをゼロにする
			if (team != null && plugin.getConfigs().deathWhenLogout){
				player.setHealth(0);
				game.message(msgPrefix+ team.getColor()+team.getTeamName()+"チーム &6のプレイヤー'"+team.getColor()+player.getName()+"&6'がログアウトしたため死亡しました");
			}
		}
	}

	// プレイヤーがコマンドを使おうとした
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event){
		Player player = event.getPlayer();
		
	}

	// プレイヤーが死んだ
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerDeath(final PlayerDeathEvent event){
		Player deader = event.getEntity();

		// ゲームワールド以外は何もしない
		if (deader.getWorld() != Bukkit.getWorld(plugin.getConfigs().gameWorld)){
			return;
		}

		// 通常の死亡メッセージ非表示
		event.setDeathMessage(null);
		String deathMsg = "";

		EntityDamageEvent cause = event.getEntity().getLastDamageCause();
		Player killer = null;

		// 死亡理由不明は何もしない
		if (cause == null) return;
		// エンティティによって殺された
		if (cause instanceof EntityDamageByEntityEvent){
			// TODO: Killカウント実装？
			Entity killerEntity = ((EntityDamageByEntityEvent) cause).getDamager();
			// エンティティ→プレイヤーによって殺された
			if (killerEntity instanceof Player){
				killer = (Player) killerEntity;
			}
			// エンティティ→プレイヤーによって発射された物(矢など)によって殺された
			else if (killerEntity instanceof Projectile && ((Projectile) killerEntity).getShooter() instanceof Player){
				killer = (Player) ((Projectile) killerEntity).getShooter();
			}
		}

		// プレイヤーによって倒されてない場合は何もしない
		if (killer == null) return;

		// 存在するゲームを回す
		for (Game game : plugin.games.values()){
			if (!game.isStarting()) continue;

			GameTeam dTeam = game.getPlayerTeam(deader);
			GameTeam aTeam = game.getPlayerTeam(killer);

			// 同じチームの場合そのゲームに
			if (dTeam != null && aTeam != null){
				deathMsg = msgPrefix+"&6["+game.getName()+"] "+aTeam.getColor()+killer.getName()+"&6 が "+dTeam.getColor()+deader.getName()+"&6 を倒しました！";
				Actions.broadcastMessage(deathMsg);
				//game.message(deathMsg); ブロードキャストがうるさそうならこっちでゲーム参加者にだけキャスト
				return;
			}
		}
	}
}
