package syam.FlagGame.Command;

import syam.FlagGame.Game.Stage;
import syam.FlagGame.Manager.SetupManager;
import syam.FlagGame.Manager.StageManager;
import syam.FlagGame.Permission.Perms;
import syam.FlagGame.Util.Actions;

public class CreateCommand extends BaseCommand {
	public CreateCommand(){
		bePlayer = true;
		name = "create";
		argLength = 1;
		usage = "<name> <- create new stage";
	}

	@Override
	public void execute() {
		if (args.size() == 0){
			Actions.message(sender, null, "&cステージ名を入力してください！ /flag create (name)");
			return;
		}
		Stage stage = StageManager.getStage(args.get(0));
		if (stage != null){
			Actions.message(sender, null, "&cそのステージ名は既に存在します！");
			return;
		}

		// 新規ゲーム登録
		stage = new Stage(plugin, args.get(0));
		SetupManager.setSelectedStage(player, stage);

		// update dynmap
		plugin.getDynmap().updateRegions();
		// save stage
		plugin.getFileManager().saveStages();

		Actions.message(sender, null, "&a新規ステージ'"+stage.getName()+"'を登録して選択しました！");
	}

	@Override
	public boolean permission() {
		return Perms.CREATE.has(sender);
	}
}
