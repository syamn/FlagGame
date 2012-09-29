package syam.flaggame.command;

import syam.flaggame.permission.Perms;
import syam.flaggame.util.Actions;

public class ReloadCommand extends BaseCommand {
	public ReloadCommand(){
		bePlayer = false;
		name = "reload";
		argLength = 0;
		usage = "<- reload config.yml";
	}

	@Override
	public void execute() {
		try{
			plugin.getConfigs().loadConfig(false);
		}catch (Exception ex){
			log.warning(logPrefix+"an error occured while trying to load the config file.");
			ex.printStackTrace();
			return;
		}

		// 権限管理プラグイン再設定
		Perms.setupPermissionHandler();

		Actions.message(sender, null, "&aConfiguration reloaded!");
	}

	@Override
	public boolean permission() {
		return Perms.RELOAD.has(sender);
	}
}
