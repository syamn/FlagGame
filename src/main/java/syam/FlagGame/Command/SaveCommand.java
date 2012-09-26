package syam.FlagGame.Command;

import syam.FlagGame.FGPlayer.PlayerManager;
import syam.FlagGame.Game.GameManager;
import syam.FlagGame.Game.StageManager;
import syam.FlagGame.Permission.Perms;
import syam.FlagGame.Util.Actions;

public class SaveCommand extends BaseCommand{
	public SaveCommand(){
		bePlayer = false;
		name = "save";
		argLength = 0;
		usage = "<- save map data";
	}

	@Override
	public void execute() {
		// データ保存
		plugin.getFileManager().saveStages();
		StageManager.saveAll();
		PlayerManager.saveAll();

		Actions.message(sender, null, "&aStages/Players Saved!");
	}

	@Override
	public boolean permission() {
		return Perms.SAVE.has(sender);
	}
}
