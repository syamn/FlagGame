package syam.FlagGame.Command;

import org.bukkit.Location;

import syam.FlagGame.Enum.GameTeam;
import syam.FlagGame.Game.OldGame;
import syam.FlagGame.Game.GameManager;
import syam.FlagGame.Permission.Perms;
import syam.FlagGame.Util.Actions;

public class TpCommand extends BaseCommand{
	public TpCommand(){
		bePlayer = true;
		name = "tp";
		argLength = 1;
		usage = "<Area> [Team] [Game] <- tp to specific location";
	}

	@Override
	public void execute() {
		if (args.get(0).equalsIgnoreCase("spawn")){
			if (args.size() < 2){
				Actions.message(null, player, "&c引数が足りません！");
				return;
			}
			// ゲーム取得
			OldGame game = null;
			// 引数からゲーム取得
			if (args.size() >= 3)
				game = plugin.getGame(args.get(2));

			// 取れなかった場合選択済みゲームを取得
			if (game == null)
				game = GameManager.getSelectedGame(player);

			// それも無ければエラーを返す
			if (game == null){
				Actions.message(null, player, "&c先にゲームを選択してください");
				return;
			}

			// チーム取得
			GameTeam team = null;
			for (GameTeam tm : GameTeam.values()){
				if (tm.name().toLowerCase().equalsIgnoreCase(args.get(1)))
				{	team = tm; break;	}
			}
			if (team == null){
				Actions.message(null, player, "&cチーム'"+args.get(1)+"'が見つかりません！");
				return;
			}

			Location loc = game.getSpawnLocation(team);

			if (loc == null){
				Actions.message(null, player, "&c"+team.getTeamName()+"チームのスポーン地点は未設定です！");
				return;
			}

			// テレポート
			player.teleport(loc);
			Actions.message(null, player, "&a"+team.getTeamName()+"チームのスポーン地点にテレポートしました！");
			return;
		}

		Actions.message(null, player, "&cそのエリアは未定義です");
		return;
	}

	@Override
	public boolean permission() {
		return Perms.TP.has(sender);
	}
}
