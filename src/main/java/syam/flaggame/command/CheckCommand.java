package syam.flaggame.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.InventoryHolder;

import syam.flaggame.enums.GameTeam;
import syam.flaggame.game.Stage;
import syam.flaggame.manager.StageManager;
import syam.flaggame.permission.Perms;
import syam.flaggame.util.Actions;

public class CheckCommand extends BaseCommand{
	public CheckCommand(){
		bePlayer = false;
		name = "check";
		argLength = 1;
		usage = "<stage> <- check the setup status";
	}

	@Override
	public void execute() {
		Stage stage = StageManager.getStage(args.get(0));
		if (stage == null){
			Actions.message(null, player, "&cステージ'"+args.get(0)+"'が見つかりません");
			return;
		}

		if (stage.isUsing()){
			Actions.message(null, player, "&cステージ'"+args.get(0)+"'は既に使われています！");
			return;
		}

		// 設定状況をチェックする
		Actions.message(sender, null, msgPrefix+ "&aステージ'"+args.get(0)+"'の設定をチェックします..");
		Actions.message(sender, null, "&a ===========================================");

		// flags
		String help = null;
		Boolean error = false;
		List<String> errorLoc = new ArrayList<String>();

		// ステージエリア
		if (stage.getStage() == null){
			error = true;
			Actions.message(sender, null, msgPrefix+ "&6[*]&bステージエリア: &c未設定");
			if (help == null)
				help = "&6 * ステージエリアを設定してください！ *\n" +
						"&6 WorldEditでステージエリアを選択して、\n" +
						"&6 '&a/flag set stage&6'コマンドを実行してください";
		}
		else Actions.message(sender, null, msgPrefix+ "&6[*]&bステージエリア: &6設定済み");

		// チームスポーン
		if (stage.getSpawns().size() != GameTeam.values().length){
			error = true;
			Actions.message(sender, null, msgPrefix+ "&6[*]&b各チームスポーン地点: &c未設定");
			if (help == null)
				help = "&6 * 各チームのスポーン地点を設定してください！ *\n" +
						"&6 スポーン地点で'&a/flag set spawn <チーム名>&6'コマンドを実行してください";
		}
		else Actions.message(sender, null, msgPrefix+ "&6[*]&b各チームスポーン地点: &6設定済み");

		// チームエリア
		if (stage.getBases().size() != GameTeam.values().length){
			error = true;
			Actions.message(sender, null, msgPrefix+ "&6[*]&b各チームスポーンエリア: &c未設定");
			if (help == null)
				help = "&6 * 各チームのスポーンエリアを設定してください！ *\n" +
						"&6 WorldEditでスポーンエリアを選択して、\n" +
						"&6 '&a/flag set base <チーム名>&6'コマンドを実行してください";
		}
		else Actions.message(sender, null, msgPrefix+ "&6[*]&b各チームスポーンエリア: &6設定済み");

		// フラッグ
		if (stage.getFlags().size() < 1 ){
			error = true;
			Actions.message(sender, null, msgPrefix+ "&6[*]&bフラッグ: &c未設定");
			if (help == null)
				help = "&6 * ゲームで使うフラッグを設定してください！ *\n" +
						"&6 '&a/flag set flag <フラッグ種類>&6'コマンドで管理モードになります";
		}
		else Actions.message(sender, null, msgPrefix+ "&6[*]&bフラッグ: &6"+stage.getFlags().size()+"個");

		// チェスト
		if (stage.getChests().size() > 0){
			// 全チェストをチェック
			for (Location loc : stage.getChests()){
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
				Actions.message(sender, null, msgPrefix+ "&6   &bチェスト: &c"+stage.getChests().size()+"個中 エラー "+errorLoc.size()+"個");
			else
				Actions.message(sender, null, msgPrefix+ "&6   &bチェスト: &6"+stage.getChests().size()+"個 OK");
		}
		else Actions.message(sender, null, msgPrefix+ "&6   &bチェスト: &6"+stage.getChests().size()+"個");


		// 観戦者スポーン
		if (stage.getSpecSpawn() == null)
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
