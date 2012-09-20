package syam.FlagGame.Command;

import syam.FlagGame.Util.Actions;

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
		Actions.message(sender, null, "&aConfiguration reloaded!");
	}

	@Override
	public boolean permission() {
		return sender.hasPermission("flag.admin.reload");
	}
}
