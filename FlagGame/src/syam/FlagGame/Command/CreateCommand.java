package syam.FlagGame.Command;

import syam.FlagGame.Actions;
import syam.FlagGame.Game.Game;

public class CreateCommand extends BaseCommand {
	public CreateCommand(){
		bePlayer = true;
		name = "create";
		argLength = 1;
		usage = "<name> <- create new map";
	}

	@Override
	public boolean execute() {
		if (args.size() == 0){
			Actions.message(sender, null, "&cゲーム名を入力してください！ /flag create (name)");
			return true;
		}
		Game game = plugin.getGame(args.get(0));
		if (game != null){
			Actions.message(sender, null, "&cそのゲーム名は既に存在します！");
			return true;
		}

		// 新規ゲーム登録
		game = new Game(plugin, args.get(0));

		Actions.message(sender, null, "&a新規ゲーム'"+game.getName()+"'を登録しました！");
		return true;
	}

	@Override
	public boolean permission() {
		return sender.hasPermission("flag.admin");
	}
}
