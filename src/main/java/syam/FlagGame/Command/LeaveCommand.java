package syam.FlagGame.Command;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import syam.FlagGame.Enum.GameResult;
import syam.FlagGame.Enum.GameTeam;
import syam.FlagGame.FGPlayer.PlayerManager;
import syam.FlagGame.FGPlayer.PlayerProfile;
import syam.FlagGame.Game.Game;
import syam.FlagGame.Game.GameManager;
import syam.FlagGame.Permission.Perms;
import syam.FlagGame.Util.Actions;

public class LeaveCommand extends BaseCommand{
	public LeaveCommand(){
		bePlayer = true;
		name = "leave";
		argLength = 0;
		usage = "<leave> <- leave the game";
	}

	@Override
	public void execute() {
		// 参加しているゲームを取得する
		Game game = null;
		GameTeam team = null;
		for (Game g : GameManager.games.values()){
			if (g.getPlayerTeam(player) != null){
				game = g;
				team = g.getPlayerTeam(player);
				break;
			}
		}

		World world = player.getWorld();

		// ゲームに参加していないプレイヤー
		if (game == null){
			// check permission
			if (!Perms.LEAVE_SPECTATE.has(sender)){
				Actions.message(sender, null, "&cあなたはゲームに参加していません");
				return;
			}

			// ゲームワールド内
			if (world.equals(Bukkit.getWorld(plugin.getConfigs().getGameWorld()))){
				leaveFromGameworld(player, world.getSpawnLocation());
			}
			// 別ワールド
			else{
				Actions.message(null, player, "&cこのゲームワールド外からこのコマンドを使うことはできません！");
			}
		}
		// ゲームに参加しているプレイヤー
		else{
			// ゲーム開始中
			if (game.isStarting()){
				// check permission
				if (!Perms.LEAVE_GAME.has(sender)){
					Actions.message(sender, null, "&cゲームを途中退場する権限がありません");
					return;
				}

				game.remPlayer(player, team);

				// アイテムをすべてその場にドロップさせる
				player.getInventory().setHelmet(null);
				Actions.dropInventoryItems(player);

				player.teleport(world.getSpawnLocation(), TeleportCause.PLUGIN);

				Actions.broadcastMessage(msgPrefix+ "&aプレイヤー'"+team.getColor()+player.getName()+"&a'がゲーム'&6"+game.getName()+"'&aから途中退場しました！");
				Actions.message(null, player, "&aゲーム'"+game.getName()+"'から抜けました！");

				// exit++
				PlayerManager.getProfile(player.getName()).addExit();

				// 参加者チェック 全員抜けたらゲーム終了
				Iterator<Entry<GameTeam, Set<String>>> entryIte = game.getPlayersMap().entrySet().iterator();
				while(entryIte.hasNext()){
					Entry<GameTeam, Set<String>> entry = entryIte.next();
					if (entry.getValue().size() <= 0){
						GameTeam t = entry.getKey();
						game.finish(GameResult.STOP, null, "&6"+t.getColor()+t.getTeamName()+"チーム &6の参加者が居なくなりました");
						break;
					}
				}
			}
			// ゲーム待機中
			else if (game.isReady()){
				// check permission
				if (!Perms.LEAVE_READY.has(sender)){
					Actions.message(sender, null, "&cゲームのエントリーを取り消す権限がありません");
					return;
				}
				// プレイヤーリストから削除
				game.remPlayer(player, team);

				Actions.broadcastMessage(msgPrefix+ "&aプレイヤー'"+team.getColor()+player.getName()+"&a'がゲーム'&6"+game.getName()+"'&aのエントリーを取り消しました！");
				Actions.message(null, player, "&aゲーム'"+game.getName()+"'の参加申請を取り消しました！");
			}
			// 例外
			else{
				Actions.message(null, player, "&c内部エラー: LeaveCommand.class");
				log.warning(logPrefix+ "Internal Exception on LeaveCommand.class, Please report this.");
			}
		}
	}

	private void leaveFromGameworld(Player player, Location def){
		PlayerProfile prof = PlayerManager.getProfile(player.getName());

		// プレイヤーデータに以前の座標が記録されていればその場所へTp
		if (prof.isLoaded() && prof.getTpBackLocation() != null){
			Location loc = prof.getTpBackLocation();

			player.teleport(loc, TeleportCause.PLUGIN);
			prof.setTpBackLocation(null);

			Actions.message(null, player, "&aテレポートしました！");
		}else{
			player.teleport(def, TeleportCause.PLUGIN);

			Actions.message(null, player, "&aゲームワールドのスポーン地点に戻りました！");
		}
	}

	@Override
	public boolean permission() {
		return (Perms.LEAVE_GAME.has(sender) ||
				Perms.LEAVE_READY.has(sender) ||
				Perms.LEAVE_SPECTATE.has(sender));
	}
}
