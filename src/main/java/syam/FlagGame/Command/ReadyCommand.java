package syam.FlagGame.Command;

import syam.FlagGame.Game.Game;
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

		Game game = plugin.getGame(args.get(0));
		if (game == null){
			Actions.message(sender, null, "&cゲーム'"+args.get(0)+"'が見つかりません");
			return;
		}

		// ready
		game.ready(sender);
	}

	@Override
	public boolean permission() {
		return sender.hasPermission("flag.admin.ready");
	}
}
