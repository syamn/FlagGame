package syam.FlagGame.Command;

import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import syam.FlagGame.Actions;
import syam.FlagGame.FlagGame;
import syam.FlagGame.Game.Flag;
import syam.FlagGame.Game.FlagType;
import syam.FlagGame.Game.Game;
import syam.FlagGame.Game.GameManager;
import syam.FlagGame.Game.GameTeam;

public class AdminCommand implements CommandExecutor{
	// Logger
	public static final Logger log = FlagGame.log;
	private static final String logPrefix = FlagGame.logPrefix;
	private static final String msgPrefix = FlagGame.msgPrefix;

	private final FlagGame plugin;
	public AdminCommand(final FlagGame plugin){
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
		// flagadmin reload - 設定再読み込み
		if (args.length >= 1 && args[0].equalsIgnoreCase("reload")){
			// 権限チェック
			if (!sender.hasPermission("flag.admin.reload")){
				Actions.message(sender, null, "&cYou don't have permission to use this!");
				return true;
			}
			try{
				plugin.getConfigs().loadConfig(false);
			}catch (Exception ex){
				log.warning(logPrefix+"an error occured while trying to load the config file.");
				ex.printStackTrace();
				return true;
			}
			Actions.message(sender, null, "&aConfiguration reloaded!");
			return true;
		}

		/* ここから下に進むには権限が必要 */
		if(!sender.hasPermission("flag.admin")){
			Actions.message(sender, null, "&cYou don't have permission to use this!");
			return true;
		}
		/* ### Checked Permission "flag.admin" ### */

		/* 選択コマンド */
		// flagadmin game - ゲームを選択/解除
		if (args.length >= 1 && args[0].equalsIgnoreCase("game")){
			if (!(sender instanceof Player)){
				Actions.message(sender, null, "&cThis command cannot run from Console!");
				return true;
			}
			Player player = (Player)sender;
			if (args.length >= 2){
				// flagadmin game (ゲーム名) - 選択
				Game game = plugin.getGame(args[1]);
				if (game != null){
					GameManager.setSelectedGame(player, game);
					Actions.message(null, player, "&aゲーム'"+game.getName()+"'を選択しました！");
				}else{
					Actions.message(null, player, "&cゲーム'"+args[1]+"'が見つかりません！");
					return true;
				}
			}else{
				// flagadmin game - 選択解除
				if (GameManager.getSelectedGame(player) != null){
					GameManager.setSelectedGame(player, null);
				}
				Actions.message(null, player, "&aゲームの選択を解除しました！");
			}
			return true;
		}

		// flagadmin team - チームを選択/解除
		if (args.length >= 1 && args[0].equalsIgnoreCase("team")){
			if (!(sender instanceof Player)){
				Actions.message(sender, null, "&cThis command cannot run from Console!");
				return true;
			}
			Player player = (Player)sender;
			if (args.length >= 2){
				// flagadmin team (チーム名) - 選択
				GameTeam team = null;
				for (GameTeam tm : GameTeam.values()){
					if (tm.name().toLowerCase().equalsIgnoreCase(args[1]))
					{	team = tm; break;	}
				}
				if (team != null){
					GameManager.setSelectedTeam(player, team);
					Actions.message(null, player, "&aチーム'"+team.name()+"'を選択しました！");
				}else{
					Actions.message(null, player, "&cチーム'"+args[1]+"'が見つかりません！");
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

		/* #選択コマンド */

		// flagadmin new - 新規ゲームを作成
		if (args.length >= 1 && args[0].equalsIgnoreCase("new")){
			if (args.length != 2){
				Actions.message(sender, null, "&cゲーム名を入力してください！ /fga new (name)");
				return true;
			}
			Game game = plugin.getGame(args[1]);
			if (game != null){
				Actions.message(sender, null, "&cそのゲーム名は既に存在します！");
				return true;
			}

			// 新規ゲーム登録
			game = new Game(plugin, args[1]);

			Actions.message(sender, null, "&a新規ゲーム'"+game.getName()+"'を登録しました！");
			return true;
		}

		// flagadmin setflag - ゲームのフラッグを設定/解除
		if (args.length >= 1 && args[0].equalsIgnoreCase("setflag")){
			if (!(sender instanceof Player)){
				Actions.message(sender, null, "&cThis command cannot run from Console!");
				return true;
			}
			Player player = (Player)sender;

			// 引数が一つの場合はフラッグ管理モードの切り替えを行う
			if (args.length == 1){
				if (GameManager.isManager(player)){
					// フラッグ管理モード終了
					GameManager.setManager(player, false);
					GameManager.setSelectedBlock(player, null);
					Actions.message(null, player, "&aフラッグ管理モードを終了しました。");
				}else{
					// フラッグ管理モード開始
					GameManager.setManager(player, true);
					String tool = Material.getMaterial(plugin.getConfigs().toolID).name();
					Actions.message(null, player, "&aフラッグ管理モードを開始しました。選択ツール: " + tool);
				}
				return true;
			}else{
				// それ以上の場合は指定済みブロックをフラッグにする
				// fa setflag (flag-type)
				Game game = GameManager.getSelectedGame(player);
				Location loc = GameManager.getSelectedBlock(player);
				FlagType type = null;

				if (game == null){
					Actions.message(null, player, "&c先に編集するゲームを選択してください");
					return true;
				}
				if (loc == null){
					Actions.message(null, player, "&c先に編集するブロックを編集アイテムで右クリックしてください");
					return true;
				}

				for (FlagType ft : FlagType.values()){
					if (ft.name().toLowerCase().equalsIgnoreCase(args[1]))
						type = ft;
				}
				if (type == null){
					Actions.message(null, player, "&cフラッグの種類を正しく指定してください！");
					return true;
				}

				new Flag(plugin, game, loc, type);

				Actions.message(null, player, "&aゲーム'"+game.getName()+"'の"+type.getTypeName()+"フラッグを登録しました！");
				return true;
			}
		}

		// コマンドヘルプを表示
		Actions.message(sender, null, "&c===================================");
		Actions.message(sender, null, "&bFlagGame Plugin version &3%version &bby syamn");
		Actions.message(sender, null, "&b -- Administration Commands!");
		Actions.message(sender, null, " &b<>&f = required, &b[]&f = optional");
		Actions.message(sender, null, " &7/flagadmin game (name) - &fSelect Game!");
		Actions.message(sender, null, " &7/flagadmin team (name) - &fSelect Team!");
		Actions.message(sender, null, " &7/flagadmin setflag - &fSet/unset Flags!");
		Actions.message(sender, null, " &7/flagadmin reload - &fReloading config.yml!");
		Actions.message(sender, null, "&c===================================");

		return true;
	}
}
