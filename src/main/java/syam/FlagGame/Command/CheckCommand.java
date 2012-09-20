package syam.FlagGame.Command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.InventoryHolder;

import syam.FlagGame.Enum.GameTeam;
import syam.FlagGame.Enum.Perms;
import syam.FlagGame.Game.Game;
import syam.FlagGame.Util.Actions;

public class CheckCommand extends BaseCommand{
	public CheckCommand(){
		bePlayer = false;
		name = "check";
		argLength = 1;
		usage = "<game> <- check the setup status";
	}

	@Override
	public void execute() {
		Game game = plugin.getGame(args.get(0));
		if (game == null){
			Actions.message(null, player, "&cゲーム'"+args.get(0)+"'が見つかりません");
			return;
		}

		if (game.isStarting()){
			Actions.message(null, player, "&cゲーム'"+args.get(0)+"'は既に始まっています！");
			return;
		}

		// 設定状況をチェックする
		Actions.message(sender, null, msgPrefix+ "&aゲーム'"+args.get(0)+"'の設定をチェックします..");
		Actions.message(sender, null, "&a ===========================================");

		// flags
		String help = null;
		Boolean error = false;
		List<String> errorLoc = new ArrayList<String>();

		// ステージエリア
		if (game.getStage() == null){
			error = true;
			Actions.message(sender, null, msgPrefix+ "&6[*]&bステージエリア: &c未設定");
			if (help == null)
				help = "&6 * ステージエリアを設定してください！ *\n" +
						"&6 WorldEditでステージエリアを選択して、\n" +
						"&6 '&a/flag set stage&6'コマンドを実行してください";
		}
		else Actions.message(sender, null, msgPrefix+ "&6[*]&bステージエリア: &6設定済み");

		// チームスポーン
		if (game.getSpawns().size() != GameTeam.values().length){
			error = true;
			Actions.message(sender, null, msgPrefix+ "&6[*]&b各チームスポーン地点: &c未設定");
			if (help == null)
				help = "&6 * 各チームのスポーン地点を設定してください！ *\n" +
						"&6 スポーン地点で'&a/flag set spawn <チーム名>&6'コマンドを実行してください";
		}
		else Actions.message(sender, null, msgPrefix+ "&6[*]&b各チームスポーン地点: &6設定済み");

		// チームエリア
		if (game.getBases().size() != GameTeam.values().length){
			error = true;
			Actions.message(sender, null, msgPrefix+ "&6[*]&b各チームスポーンエリア: &c未設定");
			if (help == null)
				help = "&6 * 各チームのスポーンエリアを設定してください！ *\n" +
						"&6 WorldEditでスポーンエリアを選択して、\n" +
						"&6 '&a/flag set base <チーム名>&6'コマンドを実行してください";
		}
		else Actions.message(sender, null, msgPrefix+ "&6[*]&b各チームスポーンエリア: &6設定済み");

		// フラッグ
		if (game.getFlags().size() < 1 ){
			error = true;
			Actions.message(sender, null, msgPrefix+ "&6[*]&bフラッグ: &c未設定");
			if (help == null)
				help = "&6 * ゲームで使うフラッグを設定してください！ *\n" +
						"&6 '&a/flag set flag <フラッグ種類>&6'コマンドで管理モードになります";
		}
		else Actions.message(sender, null, msgPrefix+ "&6[*]&bフラッグ: &6"+game.getFlags().size()+"個");

		// チェスト
		if (game.getChests().size() > 0){
			// 全チェストをチェック
			for (Location loc : game.getChests()){
				Block toBlock = loc.getBlock();
				Block fromBlock = toBlock.getRelative(BlockFace.DOWN, 2);
				// インベントリインターフェースを持たないブロック
				if (!(toBlock.getState() instanceof InventoryHolder)){
					errorLoc.add("&d インベントリを持つブロックではありません: "+Actions.getBlockLocationString(toBlock.getLocation()));
					continue;
				}
				// 2ブロック下とブロックIDが違う
				if (toBlock.getTypeId() != fromBlock.getTypeId()){
					errorLoc.add("&d 2つ下と同じブロックではありません: "+Actions.getBlockLocationString(toBlock.getLocation()));
					continue;
				}
			}
			if (errorLoc.size() > 0)
				Actions.message(sender, null, msgPrefix+ "&6   &bチェスト: &c"+game.getChests().size()+"個中 エラー "+errorLoc.size()+"個");
			else
				Actions.message(sender, null, msgPrefix+ "&6   &bチェスト: &6"+game.getChests().size()+"個 OK");
		}
		else Actions.message(sender, null, msgPrefix+ "&6   &bチェスト: &6"+game.getChests().size()+"個");


		// 観戦者スポーン
		if (game.getSpecSpawn() == null)
			Actions.message(sender, null, msgPrefix+ "&6   &b観戦者スポーン地点: &c未設定");
		else
			Actions.message(sender, null, msgPrefix+ "&6   &b観戦者スポーン地点: &6設定済み");

		Actions.message(sender, null, "&a ===========================================");
		if (error) Actions.message(sender, null, "&6 設定が完了していません。[*]の設定は必須項目です");
		else Actions.message(sender, null, "&a 必須項目は正しく設定されています");

		if (help != null){
			String[] ma = help.split("\n");
			for (String m : ma)
				Actions.message(sender, null, m);
		}

		if (errorLoc.size() > 0){
			Actions.message(sender, null, "&6 チェストに以下のエラーがあります:");
			for (String m : errorLoc)
				Actions.message(sender, null, m);
		}

		Actions.message(sender, null, "&a ===========================================");
	}

	@Override
	public boolean permission() {
		return Perms.CHECK.has(sender);
	}
}
