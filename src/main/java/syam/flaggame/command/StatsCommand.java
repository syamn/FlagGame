package syam.flaggame.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import syam.flaggame.permission.Perms;
import syam.flaggame.player.FGPlayer;
import syam.flaggame.player.PlayerManager;
import syam.flaggame.player.PlayerProfile;
import syam.flaggame.util.Actions;

public class StatsCommand extends BaseCommand{
	public StatsCommand(){
		bePlayer = false;
		name = "stats";
		argLength = 0;
		usage = "[player] <- show game stats";
	}

	@Override
	public void execute() {
		PlayerProfile prof = null;
		boolean other = false;

		// 自分の情報表示
		if (args.size() <= 0){
			// check console
			if (!(sender instanceof Player)){
				Actions.message(sender, null, "&c情報を表示するユーザ名を入力してください");
				return;
			}

			// check permission
			if (!Perms.STATS_SELF.has(sender)){
				Actions.message(sender, null, "&cあなたはこのコマンドを使う権限がありません");
				return;
			}

			prof = PlayerManager.getProfile(player.getName());
		}
		// 他人の情報表示
		else{
			other = true;

			// check permission
			if (!Perms.STATS_OTHER.has(sender)){
				Actions.message(sender, null, "&cあなたは他人の情報を見る権限がありません");
				return;
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
					return;
				}
			}
		}

		// check null
		if (prof == null){
			Actions.message(sender, null, "&cプレイヤー情報が正しく読み込めませんでした");
			return;
		}

		// メッセージ送信
		for (String line : buildStrings(prof, other)){
			Actions.message(sender, null, line);
		}
	}

	private List<String> buildStrings(PlayerProfile prof, boolean other){
		List<String> l = new ArrayList<String>();
		l.clear();

		// ヘッダー
		l.add("&a[FlagGame] プレイヤー情報");
		if (other)
			l.add("&aプレイヤー: &6" + prof.getPlayerName());

		// 一般 *************************************************
		l.add("&6-=== 一般 ===-");
		l.add("&eゲーム参加: &a" + prof.getPlayed() + " 回");
		if (prof.getExit() == 0)
			l.add("&e  途中退場: &a0 回");
		else
			l.add("&e  途中退場: &c" + prof.getExit() + " 回");

		// 結果 *************************************************
		l.add("&6-=== ゲーム勝敗 ===-");
		l.add("&e Win: &a" + prof.getWin() + " 回");
		l.add("&eLose: &a" + prof.getLose() + " 回");
		l.add("&eDraw: &a" + prof.getDraw() + " 回");

		// フラッグ *************************************************
		l.add("&6-=== フラッグ ===-");
		l.add("&e 設置: &a" + prof.getPlace() + " フラッグ");
		l.add("&e 破壊: &a" + prof.getBreak() + " フラッグ");

		// 戦闘 *************************************************
		l.add("&6-=== 戦闘 ===-");
		l.add("&e Kill: &a" + prof.getKill() + " 回");
		l.add("&eDeath: &a" + prof.getDeath() + " 回");
		l.add("&e  K/D: &a" + prof.getKDstring()); // kd

		return l;
	}

	@Override
	public boolean permission() {
		return (Perms.STATS_SELF.has(sender) ||
				Perms.STATS_OTHER.has(sender));
	}
}
