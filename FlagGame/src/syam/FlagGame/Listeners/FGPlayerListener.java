package syam.FlagGame.Listeners;

import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
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
import syam.FlagGame.Enum.GameTeam;
import syam.FlagGame.Enum.SignAction;
import syam.FlagGame.Game.Game;
import syam.FlagGame.Game.GameManager;
import syam.FlagGame.Util.Actions;
import syam.FlagGame.Util.Cuboid;

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
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPlayerInteract(final PlayerInteractEvent event){
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();

		if(block != null){
			// フラッグ管理モード
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK && GameManager.isFlagManager(player)){
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
				return;
			}
			// チェスト管理モード
			else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && GameManager.isChestManager(player)){
				if (player.getItemInHand().getTypeId() == plugin.getConfigs().toolID && player.hasPermission("flag.admin")){
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

					// チェスト、かまど、ディスペンサーのどれかでなければ返す
					if (block.getType() != Material.CHEST && block.getType() != Material.FURNACE && block.getType() != Material.DISPENSER){
						return;
					}

					// 既にフラッグブロックになっているか判定
					if (game.getChest(loc) == null){
						// 選択
						game.setChest(loc);
						Actions.message(null, player, "&aゲーム'"+game.getName()+"'のチェストを設定しました！");
					}else{
						// 削除
						game.removeChest(loc);
						Actions.message(null, player, "&aゲーム'"+game.getName()+"'のチェストを削除しました！");
					}
					event.setCancelled(true);
					event.setUseInteractedBlock(Result.DENY);
					event.setUseItemInHand(Result.DENY);
				}
				return;
			}

			// 看板を右クリックした
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK && block.getState() instanceof Sign){
				Sign sign = (Sign) block.getState();
				// 1行目チェック
				if (sign.getLine(0).equals("§a[FlagGame]")){
					clickFlagSign(player, block);
				}
			}
		}
	}

	// プレイヤーがブロックをクリックした
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerOpen(final PlayerInteractEvent event){
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();

		// ゲーム用ワールドでなければ返す
		if (block.getWorld() != Bukkit.getWorld(plugin.getConfigs().gameWorld))
			return;

		if (block != null){
			Material type = block.getType();
			// ブロックを左または右クリックした
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK){
				// クリックしたブロックがドア関係
				if (type == Material.WOODEN_DOOR || type == Material.IRON_DOOR || type == Material.FENCE_GATE){
					// 使用可能かチェック
					if (!canUseBlock(player, block)){
						event.setUseInteractedBlock(Result.DENY);
						event.setUseItemInHand(Result.DENY);
						event.setCancelled(true);
						Actions.message(null, player, msgPrefix+ "&cここは相手の拠点です！");
					}
					return;
				}
			}

			// ブロックを右クリックした
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK){
				// ブロックがコンテナ関係か看板関係
				if (type == Material.CHEST || type == Material.FURNACE || type == Material.DISPENSER || type == Material.JUKEBOX){
					// 使用可能かチェック
					if (!canUseBlock(player, block)){
						event.setUseInteractedBlock(Result.DENY);
						event.setUseItemInHand(Result.DENY);
						event.setCancelled(true);
						Actions.message(null, player, msgPrefix+ "&cここは相手の拠点です！");
					}
					return;
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
					Actions.message(null, player, msgPrefix+ "&6このゲームはあと "+Actions.getTimeString(game.getRemainTime())+" 残っています！");
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
				//game.message(msgPrefix+ team.getColor()+team.getTeamName()+"チーム &6のプレイヤー'"+team.getColor()+player.getName()+"&6'がログアウトしたため死亡しました");
				Actions.worldcastMessage(Bukkit.getWorld(plugin.getConfigs().gameWorld),
						msgPrefix+ team.getColor()+team.getTeamName()+"チーム &6のプレイヤー'"+team.getColor()+player.getName()+"&6'がログアウトしたため死亡しました");
				game.log(" Player "+player.getName()+" Died because Logged out!");
			}
		}
	}

	// プレイヤーがコマンドを使おうとした
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event){
		Player player = event.getPlayer();
		// ワールドチェック
		if (player.getWorld() != Bukkit.getWorld(plugin.getConfigs().gameWorld))
			return;

		String cmdMsg = event.getMessage().trim();
		String cmds[] = cmdMsg.split(" ");
		String cmd = null;

		if (cmds.length > 1){
			cmd = cmds[0].trim();
		}else{ // cmds.length == 1
			cmd = cmdMsg;
		}

		// 存在するゲームを回す
		for (Game game : plugin.games.values()){
			if (!game.isReady() && !game.isStarting())
				return;

			GameTeam team = game.getPlayerTeam(player);
			if (team != null){
				// ゲーム中のプレイヤー 禁止コマンドを操作
				for (String s : plugin.getConfigs().disableCommands){
					// 禁止コマンドと同じコマンドがある
					if (s.trim().equalsIgnoreCase(cmd)){
						// コマンド実行キャンセル
						event.setCancelled(true);
						Actions.message(null, player, msgPrefix+"このコマンドは試合中に使えません！");
						return;
					}
				}
			}
		}
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

				Actions.worldcastMessage(Bukkit.getWorld(plugin.getConfigs().gameWorld),deathMsg);

				//for (Player player : Bukkit.getOnlinePlayers())
				//	Actions.message(null, player, deathMsg);

				//Actions.broadcastMessage(deathMsg); // 死亡したプレイヤーには送信されない？
				//game.message(deathMsg); ブロードキャストがうるさそうならこっちでゲーム参加者にだけキャスト
				game.log(" Player ("+aTeam.name()+")"+killer.getName()+" Killed ("+dTeam.name()+")"+deader.getName()+"!");
				return;
			}
		}
	}


	/* methods */

	private boolean canUseBlock(Player player, Block block){
		// ワールドがゲーム用ワールドでなければ常にtrueを返す
		if (block.getWorld() != Bukkit.getWorld(plugin.getConfigs().gameWorld))
			return true;

		Location loc = block.getLocation();
		// 開始中のゲームを回す
		for (Game game : plugin.games.values()){
			if (!game.isStarting()) continue;

			// プレイヤーのチーム取得
			GameTeam playerTeam = game.getPlayerTeam(player);
			if (playerTeam == null) continue;

			GameTeam blockTeam = null;
			// 拠点マップを回してブロックの所属拠点チームを取得
			for (Map.Entry<GameTeam, Cuboid> entry : game.getBases().entrySet()){
				if (entry.getValue().isIn(loc)){
					blockTeam = entry.getKey();
					break;
				}
			}
			// パブリックなものは何もしない
			if (blockTeam == null) continue;

			// プレイヤーとブロックのチームが違えばイベントをキャンセルする
			if (playerTeam != blockTeam){
				return false; // 開けない
			}
		}
		// 開ける
		return true;
	}

	private void clickFlagSign(Player player, Block block){
		if (!(block.getState() instanceof Sign)) return;

		Sign sign = (Sign) block.getState();
		String line2 = sign.getLine(1); // 2行目
		String line3 = sign.getLine(2); // 3行目

		SignAction action = null;
		for (SignAction sa : SignAction.values()){
			if (sa.name().toLowerCase().equalsIgnoreCase(line2.trim()))
				action = sa;
		}
		if (action == null){
			Actions.message(null, player, "&cThis sign is broken! Please contact server staff!");
			return;
		}

		// 処理を分ける
		switch (action){
			// 回復
			case HEAL:
				if (line3 != "" && !line3.isEmpty()){
					GameTeam signTeam = null;
					GameTeam playerTeam = null;
					for (GameTeam gt : GameTeam.values()){
						if (gt.name().toLowerCase().equalsIgnoreCase(line3))
							signTeam = gt;
					}
					if (signTeam == null){
						Actions.message(null, player, "&cThis sign is broken! Please contact server staff!");
						return;
					}
					for (Game game : plugin.games.values()){
						if (!game.isStarting()) continue;
						if (game.getPlayerTeam(player) != null){
							playerTeam = game.getPlayerTeam(player);
							break;
						}
					}
					if (playerTeam == null){
						Actions.message(null, player, "&cこの看板はフラッグゲーム中にのみ使うことができます");
						return;
					}

					if (playerTeam != signTeam){
						Actions.message(null, player, "&cこれはあなたのチームの看板ではありません！");
						return;
					}
				}

				// 20以上にならないように体力とお腹ゲージを+2(ハート、おにく1つ分)回復させる
				int nowHP = player.getHealth();
				nowHP = nowHP + 2;
				if (nowHP > 20) nowHP = 20;

				int nowFL = player.getFoodLevel();
				nowFL = nowFL + 2;
				if (nowFL > 20) nowFL = 20;

				// プレイヤーにセット
				player.setHealth(nowHP);
				player.setFoodLevel(nowFL);
				player.setFireTicks(0); // 燃えてれば消してあげる

				Actions.message(null, player, msgPrefix+ "&aHealed!");

				break;
			// 自殺
			case KILL:
				for (Game game : plugin.games.values()){
					if (!game.isStarting()) continue;
					if (game.getPlayerTeam(player) != null){
						GameTeam team = game.getPlayerTeam(player);
						game.message(msgPrefix+"&6["+game.getName()+"]&6 '"+team.getColor()+player.getName()+"&6'が自殺しました。");
						break;
					}
				}
				player.setHealth(0);
				player.setFoodLevel(0);
				break;
			default:
				Actions.message(null, player, msgPrefix+"&cSorry I forgot this sign-action. Please contact server staff!");
		}

	}
}
