package syam.FlagGame.Command;

import java.util.Random;

import syam.FlagGame.Game.Game;
import syam.FlagGame.Permission.Perms;
import syam.FlagGame.Util.Actions;

public class ReadyCommand extends BaseCommand {
	public ReadyCommand(){
		bePlayer = false;
		name = "ready";
		argLength = 1;
		usage = "<game> <- ready game";
	}

	@Override
	public void execute() {
		// flagadmin ready - ゲームを開始準備中にする
		if (args.size() == 0){
			Actions.message(sender, null, "&cゲーム名を入力してください！ /fg ready (name)");
			return;
		}

		Game game = null;

		// ランダム
		if (args.get(0).equalsIgnoreCase("random")){
			game = plugin.getGame(getRandomStage());
			// TODO: 開始中のゲームが選択される可能性 ランダムアナウンスができない
		}
		// 通常のゲーム
		else{
			game = plugin.getGame(args.get(0));
		}

		if (game == null){
			Actions.message(sender, null, "&cゲーム'"+args.get(0)+"'が見つかりません");
			return;
		}

		// ready
		game.ready(sender);
	}

	/**
	 * ランダムなステージ名を返す
	 * @return ステージ名
	 */
	private String getRandomStage(){
		Random rnd = new Random();

		String[] games = plugin.games.keySet().toArray(new String[plugin.games.size()]);
		String game = games[rnd.nextInt(games.length)];

		return game;
	}

	@Override
	public boolean permission() {
		return Perms.READY.has(sender);
	}
}
