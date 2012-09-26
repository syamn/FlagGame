package syam.FlagGame.Command;

import syam.FlagGame.Enum.GameTeam;
import syam.FlagGame.Game.Game;
import syam.FlagGame.Game.Stage;
import syam.FlagGame.Manager.GameManager;
import syam.FlagGame.Manager.StageManager;
import syam.FlagGame.Permission.Perms;
import syam.FlagGame.Util.Actions;

public class JoinCommand extends BaseCommand {
	public JoinCommand(){
		bePlayer = true;
		name = "join";
		argLength = 1;
		usage = "<game> <- join the game";
	}

	@Override
	public void execute() {
		Stage stage = StageManager.getStage(args.get(0));
		if (stage == null){
			Actions.message(null, player, "&cステージ'"+args.get(0)+"'が見つかりません");
			return;
		}

		Game game = null;
		if (stage.isUsing() && stage.getGame() != null){
			game = stage.getGame();
		}else{
			Actions.message(null, player, "&cステージ'"+args.get(0)+"'は現在参加受付中ではありません");
			return;
		}

		if (game.isStarting()){
			Actions.message(null, player, "&cゲーム'"+args.get(0)+"'は既に始まっています！");
			return;
		}

		// 既に参加していないかチェック
		if (game.getPlayerTeam(player) != null){
			GameTeam team = game.getPlayerTeam(player);
			Actions.message(null, player, "&cあなたは既にこのゲームに"+team.getColor()+team.getTeamName()+"チーム&cとしてエントリーしています！");
			return;
		}
		for (Game check : GameManager.getGames().values()){
			GameTeam checkT = check.getPlayerTeam(player);
			if (checkT != null){
				Actions.message(null, player, "&cあなたは別のゲーム'"+check.getName()+"'に"+checkT.getColor()+checkT.getTeamName()+"チーム&cとして参加しています！");
				return;
			}
		}

		// 人数チェック
		int limit = game.getStage().getTeamLimit();
		if ((game.getPlayersSet(GameTeam.RED).size() >= limit) && (game.getPlayersSet(GameTeam.BLUE).size() >= limit)){
			Actions.message(null, player, "&cこのゲームは参加可能な定員に達しています！");
			return;
		}

		// 参加料チェック
		if (game.getStage().getEntryFee() > 0){
			// 所持金確認
			if (!Actions.checkMoney(player.getName(), game.getStage().getEntryFee())){
				Actions.message(null, player, "&c参加するためには参加料 "+game.getStage().getEntryFee()+"Coin が必要です！");
				return;
			}
			// 引き落とし
			if (!Actions.takeMoney(player.getName(), game.getStage().getEntryFee())){
				Actions.message(null, player, "&c参加料の引き落としにエラーが発生しました。管理人までご連絡ください。");
				return;
			}else{
				Actions.message(null, player, "&c参加料として "+game.getStage().getEntryFee()+"Coin を支払いました！");
			}
		}

		// join
		game.addPlayer(player);

		// 所属チーム取得
		GameTeam team = game.getPlayerTeam(player);
		Actions.broadcastMessage(msgPrefix+"&aプレイヤー'&6"+player.getName()+"&a'が"+team.getColor()+team.getTeamName()+"チーム&aに参加しました！");

		// 参加後に人数チェックして定員通知
		if ((game.getPlayersSet(GameTeam.RED).size() >= limit) && (game.getPlayersSet(GameTeam.BLUE).size() >= limit)){
			Actions.message(null, player, "&aゲーム'"+game.getName()+"'が定員("+limit*2+"人)に達しました！");
		}
	}

	@Override
	public boolean permission() {
		return Perms.JOIN.has(sender);
	}
}
