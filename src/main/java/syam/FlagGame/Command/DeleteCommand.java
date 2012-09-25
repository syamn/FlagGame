package syam.FlagGame.Command;

import java.io.File;

import syam.FlagGame.Game.Stage;
import syam.FlagGame.Game.StageManager;
import syam.FlagGame.Permission.Perms;
import syam.FlagGame.Util.Actions;

public class DeleteCommand extends BaseCommand {
	public DeleteCommand(){
		bePlayer = false;
		name = "delete";
		argLength = 1;
		usage = "<name> <- delete exist stage";
	}

	@Override
	public void execute() {
		if (args.size() == 0){
			Actions.message(sender, null, "&cステージ名を入力してください！");
			return;
		}
		Stage stage = StageManager.getStage(args.get(0));
		if (stage == null){
			Actions.message(sender, null, "&cそのゲーム名は存在しません！");
			return;
		}

		if (stage.isUsing()){
			Actions.message(sender, null, "&cそのゲームは現在受付中または開始中のため削除できません");
			return;
		}

		// ステージロールバック
		stage.rollbackFlags();
		stage.rollbackChests();

		// ゲームリストから削除
		StageManager.stages.remove(args.get(0));

		// ゲームデータファイルを削除
		String fileDir = plugin.getDataFolder() + System.getProperty("file.separator") + "gameData"; // TODO: change here
		boolean deleted = false;
		try{
			File file = new File(fileDir + System.getProperty("file.separator") + stage.getFileName());
			if (file.exists()){
				deleted = file.delete();
			}
		}catch (Exception ex){
			deleted = false;
			ex.printStackTrace();
		}

		if (!deleted){
			Actions.message(sender, null, "&cステージ'"+args.get(0)+"'のデータファイル削除中にエラーが発生しました！");
		}else{
			Actions.message(sender, null, "&aステージ'"+args.get(0)+"'を削除しました！");
			plugin.getDynmap().updateRegions();
		}
	}

	@Override
	public boolean permission() {
		return Perms.DELETE.has(sender);
	}
}
