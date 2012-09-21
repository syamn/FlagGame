package syam.FlagGame.Command;

import syam.FlagGame.Game.OldGame;
import syam.FlagGame.Game.GameManager;
import syam.FlagGame.Permission.Perms;
import syam.FlagGame.Util.Actions;

public class CreateCommand extends BaseCommand {
	public CreateCommand(){
		bePlayer = true;
		name = "create";
		argLength = 1;
		usage = "<name> <- create new game";
	}

	@Override
	public void execute() {
		if (args.size() == 0){
			Actions.message(sender, null, "&cゲーム名を入力してください！ /flag create (name)");
			return;
		}
		OldGame game = plugin.getGame(args.get(0));
		if (game != null){
			Actions.message(sender, null, "&cそのゲーム名は既に存在します！");
			return;
		}

		// 新規ゲーム登録
		game = new OldGame(plugin, args.get(0));
		GameManager.setSelectedGame(player, game);

		// update dynmap
		plugin.getDynmap().updateRegions();
		// save project
		plugin.getFileManager().saveGames();

		Actions.message(sender, null, "&a新規ゲーム'"+game.getName()+"'を登録して選択しました！");
	}

	@Override
	public boolean permission() {
		return Perms.CREATE.has(sender);
	}
}
