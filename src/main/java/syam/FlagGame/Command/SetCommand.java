package syam.FlagGame.Command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import syam.FlagGame.Enum.FlagType;
import syam.FlagGame.Enum.GameTeam;
import syam.FlagGame.Enum.Config.ConfigType;
import syam.FlagGame.Enum.Config.Configables;
import syam.FlagGame.Game.GameManager;
import syam.FlagGame.Game.Stage;
import syam.FlagGame.Permission.Perms;
import syam.FlagGame.Util.Actions;
import syam.FlagGame.Util.Util;
import syam.FlagGame.Util.WorldEditHandler;

public class SetCommand extends BaseCommand {
	/*
	 * TODO: 設定によってコンソールから実行可能にする
	 * Confiable列挙にbePlayer (boolean) を追加するか、ConfigType.Area
	 */
	public SetCommand(){
		bePlayer = true;
		name = "set";
		argLength = 0;
		usage = "<option> [value] <- set option";
	}

	/**
	 * コマンド実行時に呼ばれる
	 */
	@Override
	public void execute() {
		// flag set のみ (サブ引数なし)
		if (args.size() <= 0){
			if (GameManager.getManager(player) != null){
				GameManager.removeManager(player, false);
			}else{
				Actions.message(null, player, "&c設定項目を指定してください！");
				sendAvailableConf();
			}
			return;
		}

		// 管理モードであれば外す
		GameManager.removeManager(player, false);

		// ゲーム取得
		Stage stage = GameManager.getSelectedStage(player);
		if (stage == null){
			Actions.message(null, player, "&c先に編集するゲームを選択してください");
			return;
		}

		// 開始中でないかチェック
		if (stage.isUsing()){
			Actions.message(sender, null, "&cこのゲームは受付中か開始中のため設定変更できません！");
			return;
		}

		// 設定可能項目名を回す
		Configables conf = null;
		for (Configables check : Configables.values()){
			if (check.name().equalsIgnoreCase(args.get(0))){
				conf = check;
				break;
			}
		}
		if (conf == null){
			Actions.message(sender, null, "&cその設定項目は存在しません！");
			sendAvailableConf();
			return;
		}

		// 設定タイプが ConfigType.SIMPLE の場合はサブ引数が2つ以上必要
		if (conf.getConfigType() == ConfigType.SIMPLE){
			if (args.size() < 2){
				Actions.message(sender, null, "&c引数が足りません！ 設定する値を入力してください！");
				return;
			}
		}

		// 設定項目によって処理を分ける
		switch (conf){
			/* 一般 */
			case STAGE: // ステージ設定
				setStage(stage); return;
			case BASE: // 拠点設定
				setBase(stage); return;
			case SPAWN: // スポーン地点設定
				setSpawn(stage); return;
			case FLAG: // フラッグ設定
				setFlag(stage); return;
			case CHEST: // チェスト設定
				setChest(stage); return;
			case SPECSPAWN: // 観戦者スポーン設定
				setSpecSpawn(stage); return;

			/* オプション */
			case GAMETIME: // 制限時間
				setGameTime(stage); return;
			case TEAMLIMIT: // チーム人数制限
				setTeamLimit(stage); return;
			case AWARD: // 賞金
				setAward(stage); return;
			case ENTRYFEE: // 参加料
				setEntryFee(stage); return;
			case PROTECT: // ステージ保護
				setStageProtect(stage); return;


			// 定義漏れ
			default:
				Actions.message(sender, null, "&c設定項目が不正です 開発者にご連絡ください");
				log.warning(logPrefix+ "Undefined configables! Please report this!");
				break;
		}
	}

	/* ***** ここから各設定関数 ****************************** */

	// 一般
	private void setStage(Stage game){
		// WorldEdit選択領域取得
		Block[] corners = WorldEditHandler.getWorldEditRegion(player);
		// エラー プレイヤーへのメッセージ送信はWorldEditHandlerクラスで処理
		if (corners == null || corners.length != 2) return;

		Block block1 = corners[0];
		Block block2 = corners[1];

		// ワールドチェック
		if (block1.getWorld() != Bukkit.getWorld(plugin.getConfigs().getGameWorld())){
			Actions.message(null, player, "&c指定しているエリアはゲームワールドではありません！");
			return;
		}

		// ステージ設定
		game.setStage(block1.getLocation(), block2.getLocation());

		Actions.message(null, player, "&aゲーム'"+game.getName()+"'のステージエリアを設定しました！");
		plugin.getDynmap().updateRegion(game);
	}
	/**
	 * 拠点エリア設定
	 * @param game
	 * @return true
	 */
	private void setBase(Stage game){
		// 引数チェック
		if (args.size() < 2){
			Actions.message(sender, null, "&c引数が足りません！設定するチームを指定してください！");
			return;
		}

		// チーム取得
		GameTeam team = null;
		for (GameTeam tm : GameTeam.values()){
			if (tm.name().toLowerCase().equalsIgnoreCase(args.get(1))){
				team = tm;
				break;
			}
		}
		if (team == null){
			Actions.message(null, player, "&cチーム'"+args.get(1)+"'が見つかりません！");
			return;
		}

		// WorldEdit選択領域取得
		Block[] corners = WorldEditHandler.getWorldEditRegion(player);
		// エラー プレイヤーへのメッセージ送信はWorldEditHandlerクラスで処理
		if (corners == null || corners.length != 2) return;

		Block block1 = corners[0];
		Block block2 = corners[1];

		// ワールドチェック
		if (block1.getWorld() != Bukkit.getWorld(plugin.getConfigs().getGameWorld())){
			Actions.message(null, player, "&c指定しているエリアはゲームワールドではありません！");
			return;
		}

		// 拠点設定
		game.setBase(team, block1.getLocation(), block2.getLocation());

		Actions.message(null, player, "&a"+team.getTeamName()+"チームの拠点を設定しました！");
		plugin.getDynmap().updateRegion(game);
	}
	/**
	 * スポーン地点設定
	 * @param game
	 * @return true
	 */
	private void setSpawn(Stage game){
		// 引数チェック
		if (args.size() < 2){
			Actions.message(sender, null, "&c引数が足りません！設定するチームを指定してください！");
			return;
		}

		// チーム取得
		GameTeam team = null;
		for (GameTeam tm : GameTeam.values()){
			if (tm.name().toLowerCase().equalsIgnoreCase(args.get(1))){
				team = tm;
				break;
			}
		}
		if (team == null){
			Actions.message(null, player, "&cチーム'"+args.get(1)+"'が見つかりません！");
			return;
		}

		// スポーン地点設定
		game.setSpawn(team, player.getLocation());

		Actions.message(null, player, "&a"+team.getTeamName()+"チームのスポーン地点を設定しました！");
		plugin.getDynmap().updateRegion(game);
	}
	/**
	 * フラッグ管理モード
	 * @param game
	 * @return true
	 */
	private void setFlag(Stage game){
		// 引数チェック
		if (args.size() < 2){
			Actions.message(sender, null, "&c引数が足りません！フラッグの種類を指定してください！");
			return;
		}

		// フラッグタイプチェック
		FlagType type = null;
		for (FlagType ft : FlagType.values()){
			if (ft.name().equalsIgnoreCase(args.get(1)))
				type = ft;
		}
		if (type == null){
			Actions.message(null, player, "&cフラッグの種類を正しく指定してください！");
			return;
		}

		// マネージャーセット
		GameManager.setManager(player, Configables.FLAG);
		GameManager.setSelectedFlagType(player, type);
		String tool = Material.getMaterial(plugin.getConfigs().getToolID()).name();
		Actions.message(null, player, "&aフラッグ管理モードを開始しました。選択ツール: " + tool);
	}
	/**
	 * チェスト管理モード
	 * @param game
	 * @return true
	 */
	private void setChest(Stage game){
		// マネージャーセット
		GameManager.setManager(player, Configables.CHEST);
		String tool = Material.getMaterial(plugin.getConfigs().getToolID()).name();
		Actions.message(null, player, "&aチェスト管理モードを開始しました。選択ツール: " + tool);
	}
	/**
	 * 観戦者スポーン地点
	 * @param game 設定対象のゲームイン寸タンス
	 * @return
	 */
	private void setSpecSpawn(Stage game){
		// 観戦者スポーン地点設定
		game.setSpecSpawn(player.getLocation());

		Actions.message(null, player, "&aゲーム'"+game.getName()+"'の観戦者スポーン地点を設定しました！");
		plugin.getDynmap().updateRegion(game);
	}

	// オプション
	private void setGameTime(Stage game){
		int num = 60 * 10; // デフォルト10分
		try{
			num = Integer.parseInt(args.get(1));
		}catch(NumberFormatException ex){
			Actions.message(sender, null, "&cオプションの値が整数ではありません！");
			return;
		}

		if (num <= 0){
			Actions.message(sender, null, "&c値が不正です！正数を入力してください！");
			return;
		}
		game.setGameTime(num);

		String sec = num+"秒";
		if (num >= 60) sec = sec + "("+Actions.getTimeString(num)+")";
		Actions.message(sender, null, "&aゲーム'"+game.getName()+"'のゲーム時間は "+sec+" に設定されました！");
	}
	private void setTeamLimit(Stage game){
		int cnt = 8; // デフォルト8人
		try{
			cnt = Integer.parseInt(args.get(1));
		}catch(NumberFormatException ex){
			Actions.message(sender, null, "&cオプションの値が整数ではありません！");
			return;
		}

		if (cnt <= 0){
			Actions.message(sender, null, "&c値が不正です！正数を入力してください！");
			return;
		}

		game.setTeamLimit(cnt);

		Actions.message(sender, null, "&aゲーム'"+game.getName()+"'のチーム毎人数上限値は "+cnt+"人 に設定されました！");
		plugin.getDynmap().updateRegion(game);
	}
	private void setAward(Stage game){
		int award = 300; // デフォルト300コイン
		try{
			award = Integer.parseInt(args.get(1));
		}catch(NumberFormatException ex){
			Actions.message(sender, null, "&cオプションの値が整数ではありません！");
			return;
		}
		if (award < 0){
			Actions.message(sender, null, "&c値が不正です！負数は指定できません！");
			return;
		}

		game.setAward(award);

		Actions.message(sender, null, "&aゲーム'"+game.getName()+"'の賞金は "+award+"Coin に設定されました！");
		plugin.getDynmap().updateRegion(game);
	}
	private void setEntryFee(Stage game){
		int entryfee = 100; // デフォルト100コイン
		try{
			entryfee = Integer.parseInt(args.get(1));
		}catch(NumberFormatException ex){
			Actions.message(sender, null, "&cオプションの値が整数ではありません！");
			return;
		}
		if (entryfee < 0){
			Actions.message(sender, null, "&c値が不正です！負数は指定できません！");
			return;
		}

		game.setEntryFee(entryfee);
		Actions.message(sender, null, "&aゲーム'"+game.getName()+"'の参加料は "+entryfee+"Coin に設定されました！");
		plugin.getDynmap().updateRegion(game);
	}
	private void setStageProtect(Stage game){
		Boolean protect = true; // デフォルトtrue
		String value = args.get(1).trim();

		if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes")){
			protect = true;
		}else if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("no")){
			protect = false;
		}else{
			Actions.message(sender, null, "&c値が不正です！true または false を指定してください！");
			return;
		}

		String result = "";
		if (protect) result = "&a有効";
		else result = "&c無効";

		game.setStageProtected(protect);
		Actions.message(sender, null, "&aゲーム'"+game.getName()+"'のステージ保護は "+result+" &aに設定されました！");
		plugin.getDynmap().updateRegion(game);
	}

	/* ***** ここまで **************************************** */

	/**
	 * 設定可能な設定とヘルプをsenderに送信する
	 */
	private void sendAvailableConf(){
		List<String> col = new ArrayList<String>();
		for (Configables conf : Configables.values()){
			col.add(conf.name());
		}

		Actions.message(sender, null, "&6 " + Util.join(col, "/").toLowerCase());
		//Actions.message(sender, null, "&6 stage / base / spawn / flag / chest / gametime / teamlimit / award / entryfee / protect");
	}

	@Override
	public boolean permission() {
		return Perms.SET.has(sender);
	}
}
