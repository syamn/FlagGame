package syam.flaggame.command;

import syam.flaggame.FlagGame;
import syam.flaggame.util.Actions;

public class HelpCommand extends BaseCommand {
	public HelpCommand(){
		bePlayer = false;
		name = "help";
		argLength = 0;
		usage = "<- show command help";
	}

	@Override
	public void execute() {
		Actions.message(sender, "&c===================================");
		Actions.message(sender, "&bFlagGame Plugin version &3" + plugin.getDescription().getVersion() + " &bby syamn");
		Actions.message(sender, " &b<>&f = required, &b[]&f = optional");
		// 全コマンドをループで表示
		for (BaseCommand cmd : FlagGame.getCommands().toArray(new BaseCommand[0])){
			cmd.sender = this.sender;
			if (cmd.permission()){
				Actions.message(sender, "&8-&7 /"+command+" &c" + cmd.name + " &7" + cmd.usage);
			}
		}
		Actions.message(sender, "&c===================================");
	}

	@Override
	public boolean permission() {
		return true;
	}
}
