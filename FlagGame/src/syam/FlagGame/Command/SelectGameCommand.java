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
				if (GameManager.getSelectedGame(player) != null){
					if (GameManager.getSelectedGame(player) != game){
						if (GameManager.isFlagManager(player)){
							GameManager.setFlagManager(player, false);
							Actions.message(null, player, "&aフラッグ管理モードを終了しました！");
						}
						if (GameManager.isChestManager(player)){
							GameManager.setChestManager(player, false);
							Actions.message(null, player, "&aチェスト管理モードを終了しました！");
						}
					}
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
			if (GameManager.isFlagManager(player)){
				GameManager.setFlagManager(player, false);
				Actions.message(null, player, "&aフラッグ管理モードを終了しました！");
			}
			if (GameManager.isChestManager(player)){
				GameManager.setChestManager(player, false);
				Actions.message(null, player, "&aチェスト管理モードを終了しました！");
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
