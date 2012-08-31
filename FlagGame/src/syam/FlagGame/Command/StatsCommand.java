package syam.FlagGame.Command;

import java.util.ArrayList;
import java.util.List;

import syam.FlagGame.FGPlayer.FGPlayer;
import syam.FlagGame.FGPlayer.PlayerManager;
import syam.FlagGame.FGPlayer.PlayerProfile;
import syam.FlagGame.Util.Actions;

public class StatsCommand extends BaseCommand{
	public StatsCommand(){
		bePlayer = false;
		name = "stats";
		argLength = 0;
		usage = "[player] <- show game stats";
	}

	@Override
	public boolean execute() {
		PlayerProfile prof = null;
		boolean other = false;

		// 自分の情報表示
		if (args.size() <= 0){
			// check console
			if (player == null){
				Actions.message(sender, null, "&c情報を表示するユーザ名を入力してください");
				return true;
			}

			// check permission
			if (!sender.hasPermission("flag.user.stats.self")){
				Actions.message(sender, null, "&cあなたはこのコマンドを使う権限がありません");
				return true;
			}

			prof = PlayerManager.getProfile(player.getName());
		}
		// 他人の情報表示
		else{
			other = true;

			// check permission
			if (!sender.hasPermission("flag.user.stats.other")){
				Actions.message(sender, null, "&cあなたは他人の情報を見る権限がありません");
				return true;
			}

			FGPlayer fgPlayer = PlayerManager.getPlayer(args.get(0));

			// 対象者がログイン中かどうか
			if (fgPlayer != null){
				prof = fgPlayer.getProfile();
			}
			// オフライン
			else{
				prof = new PlayerProfile(args.get(0), false);

				if (!prof.isLoaded()){
					Actions.message(sender, null, "&c指定したプレイヤーの情報が見つかりません");
					return true;
				}
			}
		}

		// check null
		if (prof == null){
			Actions.message(sender, null, "&cプレイヤー情報が正しく読み込めませんでした");
			return true;
		}

		// メッセージ送信
		for (String line : buildStrings(prof)){
			Actions.message(sender, null, line);
		}

		return true;
	}

	private List<String> buildStrings(PlayerProfile prof){
		List<String> l = new ArrayList<String>();
		l.clear();

		// ヘッダー
		l.add("&a[FlagGame] Stats");

		// 一般
		l.add("&6-= 一般 =-");
		l.add("&e    ゲーム参加: &a" + prof.getPlayed() + " 回");
		if (prof.getExit() == 0)
			l.add("&eゲーム途中退場: &a0 回");
		else
			l.add("&eゲーム途中退場: &c" + prof.getExit() + " 回");

		// 勝敗
		l.add("&6-= 勝敗 =-");
		l.add("&e Win: &a" + prof.getWin() + " 回");
		l.add("&eLose: &a" + prof.getLose() + " 回");
		l.add("&eDraw: &a" + prof.getDraw() + " 回");

		// Kill/Death
		l.add("&6-= 個人 =-");
		l.add("&e Kill数: &a" + prof.getKill() + " Kill");
		l.add("&eDeath数: &a" + prof.getDeath() + " Death");
		l.add("&e    K/D: &a" + String.format("%.3f", prof.getKD()));

		return l;
	}

	@Override
	public boolean permission() {
		if (sender.hasPermission("flag.user.stats.self") ||
			sender.hasPermission("flag.user.stats.other")){
			return true;
		}else{
			return false;
		}
	}
}
