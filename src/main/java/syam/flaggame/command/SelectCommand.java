package syam.flaggame.command;

import syam.flaggame.game.Stage;
import syam.flaggame.manager.SetupManager;
import syam.flaggame.manager.StageManager;
import syam.flaggame.permission.Perms;
import syam.flaggame.util.Actions;
import syam.flaggame.util.Cuboid;
import syam.flaggame.util.WorldEditHandler;

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
			// flag select (ステージ名) - 選択
			Stage stage = StageManager.getStage(args.get(0));
			if (stage != null){
				// 既に選択中のステージと別のゲームを選択した場合は管理モードを終了する
				if (SetupManager.getSelectedStage(player) != null && SetupManager.getSelectedStage(player) != stage){
					SetupManager.removeManager(player, false);
				}
				SetupManager.setSelectedStage(player, stage);

				String msg = "&aステージ'&6"+stage.getName()+"&a'を選択しました！";
				if (selectRegion(stage)){
					Actions.message(null, player, msg + "(+WorldEdit)");
				}else{
					Actions.message(null, player, msg);
				}
			}else{
				Actions.message(null, player, "&cステージ'"+args.get(0)+"'が見つかりません！");
				return;
			}
		}else{
			// flag select - 選択解除
			if (SetupManager.getSelectedStage(player) != null){
				SetupManager.setSelectedStage(player, null);
			}
			SetupManager.removeManager(player, false);
			Actions.message(null, player, "&aステージの選択を解除しました！");
		}
	}

	private boolean selectRegion(final Stage stage){
		if (!stage.hasStage() || !WorldEditHandler.isAvailable()){
			return false;
		}

		Cuboid stageArea = stage.getStage();
		return WorldEditHandler.selectWorldEditRegion(player, stageArea.getPos1(), stageArea.getPos2());
	}

	@Override
	public boolean permission() {
		return Perms.SELECT.has(sender);
	}
}
