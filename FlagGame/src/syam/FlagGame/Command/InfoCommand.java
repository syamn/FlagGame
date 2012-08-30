package syam.FlagGame.Command;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import syam.FlagGame.Enum.GameTeam;
import syam.FlagGame.FGPlayer.PlayerFile;
import syam.FlagGame.Game.Game;
import syam.FlagGame.Util.Actions;

public class InfoCommand extends BaseCommand {
	public InfoCommand(){
		bePlayer = false;
		name = "info";
		argLength = 0;
		usage = "[game] <- show game info";
	}

	@Override
	public boolean execute() {
		// 引数が無ければすべてのゲームデータを表示する
		if (args.size() == 0){
			int gamecount = plugin.games.size();

			Actions.message(sender, null, "&a ===============&b GameList("+gamecount+") &a===============");
			if (gamecount == 0){
				Actions.message(sender, null, " &7読み込まれているゲームデータがありません");
			}else{
				for (Game game : plugin.games.values()){
					// ゲームステータス取得
					String status = "&7待機中";
					if (game.isStarting()){
						// 開始中なら残り時間も表示
						String time = Actions.getTimeString(game.getRemainTime());
						status = "&c開始中&7(あと:"+time+")";
					}
					else if (game.isReady()) status = "&6受付中";

					String s = "&6"+game.getName()+"&b: 状態=&f"+status+"&b 制限時間=&6"+Actions.getTimeString(game.getGameTime())+"&b フラッグ数=&6"+game.getFlags().size();

					// メッセージ送信
					Actions.message(sender, null, s);
				}
			}
			Actions.message(sender, null, "&a ===========================================");
			return true;
		}

		// 引数があれば指定したゲームについての詳細情報を表示する
		else {
			Game game = plugin.getGame(args.get(0));
			if (game == null){
				Actions.message(sender, null, "&cそのゲーム名は存在しません！");
				return true;
			}

			Actions.message(sender, null, "&a ==================&b GameDetail &a==================");

			// ゲームステータス取得
			String status = "&7待機中";
			if (game.isStarting()){
				// 開始中なら残り時間も表示
				String time = Actions.getTimeString(game.getRemainTime());
				status = "&c開始中&7(あと:"+time+")";
			}

			String chksp_red = "&c未設定";
			String chksp_blue = "&c未設定";
			if (game.getSpawnLocation(GameTeam.RED) != null) chksp_red = "&6設定済";
			if (game.getSpawnLocation(GameTeam.BLUE) != null) chksp_blue = "&6設定済";

			// プレイヤーリスト構築
			String players = ""; int cnt_players = 0;
			for (Entry<GameTeam, ConcurrentHashMap<String, PlayerFile>> entry : game.getPlayersMap().entrySet()){
				String color = entry.getKey().getColor();
				for (String name : entry.getValue().keySet()){
					players = players + color + name + "&f, ";
					cnt_players++;
				}
			}
			if (players != "") players = players.substring(0, players.length() - 2);
			else players = "&7参加プレイヤーなし";

			String s1 = "&6 "+game.getName()+"&7("+game.getFileName()+")"+"&b: 状態=&f"+status+"&b 制限時間=&6"+Actions.getTimeString(game.getGameTime())+"&b フラッグ数=&6"+game.getFlags().size();
			String s2 = "&b 参加料=&6"+game.getEntryFee()+"&b 賞金=&6"+game.getAward()+"&b チェスト数=&6"+game.getChests().size();
			String s3 = "&b チーム毎人数制限=&6"+game.getTeamLimit()+"&b 赤チームスポーン="+chksp_red+"&b 青チームスポーン="+chksp_blue;
			String s4 = "&b プレイヤーリスト&7("+cnt_players+"人)&b: "+players;

			// メッセージ送信
			Actions.message(sender, null, s1);
			Actions.message(sender, null, s2);
			Actions.message(sender, null, s3);
			Actions.message(sender, null, s4);

			Actions.message(sender, null, "&a ================================================");
		}
		return true;
	}

	@Override
	public boolean permission() {
		return sender.hasPermission("flag.user.info");
	}
}
