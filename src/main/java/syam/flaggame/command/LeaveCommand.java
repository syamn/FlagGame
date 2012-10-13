package syam.flaggame.command;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import syam.flaggame.command.queue.Queueable;
import syam.flaggame.enums.GameResult;
import syam.flaggame.enums.GameTeam;
import syam.flaggame.exception.CommandException;
import syam.flaggame.game.Game;
import syam.flaggame.manager.GameManager;
import syam.flaggame.permission.Perms;
import syam.flaggame.player.PlayerManager;
import syam.flaggame.player.PlayerProfile;
import syam.flaggame.util.Actions;

public class LeaveCommand extends BaseCommand implements Queueable{
	public LeaveCommand(){
		bePlayer = true;
		name = "leave";
		argLength = 0;
		usage = "<leave> <- leave the game";
	}

	@Override
	public void execute() throws CommandException {
		// 参加しているゲームを取得する
		Game game = null;
		GameTeam team = null;
		for (Game g : GameManager.getGames().values()){
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
				throw new CommandException("&cあなたはゲームに参加していません");
			}

			// ゲームワールド内
			if (world.equals(Bukkit.getWorld(plugin.getConfigs().getGameWorld()))){
				leaveFromGameworld(player, world.getSpawnLocation());
			}
			// 別ワールド
			else{
				throw new CommandException("&cこのゲームワールド外からこのコマンドを使うことはできません！");
			}
		}
		// ゲームに参加しているプレイヤー
		else{
			// ゲーム開始中
			if (game.isStarting()){
				// check permission
				if (!Perms.LEAVE_GAME.has(sender)){
					throw new CommandException("&cゲームを途中退場する権限がありません");
				}

				// confirmキュー追加
				plugin.getQueue().addQueue(sender, this, args, 15);
				Actions.message(sender, "&d途中退場回数は記録されます。本当にこのゲームを途中退場しますか？");
				Actions.message(sender, "&d退場するには &a/flag confirm &dコマンドを入力してください。");
				Actions.message(sender, "&a/flag confirm &dコマンドは15秒間のみ有効です。");
			}
			// ゲーム待機中
			else if (game.isReady()){
				// check permission
				if (!Perms.LEAVE_READY.has(sender)){
					throw new CommandException("&cゲームのエントリーを取り消す権限がありません");
				}
				// プレイヤーリストから削除
				game.remPlayer(player, team);

				String stageName = game.getName();
				if (game.isRandom() && game.isReady()) stageName = "ランダムステージ";
				Actions.broadcastMessage(msgPrefix+ "&a'"+team.getColor()+player.getName()+"&a'がゲーム'&6"+stageName+"&a'のエントリーを取り消しました！");

				Actions.message(player, "&aゲーム'"+stageName+"'の参加申請を取り消しました！");
			}
			// 例外
			else{
				Actions.message(player, "&c内部エラー: LeaveCommand.class");
				log.warning(logPrefix+ "Internal Exception on LeaveCommand.class, Please report this.");
			}
		}
	}

	/**
	 * ゲーム中に離脱する際確認する
	 */
	@Override
	public void executeQueue(List<String> args) {
		// 参加しているゲームを取得する
		Game game = null;
		GameTeam team = null;
		for (Game g : GameManager.getGames().values()){
			if (g.getPlayerTeam(player) != null){
				game = g;
				team = g.getPlayerTeam(player);
				break;
			}
		}
		if (game == null || !game.isStarting()){
			Actions.message(sender, "&c既にゲームは終了しています！");
			return;
		}

		// 途中退場処理

		game.remPlayer(player, team);

		// アイテムをすべてその場にドロップさせる
		player.getInventory().setHelmet(null);
		Actions.dropInventoryItems(player);

		// テレポート
		Location tpLoc = game.getStage().getSpecSpawn();
		if (tpLoc == null){
			tpLoc = player.getWorld().getSpawnLocation();
		}
		player.teleport(tpLoc, TeleportCause.PLUGIN);

		Actions.broadcastMessage(msgPrefix+ "&aプレイヤー'"+team.getColor()+player.getName()+"&a'がゲーム'&6"+game.getName()+"&a'から途中退場しました！");
		Actions.message(player, "&aゲーム'"+game.getName()+"'から抜けました！");

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

	private void leaveFromGameworld(Player player, Location def){
		PlayerProfile prof = PlayerManager.getProfile(player.getName());

		// プレイヤーデータに以前の座標が記録されていればその場所へTp
		if (prof.isLoaded() && prof.getTpBackLocation() != null){
			Location loc = prof.getTpBackLocation();

			player.teleport(loc, TeleportCause.PLUGIN);
			prof.setTpBackLocation(null);

			Actions.message(player, "&aテレポートしました！");
		}else{
			player.teleport(def, TeleportCause.PLUGIN);

			Actions.message(player, "&aゲームワールドのスポーン地点に戻りました！");
		}
	}

	@Override
	public boolean permission() {
		return (Perms.LEAVE_GAME.has(sender) ||
				Perms.LEAVE_READY.has(sender) ||
				Perms.LEAVE_SPECTATE.has(sender));
	}
}
