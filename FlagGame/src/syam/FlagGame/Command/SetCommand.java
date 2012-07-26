package syam.FlagGame.Command;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import syam.FlagGame.Enum.FlagType;
import syam.FlagGame.Enum.Config.ConfigType;
import syam.FlagGame.Enum.Config.Configables;
import syam.FlagGame.Game.Game;
import syam.FlagGame.Game.GameManager;
import syam.FlagGame.Util.Actions;

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
				removeManagerMode();
			}else{
				Actions.message(null, player, "&c設定項目を指定してください！");
				sendAvailableConf();
			}
			return true;
		}

		// 管理モードであれば外す
		removeManagerMode();

		// ゲーム取得
		Game game = GameManager.getSelectedGame(player);
		if (game == null){
			Actions.message(null, player, "&c先に編集するゲームを選択してください");
			return true;
		}

		// 設定可能項目名を回す
		Configables conf = null;
		for (Configables check : Configables.values()){
			if (check.name().equalsIgnoreCase(args.get(0))){
				conf = check; break;
			}
		}

		// 列挙体にあったかチェック
		if (conf == null){
			Actions.message(sender, null, "&cその設定項目は存在しません！");
			sendAvailableConf();
			return true;
		}

		//if (conf.getConfigType() != ConfigType.MANAGER)
		//	removeManagerMode();

		// 設定項目によって処理を分ける
		switch (conf){
			// ステージ設定
			case STAGE:
				break;

			// 拠点設定
			case BASE:
				break;

			// フラッグ設定
			case FLAG:
				return setFlag(game);

			// チェスト設定
			case CHEST:
				return setChest(game);

			// 定義漏れ
			default:
				Actions.message(sender, null, "&c設定項目が不正です 開発者にご連絡ください");
				log.warning(logPrefix+ "Undefined configables! Please report this!");
				break;
		}

		return true;
	}

	/* ***** ここから各設定関数 ****************************** */

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
		GameManager.setManager(null, Configables.FLAG);
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


	/* ***** ここまで **************************************** */

	/**
	 * 管理モードになっていれば解除する
	 */
	private void removeManagerMode(Player player){
		Configables conf = GameManager.getManager(player);
		if (conf != null){
			GameManager.setManager(player, null);
			Actions.message(null, player, "&a"+conf.getConfigName()+"管理モードを解除しました！");
		}
	}
	private void removeManagerMode(){
		removeManagerMode(player);
	}

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
