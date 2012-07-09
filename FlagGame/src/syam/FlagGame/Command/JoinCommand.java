package syam.FlagGame.Command;

import syam.FlagGame.Game.Game;
import syam.FlagGame.Game.GameTeam;
import syam.FlagGame.Util.Actions;

public class JoinCommand extends BaseCommand {
	public JoinCommand(){
		bePlayer = true;
		name = "join";
		argLength = 1;
		usage = "<game> <- join game";
	}

	@Override
	public boolean execute() {
		Game game = plugin.getGame(args.get(0));
		if (game == null){
			Actions.message(null, player, "&cゲーム'"+args.get(0)+"'が見つかりません");
			return true;
		}

		if (!game.isReady()){
			Actions.message(null, player, "&cゲーム'"+args.get(0)+"'は現在参加受付中ではありません");
			return true;
		}

		// 既に参加していないかチェック
		if (game.getPlayerTeam(player) != null){
			GameTeam team = game.getPlayerTeam(player);
			Actions.message(null, player, "&cあなたは既にこのゲームに"+team.getColor()+team.getTeamName()+"チーム&cとしてエントリーしています！");
			return true;
		}
		for (Game check : plugin.games.values()){
			GameTeam checkT = check.getPlayerTeam(player);
			if (checkT != null){
				Actions.message(null, player, "&cあなたは別のフラッグゲーム'"+check.getName()+"'に"+checkT.getColor()+checkT.getTeamName()+"チーム&cとして参加しています！");
				return true;
			}
		}

		// 人数チェック
		int limit = game.getTeamPlayerLimit();
		if ((game.getPlayersSet(GameTeam.RED).size() >= limit) && (game.getPlayersSet(GameTeam.BLUE).size() >= limit)){
			Actions.message(null, player, "&cこのゲームは参加可能な定員に達しています！");
			return true;
		}

		// join
		game.addPlayer(player);

		// 所属チーム取得
		GameTeam team = game.getPlayerTeam(player);
		Actions.broadcastMessage(msgPrefix+"&aプレイヤー'&6"+player.getName()+"&a'が"+team.getColor()+team.getTeamName()+"チーム&aに参加しました！");
		return true;
	}

	@Override
	public boolean permission() {
		return sender.hasPermission("flag.user.join");
	}
}
