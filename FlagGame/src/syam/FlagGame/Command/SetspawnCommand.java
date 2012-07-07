package syam.FlagGame.Command;

import org.bukkit.Location;

import syam.FlagGame.Game.Game;
import syam.FlagGame.Game.GameManager;
import syam.FlagGame.Game.GameTeam;
import syam.FlagGame.Util.Actions;

public class SetspawnCommand extends BaseCommand{
	public SetspawnCommand(){
		bePlayer = true;
		name = "setspawn";
		argLength = 1;
		usage = "[team] <- set team spawn";
	}

	@Override
	public boolean execute() {
		// ゲーム取得
		Game game = GameManager.getSelectedGame(player);
		if (game == null){
			Actions.message(null, player, "&c先に編集するゲームを選択してください");
			return true;
		}

		// チーム取得
		GameTeam team = null;
		for (GameTeam tm : GameTeam.values()){
			if (tm.name().toLowerCase().equalsIgnoreCase(args.get(0)))
			{	team = tm; break;	}
		}
		if (team == null){
			Actions.message(null, player, "&cチーム'"+args.get(0)+"'が見つかりません！");
			return true;
		}

		// スポーン地点設定
		game.setSpawn(team, player.getLocation());

		Actions.message(null, player, "&a"+team.getTeamName()+"チームのスポーン地点を設定しました！");
		return true;
	}

	@Override
	public boolean permission() {
		return sender.hasPermission("flag.admin");
	}
}
