package syam.FlagGame.Command;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import syam.FlagGame.Enum.FlagType;
import syam.FlagGame.Enum.GameTeam;
import syam.FlagGame.Enum.Config.ConfigType;
import syam.FlagGame.Enum.Config.Configables;
import syam.FlagGame.Game.Game;
import syam.FlagGame.Game.GameManager;
import syam.FlagGame.Util.Actions;
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
	public boolean execute() {
		// flag set のみ (サブ引数なし)
		if (args.size() <= 0){
			if (GameManager.getManager(player) != null){
				GameManager.removeManager(player, false);
			}else{
				Actions.message(null, player, "&c設定項目を指定してください！");
				sendAvailableConf();
			}
			return true;
		}

		// 管理モードであれば外す
		GameManager.removeManager(player, false);

		// ゲーム取得
		Game game = GameManager.getSelectedGame(player);
		if (game == null){
			Actions.message(null, player, "&c先に編集するゲームを選択してください");
			return true;
		}

		// 開始中でないかチェック
		if (game.isReady() || game.isStarting()){
			Actions.message(sender, null, "&cこのゲームは受付中か開始中のため設定変更できません！");
			return true;
		}

		// 設定可能項目名を回す
		Configables conf = null;
		for (Configables check : Configables.values()){
			if (check.name().equalsIgnoreCase(args.get(0))){
				conf = check; break;
			}
		}
		if (conf == null){
			Actions.message(sender, null, "&cその設定項目は存在しません！");
			sendAvailableConf();
			return true;
		}

		// 設定タイプが ConfigType.SIMPLE の場合はサブ引数が2つ以上必要
		if (conf.getConfigType() == ConfigType.SIMPLE){
			if (args.size() < 2){
				Actions.message(sender, null, "&c引数が足りません！ 設定する値を入力してください！");
				return true;
			}
		}

		// 設定項目によって処理を分ける
		switch (conf){
			/* 一般 */
			case STAGE: // ステージ設定
				break;
			case BASE: // 拠点設定
				return setBase(game);
			case SPAWN: // スポーン地点設定
				return setSpawn(game);
			case FLAG: // フラッグ設定
				return setFlag(game);
			case CHEST: // チェスト設定
				return setChest(game);

			/* オプション */
			case GAMETIME: // 制限時間
				return setGameTime(game);
			case TEAMLIMIT: // チーム人数制限
				return setTeamLimit(game);
			case AWARD: // 賞金
				return setAward(game);
			case ENTRYFEE: // 参加料
				return setEntryFee(game);


			// 定義漏れ
			default:
				Actions.message(sender, null, "&c設定項目が不正です 開発者にご連絡ください");
				log.warning(logPrefix+ "Undefined configables! Please report this!");
				break;
		}

		return true;
	}

	/* ***** ここから各設定関数 ****************************** */

	// 一般
	/**
	 * 拠点エリア設定
	 * @param game
	 * @return true
	 */
	private boolean setBase(Game game){
		// 引数チェック
		if (args.size() < 2){
			Actions.message(sender, null, "&c引数が足りません！設定するチームを指定してください！");
			return true;
		}

		// チーム取得
		GameTeam team = null;
		for (GameTeam tm : GameTeam.values()){
			if (tm.name().toLowerCase().equalsIgnoreCase(args.get(0)))
				team = tm; break;
		}
		if (team == null){
			Actions.message(null, player, "&cチーム'"+args.get(0)+"'が見つかりません！");
			return true;
		}

		// WorldEdit選択領域取得
		Block[] corners = WorldEditHandler.getWorldEditRegion(player);
		// エラー プレイヤーへのメッセージ送信はWorldEditHandlerクラスで処理
		if (corners == null || corners.length != 2) return true;

		Block block1 = corners[0];
		Block block2 = corners[1];

		// ワールドチェック
		if (block1.getWorld() != Bukkit.getWorld(plugin.getConfigs().gameWorld)){
			Actions.message(null, player, "&c指定しているエリアはゲームワールドではありません！");
			return true;
		}

		// 拠点設定
		game.setBase(team, block1.getLocation(), block2.getLocation());

		Actions.message(null, player, "&a"+team.getTeamName()+"チームの拠点を設定しました！");
		return true;
	}
	/**
	 * スポーン地点設定
	 * @param game
	 * @return true
	 */
	private boolean setSpawn(Game game){
		// 引数チェック
		if (args.size() < 2){
			Actions.message(sender, null, "&c引数が足りません！設定するチームを指定してください！");
			return true;
		}

		// チーム取得
		GameTeam team = null;
		for (GameTeam tm : GameTeam.values()){
			if (tm.name().toLowerCase().equalsIgnoreCase(args.get(0)))
				team = tm; break;
		}
		if (team == null){
			Actions.message(null, player, "&cチーム'"+args.get(0)+"'が見つかりません！");
			return true;
		}

		// スポーン地点設定
		game.setSpawn(team, player.getLocation());

		Actions.message(null, player, "&a"+team.getTeamName()+"チームのスポーン地点を設定しました！");
		return true;
	}
	/**
	 * フラッグ管理モード
	 * @param game
	 * @return true
	 */
	private boolean setFlag(Game game){
		// 引数チェック
		if (args.size() < 2){
			Actions.message(sender, null, "&c引数が足りません！フラッグの種類を指定してください！");
			return true;
		}

		// フラッグタイプチェック
		FlagType type = null;
		for (FlagType ft : FlagType.values()){
			if (ft.name().equalsIgnoreCase(args.get(1)))
				type = ft;
		}
		if (type == null){
			Actions.message(null, player, "&cフラッグの種類を正しく指定してください！");
			return true;
		}

		// マネージャーセット
		GameManager.setManager(player, Configables.FLAG);
		GameManager.setSelectedFlagType(player, type);
		String tool = Material.getMaterial(plugin.getConfigs().toolID).name();
		Actions.message(null, player, "&aフラッグ管理モードを開始しました。選択ツール: " + tool);
		return true;
	}
	/**
	 * チェスト管理モード
	 * @param game
	 * @return true
	 */
	private boolean setChest(Game game){
		// マネージャーセット
		GameManager.setManager(player, Configables.CHEST);
		String tool = Material.getMaterial(plugin.getConfigs().toolID).name();
		Actions.message(null, player, "&aチェスト管理モードを開始しました。選択ツール: " + tool);

		return true;
	}

	// オプション
	private boolean setGameTime(Game game){
		int num = 60 * 10; // デフォルト10分
		try{
			num = Integer.parseInt(args.get(1));
		}catch(NumberFormatException ex){
			Actions.message(sender, null, "&cオプションの値が整数ではありません！");
			return true;
		}

		if (num <= 0){
			Actions.message(sender, null, "&c値が不正です！正数を入力してください！");
			return true;
		}
		game.setGameTime(num);

		String sec = num+"秒";
		if (num >= 60) sec = sec + "("+Actions.getTimeString(num)+")";
		Actions.message(sender, null, "&aゲーム'"+game.getName()+"'のゲーム時間は "+sec+" に設定されました！");

		return true;
	}
	private boolean setTeamLimit(Game game){
		int cnt = 8; // デフォルト8人
		try{
			cnt = Integer.parseInt(args.get(1));
		}catch(NumberFormatException ex){
			Actions.message(sender, null, "&cオプションの値が整数ではありません！");
			return true;
		}

		if (cnt <= 0){
			Actions.message(sender, null, "&c値が不正です！正数を入力してください！");
			return true;
		}

		game.setTeamLimit(cnt);

		Actions.message(sender, null, "&aゲーム'"+game.getName()+"'のチーム毎人数上限値は "+cnt+"人 に設定されました！");

		return true;
	}
	private boolean setAward(Game game){
		int award = 300; // デフォルト300コイン
		try{
			award = Integer.parseInt(args.get(1));
		}catch(NumberFormatException ex){
			Actions.message(sender, null, "&cオプションの値が整数ではありません！");
			return true;
		}
		if (award < 0){
			Actions.message(sender, null, "&c値が不正です！負数は指定できません！");
			return true;
		}

		game.setAward(award);

		Actions.message(sender, null, "&aゲーム'"+game.getName()+"'の賞金は "+award+"Coin に設定されました！");

		return true;
	}
	private boolean setEntryFee(Game game){
		int entryfee = 100; // デフォルト100コイン
		try{
			entryfee = Integer.parseInt(args.get(1));
		}catch(NumberFormatException ex){
			Actions.message(sender, null, "&cオプションの値が整数ではありません！");
			return true;
		}
		if (entryfee < 0){
			Actions.message(sender, null, "&c値が不正です！負数は指定できません！");
			return true;
		}

		game.setEntryFee(entryfee);

		Actions.message(sender, null, "&aゲーム'"+game.getName()+"'の参加料は "+entryfee+"Coin に設定されました！");

		return true;
	}

	/* ***** ここまで **************************************** */

	/**
	 * 設定可能な設定とヘルプをsenderに送信する
	 */
	private void sendAvailableConf(){
		Actions.message(sender, null, "message here");
	}

	@Override
	public boolean permission() {
		return sender.hasPermission("flag.admin");
	}
}
