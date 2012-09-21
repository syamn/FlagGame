package syam.FlagGame.Command;

import java.io.File;

import syam.FlagGame.Game.OldGame;
import syam.FlagGame.Permission.Perms;
import syam.FlagGame.Util.Actions;

public class DeleteCommand extends BaseCommand {
	public DeleteCommand(){
		bePlayer = false;
		name = "delete";
		argLength = 1;
		usage = "<name> <- delete exist game";
	}

	@Override
	public void execute() {
		if (args.size() == 0){
			Actions.message(sender, null, "&cゲーム名を入力してください！ /flag delete (name)");
			return;
		}
		OldGame game = plugin.getGame(args.get(0));
		if (game == null){
			Actions.message(sender, null, "&cそのゲーム名は存在しません！");
			return;
		}

		if (game.isReady() || game.isStarting()){
			Actions.message(sender, null, "&cそのゲームは現在受付中または開始中のため削除できません");
			return;
		}

		// ゲームのフラッグブロックロールバック
		game.rollbackFlags();

		// ゲームリストから削除
		plugin.games.remove(args.get(0));

		// ゲームデータファイルを削除
		String fileDir = plugin.getDataFolder() + System.getProperty("file.separator") + "gameData";
		boolean deleted = false;
		try{
			File file = new File(fileDir + System.getProperty("file.separator") + game.getFileName());
			if (file.exists()){
				deleted = file.delete();
			}
		}catch (Exception ex){
			deleted = false;
			ex.printStackTrace();
		}

		if (!deleted){
			Actions.message(sender, null, "&cゲーム'"+args.get(0)+"'のゲームデータファイル削除中にエラーが発生しました！");
		}else{
			Actions.message(sender, null, "&aゲーム'"+args.get(0)+"'を削除しました！");
			plugin.getDynmap().updateRegions();
		}
	}

	@Override
	public boolean permission() {
		return Perms.DELETE.has(sender);
	}
}
