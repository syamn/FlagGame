package syam.FlagGame.Command;

import syam.FlagGame.Actions;
import syam.FlagGame.Game.Game;

public class ReadyCommand extends BaseCommand {
	public ReadyCommand(){
		bePlayer = true;
		name = "ready";
		argLength = 1;
		usage = "<name> <- ready game";
	}

	@Override
	public boolean execute() {
		// flagadmin ready - ゲームを開始準備中にする
		if (args.size() == 0){
			Actions.message(sender, null, "&cゲーム名を入力してください！ /fg ready (name)");
			return true;
		}

		Game game = plugin.getGame(args.get(0));
		if (game == null){
			Actions.message(sender, null, "&cゲーム'"+args.get(0)+"'が見つかりません");
			return true;
		}

		return true;
	}

	@Override
	public boolean permission() {
		return sender.hasPermission("flag.admin");
	}
}
