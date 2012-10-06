package syam.flaggame.command;

import java.util.ArrayList;

import syam.flaggame.enums.GameTeam;
import syam.flaggame.exception.CommandException;
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
	public void execute() throws CommandException {
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
					throw new CommandException("&cステージ'"+args.get(0)+"'が見つかりません");
				}
			}
		}
		// 引数がなければ自動補完
		else{
			ArrayList<Game> readyingGames = GameManager.getReadyingGames();
			if (readyingGames.size() <= 0){
				throw new CommandException("&c現在、参加受付中のゲームはありません！");
			}else if (readyingGames.size() >= 2){
				throw new CommandException("&c複数のゲームが受付中です！参加するステージを指定してください！");
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
				throw new CommandException("&cステージ'"+args.get(0)+"'は現在参加受付中ではありません");
			}
		}
		// ランダムゲーム
		else{
			game = GameManager.getRandomGame();
			if (game == null){
				throw new CommandException("&c現在受付中のランダムステージはありません！");
			}
		}

		if (game.isStarting()){
			throw new CommandException("&cゲーム'"+args.get(0)+"'は既に始まっています！");
		}

		// 既に参加していないかチェック
		if (game.getPlayerTeam(player) != null){
			GameTeam team = game.getPlayerTeam(player);
			throw new CommandException("&cあなたは既にこのゲームに"+team.getColor()+team.getTeamName()+"チーム&cとしてエントリーしています！");
		}
		for (Game check : GameManager.getGames().values()){
			GameTeam checkT = check.getPlayerTeam(player);
			if (checkT != null){
				throw new CommandException("&cあなたは別のゲーム'"+check.getName()+"'に"+checkT.getColor()+checkT.getTeamName()+"チーム&cとして参加しています！");
			}
		}

		// 人数チェック
		int limit = game.getStage().getTeamLimit();
		if ((game.getPlayersSet(GameTeam.RED).size() >= limit) && (game.getPlayersSet(GameTeam.BLUE).size() >= limit)){
			throw new CommandException("&cこのゲームは参加可能な定員に達しています！");
		}

		// 参加料チェック
		if (game.getStage().getEntryFee() > 0){
			// 所持金確認
			if (!Actions.checkMoney(player.getName(), game.getStage().getEntryFee())){
				throw new CommandException("&c参加するためには参加料 "+game.getStage().getEntryFee()+"Coin が必要です！");
			}
			// 引き落とし
			if (!Actions.takeMoney(player.getName(), game.getStage().getEntryFee())){
				throw new CommandException("&c参加料の引き落としにエラーが発生しました。管理人までご連絡ください。");
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
