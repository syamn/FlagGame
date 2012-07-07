package syam.FlagGame.Command;

import syam.FlagGame.Game.Game;
import syam.FlagGame.Game.GameManager;
import syam.FlagGame.Util.Actions;

public class SelectGameCommand extends BaseCommand {
	public SelectGameCommand(){
		bePlayer = true;
		name = "game";
		argLength = 0;
		usage = "[name] <- select exist game";
	}

	@Override
	public boolean execute() {
		if (args.size() >= 1){
			// flag game (ゲーム名) - 選択
			Game game = plugin.getGame(args.get(0));
			if (game != null){
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
			Actions.message(null, player, "&aゲームの選択を解除しました！");
		}
		return true;
	}

	@Override
	public boolean permission() {
		return sender.hasPermission("flag.admin");
	}
}
