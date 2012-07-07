package syam.FlagGame.Command;

import syam.FlagGame.Game.GameManager;
import syam.FlagGame.Game.GameTeam;
import syam.FlagGame.Util.Actions;

public class SelectTeamCommand extends BaseCommand {
	public SelectTeamCommand(){
		bePlayer = true;
		name = "team";
		argLength = 0;
		usage = "[name] <- select team";
	}

	@Override
	public boolean execute() {
		if (args.size() >= 1){
			// flagadmin team (チーム名) - 選択
			GameTeam team = null;
			for (GameTeam tm : GameTeam.values()){
				if (tm.name().toLowerCase().equalsIgnoreCase(args.get(0)))
				{	team = tm; break;	}
			}
			if (team != null){
				GameManager.setSelectedTeam(player, team);
				Actions.message(null, player, "&aチーム'"+team.name()+"'を選択しました！");
			}else{
				Actions.message(null, player, "&cチーム'"+args.get(0)+"'が見つかりません！");
				return true;
			}
		}else{
			// flagadmin team - 選択解除
			if (GameManager.getSelectedTeam(player) != null){
				GameManager.setSelectedTeam(player, null);
			}
			Actions.message(null, player, "&aチームの選択を解除しました！");
		}
		return true;
	}

	@Override
	public boolean permission() {
		return sender.hasPermission("flag.admin");
	}
}
