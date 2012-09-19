package syam.FlagGame.Command;

import syam.FlagGame.Enum.Config.Configables;
import syam.FlagGame.Game.Game;
import syam.FlagGame.Game.GameManager;
import syam.FlagGame.Util.Actions;

public class SelectCommand extends BaseCommand {
	public SelectCommand(){
		bePlayer = true;
		name = "select";
		argLength = 0;
		usage = "[game] <- select exist game";
	}

	@Override
	public boolean execute() {
		if (args.size() >= 1){
			// flag game (ゲーム名) - 選択
			Game game = plugin.getGame(args.get(0));
			if (game != null){
				// 既に選択中のゲームと別のゲームを選択した場合は管理モードを終了する
				if (GameManager.getSelectedGame(player) != null && GameManager.getSelectedGame(player) != game){
					GameManager.removeManager(player, false);
				}
				GameManager.setSelectedGame(player, game);
				Actions.message(null, player, "&aゲーム'"+game.getName()+"'を選択しました！");
			}else{
				Actions.message(null, player, "&cゲーム'"+args.get(0)+"'が見つかりません！");
				return true;
			}
		}else{
			// flagadmin game - 選択解除
			if (GameManager.getSelectedGame(player) != null){
				GameManager.setSelectedGame(player, null);
			}
			GameManager.removeManager(player, false);
			Actions.message(null, player, "&aゲームの選択を解除しました！");
		}
		return true;
	}

	@Override
	public boolean permission() {
		return sender.hasPermission("flag.admin.select");
	}
}
