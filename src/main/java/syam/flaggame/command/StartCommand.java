package syam.flaggame.command;

import java.util.ArrayList;
import java.util.Set;

import syam.flaggame.exception.CommandException;
import syam.flaggame.game.Game;
import syam.flaggame.manager.GameManager;
import syam.flaggame.manager.StageManager;
import syam.flaggame.permission.Perms;
import syam.flaggame.util.Actions;

public class StartCommand extends BaseCommand{
	public StartCommand(){
		bePlayer = false;
		name = "start";
		argLength = 0;
		usage = "[stage] <- start game";
	}

	@Override
	public void execute() throws CommandException {
		Game game = null;

		// 引数があれば指定したステージを開始
		if (args.size() >= 1){
			if (args.get(0).equalsIgnoreCase("random")){
				// ランダムステージ
				game = GameManager.getRandomGame();
				if (game == null || !game.isReady()){
					throw new CommandException("&c参加受付状態のランダムステージはありません！");
				}
			}else{
				game = GameManager.getGame(args.get(0));
			}
		}
		// 引数がなければ自動補完
		else{
			ArrayList<Game> readyingGames = GameManager.getReadyingGames();
			if (readyingGames.size() <= 0){
				throw new CommandException("&c現在受付中のゲームはありません！");
			}else if (readyingGames.size() >= 2){
				throw new CommandException("&c複数のゲームが受付中です！開始するステージを指定してください！");
			}
			// 受付中のステージが1つのみなら自動補完
			else{
				game = readyingGames.get(0);
			}
		}

		if (game == null || !game.isReady()){
			// そのステージが本当にあるかチェック
			if (StageManager.getStage(args.get(0)) == null){
				throw new CommandException("&cステージ'"+args.get(0)+"'が見つかりません");
			}else{
				throw new CommandException("&cステージ'"+args.get(0)+"'は参加受付状態ではありません");
			}
		}

		for (Set<String> teamSet : game.getPlayersMap().values()){
			if (teamSet.size() <= 0){
				throw new CommandException("&cプレイヤーが参加していないチームがあります");
			}
		}

		// check starting countdown
		if (game.getStarttimerThreadID() != -1){
			throw new CommandException("&cこのゲームは既に開始カウントダウン中です！");
		}

		// start
		//game.start(sender);
		game.start_timer(sender);
	}

	@Override
	public boolean permission() {
		return Perms.START.has(sender);
	}
}
