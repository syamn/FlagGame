/**
 * FlagGame - Package: syam.FlagGame.Command
 * Created: 2012/09/01 23:47:14
 */
package syam.FlagGame.Command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.command.CommandSender;

import syam.FlagGame.FlagGame;
import syam.FlagGame.Database.Database;
import syam.FlagGame.Enum.PlayerStat;
import syam.FlagGame.Permission.Perms;
import syam.FlagGame.Util.Actions;
import syam.FlagGame.Util.Util;

/**
 * TopCommand (TopCommand.java)
 * @author syam
 */
public class TopCommand extends BaseCommand{
	public TopCommand(){
		bePlayer = false;
		name = "top";
		argLength = 0;
		usage = "[category] [page] <- show player ranking";
	}

	@Override
	public void execute() {
		PlayerStat stat = PlayerStat.WIN; // デフォルト表示対象 チーム勝利回数

		// 引数なし デフォルト表示
		if (args.size() <= 0){
			sendRanking(sender, stat, 1);
			return;
		}
		// 引数1 ページ番号 or ランキング種類指定表示
		else if (args.size() == 1){
			if (Util.isInteger(args.get(0))){
				sendRanking(sender, stat, Integer.valueOf(args.get(0)));
				return;
			}

			stat = PlayerStat.getStat(args.get(0));
			if (stat != null){
				sendRanking(sender, PlayerStat.getStat(args.get(0)), 1);
			}
			else{
				Actions.message(sender, null, "&c表示するランキングの種類が不正です！");
				sendAvailableStat();
			}

			return;
		}
		// 引数2+ ページ番号 and ランキング種類指定表示
		else{
			stat = PlayerStat.getStat(args.get(0));
			if (stat == null){
				Actions.message(sender, null, "&c表示するランキングの種類が不正です！");
				sendAvailableStat();
				return;
			}

			if (Util.isInteger(args.get(1))){
				sendRanking(sender, stat, Integer.valueOf(args.get(1)));
			}else{
				sendRanking(sender, stat, 1);
			}
		}
	}

	/**
	 * 指定した引数でデータベースからランキングを構築して送信する
	 * @param sender 送信するCommandSender
	 * @param stat ランキングを抽出する際の種類
	 * @param page 表示するページ番号
	 */
	private void sendRanking(CommandSender sender, PlayerStat stat, int page){
		Database database = FlagGame.getDatabases();
		String tablePrefix = FlagGame.getInstance().getConfigs().getMySQLtablePrefix();
		String suffix = stat.getSuffix();

		HashMap<Integer, ArrayList<String>> rankersList = database.read("SELECT `" + stat.getColumnName() + "`, player_id FROM " + tablePrefix + "records WHERE `" + stat.getColumnName() + "` > 0 ORDER BY `" + stat.getColumnName() + "` DESC");

		// ヘッダ送信
		Actions.message(sender, null, "&e-- FlagGame &a" + stat.getDescription() + " &e順位表 --");

		// ランキング送信
		boolean isFirstRow = true;
		for (int i = (page * 10) - 9; i <= (page * 10); i++){
			// ランキングの終末
			if (i > rankersList.size()){
				if (isFirstRow){
					Actions.message(sender, null, "&7表示するプレイヤーがいません");
				}
				break;
			}

			// ユーザ名取得
			HashMap<Integer, ArrayList<String>> rankerName = database.read("SELECT player_name FROM " + tablePrefix + "users WHERE player_id = '" + Integer.valueOf(rankersList.get(i).get(1)) + "'");
			if (rankerName == null ){
				break;
			}

			// 送信
			Actions.message(sender, null, i + ". &a" + rankersList.get(i).get(0) + " " + suffix + "&7 - &f" + rankerName.get(1).get(0));
			isFirstRow = false;
		}
	}

	/**
	 * 指定可能なランキング種類をsenderに送信する
	 */
	private void sendAvailableStat(){
		List<String> col = new ArrayList<String>();
		for (PlayerStat ps : PlayerStat.values()){
			col.add(ps.name());
		}

		Actions.message(sender, null, "&6 " + Util.join(col, "/").toLowerCase());
	}

	@Override
	public boolean permission() {
		return Perms.TOP.has(sender);
	}
}
