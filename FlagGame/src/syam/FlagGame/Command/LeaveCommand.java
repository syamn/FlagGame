package syam.FlagGame.Command;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import syam.FlagGame.Enum.GameResult;
import syam.FlagGame.Enum.GameTeam;
import syam.FlagGame.Game.Game;
import syam.FlagGame.Util.Actions;

public class LeaveCommand extends BaseCommand{
	public LeaveCommand(){
		bePlayer = true;
		name = "leave";
		argLength = 0;
		usage = "<leave> <- leave the game";
	}

	@Override
	public boolean execute() {
		// 参加しているゲームを取得する
		Game game = null;
		GameTeam team = null;
		for (Game g : plugin.games.values()){
			if (!g.isStarting() && !g.isReady()) continue;
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
			if (!sender.hasPermission("flag.user.leave.spectate")){
				Actions.message(sender, null, "&cあなたはゲームに参加していません");
				return true;
			}

			// ゲームワールド内
			if (world.equals(Bukkit.getWorld(plugin.getConfigs().gameWorld))){
				player.teleport(world.getSpawnLocation(), TeleportCause.PLUGIN);
				Actions.message(null, player, "&aゲームワールドのスポーン地点に戻りました！");
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
				if (!sender.hasPermission("flag.user.leave.game")){
					Actions.message(sender, null, "&cゲームを途中退場する権限がありません");
					return true;
				}

				game.remPlayer(player, team);

				// アイテムをすべてその場にドロップさせる
				player.getInventory().setHelmet(null);
				Actions.dropInventoryItems(player);

				player.teleport(world.getSpawnLocation(), TeleportCause.PLUGIN);

				Actions.broadcastMessage(msgPrefix+ "&aプレイヤー'"+team.getColor()+player.getName()+"&a'がゲーム'&6"+game.getName()+"'&aから途中退場しました！");
				Actions.message(null, player, "&aゲーム'"+game.getName()+"'から抜けました！");

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
				if (!sender.hasPermission("flag.user.leave.ready")){
					Actions.message(sender, null, "&cゲームのエントリーを取り消す権限がありません");
					return true;
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

		return true;
	}

	@Override
	public boolean permission() {
		if (sender.hasPermission("flag.user.leave.spectate") ||
			sender.hasPermission("flag.user.leave.game") ||
			sender.hasPermission("flag.user.leave.ready")) {
			return true;
		}else{
			return false;
		}
	}
}
