package syam.flaggame.command;

import org.bukkit.Location;

import syam.flaggame.enums.GameTeam;
import syam.flaggame.game.Stage;
import syam.flaggame.manager.SetupManager;
import syam.flaggame.manager.StageManager;
import syam.flaggame.permission.Perms;
import syam.flaggame.util.Actions;

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
			Stage stage = null;
			// 引数からゲーム取得
			if (args.size() >= 3)
				stage = StageManager.getStage(args.get(2));

			// 取れなかった場合選択済みゲームを取得
			if (stage == null)
				stage = SetupManager.getSelectedStage(player);

			// それも無ければエラーを返す
			if (stage == null){
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

			Location loc = stage.getSpawn(team);

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