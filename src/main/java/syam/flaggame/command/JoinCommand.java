package syam.flaggame.command;

import java.util.ArrayList;

import syam.flaggame.enums.GameTeam;
import syam.flaggame.game.Game;
import syam.flaggame.game.Stage;
import syam.flaggame.manager.GameManager;
import syam.flaggame.manager.StageManager;
import syam.flaggame.permission.Perms;
import syam.flaggame.util.Actions;

public class JoinCommand extends BaseCommand {
	public JoinCommand(){
		bePlayer = true;
		name = "join";
		argLength = 0;
		usage = "[game] <- join the game";
	}

	@Override
	public void execute() {
		boolean random = false;
		Stage stage = null;
		Game game = null;

		// 引数があれば指定したステージに参加
		if (args.size() >= 1){
			if (args.get(0).equalsIgnoreCase("random")){
				random = true;
			}else{
				stage = StageManager.getStage(args.get(0));
				if (stage == null){
					Actions.message(null, player, "&cステージ'"+args.get(0)+"'が見つかりません");
					return;
				}
			}
		}
		// 引数がなければ自動補完
		else{
			ArrayList<Game> readyingGames = GameManager.getReadyingGames();
			if (readyingGames.size() <= 0){
				Actions.message(null, player, "&c現在、参加受付中のゲームはありません！");
				return;
			}else if (readyingGames.size() >= 2){
				Actions.message(null, player, "&c複数のゲームが受付中です！参加するステージを指定してください！");
				return;
			}
			// 受付中のステージが1つのみなら自動補完
			else{
				stage = readyingGames.get(0).getStage();
			}
		}

		if (!random){
			if (stage.isUsing() && stage.getGame() != null){
				game = stage.getGame();
			}else{
				Actions.message(null, player, "&cステージ'"+args.get(0)+"'は現在参加受付中ではありません");
				return;
			}
		}
		// ランダムゲーム
		else{
			game = GameManager.getRandomGame();
			if (game == null){
				Actions.message(null, player, "&c現在受付中のランダムステージはありません！");
				return;
			}
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
			Actions.message(null, player, "&aこのゲームは参加定員("+limit*2+"人)に達しています！");
		}
	}

	@Override
	public boolean permission() {
		return Perms.JOIN.has(sender);
	}
}
