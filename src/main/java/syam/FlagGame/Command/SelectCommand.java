package syam.FlagGame.Command;

import syam.FlagGame.Enum.Config.Configables;
import syam.FlagGame.Game.GameManager;
import syam.FlagGame.Game.Stage;
import syam.FlagGame.Game.StageManager;
import syam.FlagGame.Permission.Perms;
import syam.FlagGame.Util.Actions;

public class SelectCommand extends BaseCommand {
	public SelectCommand(){
		bePlayer = true;
		name = "select";
		argLength = 0;
		usage = "[stage] <- select exist stage";
	}

	@Override
	public void execute() {
		if (args.size() >= 1){
			// flag game (ゲーム名) - 選択
			Stage stage = StageManager.getStage(args.get(0));
			if (stage != null){
				// 既に選択中のゲームと別のゲームを選択した場合は管理モードを終了する
				if (GameManager.getSelectedStage(player) != null && GameManager.getSelectedStage(player) != stage){
					GameManager.removeManager(player, false);
				}
				GameManager.setSelectedStage(player, stage);
				Actions.message(null, player, "&aステージ'"+stage.getName()+"'を選択しました！");
			}else{
				Actions.message(null, player, "&cステージ'"+args.get(0)+"'が見つかりません！");
				return;
			}
		}else{
			// flag game - 選択解除
			if (GameManager.getSelectedStage(player) != null){
				GameManager.setSelectedStage(player, null);
			}
			GameManager.removeManager(player, false);
			Actions.message(null, player, "&aステージの選択を解除しました！");
		}
	}

	@Override
	public boolean permission() {
		return Perms.SELECT.has(sender);
	}
}
