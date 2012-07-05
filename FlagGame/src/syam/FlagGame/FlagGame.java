package syam.FlagGame;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import syam.FlagGame.Command.AdminCommand;
import syam.FlagGame.Game.Game;
import syam.FlagGame.Listeners.FGBlockListener;
import syam.FlagGame.Listeners.FGPlayerListener;

public class FlagGame extends JavaPlugin{

	/*
	 * TODO:
	 * フラッグは新規クラス Flag を作って各ゲームに List<Flag> または HashMap<Location, Flag> で持たせる(後者のがいい？)
	 *  → どちらのチームのものか(またはセンターか)、元と今のブロックのIDとデータ値、そのフラッグのポイント(ゲームの勝敗判定に使う点数)
	 *
	 *  ゲーム参加者だけにメッセージキャストする等で使うので、一つの変数(リスト？マップ？配列はナシ)に参加プレイヤーを格納したい
	 *  → チーム毎も必須か 現状維持で HashMap<List,List>を使う
	 *
	 *
	 *  WorldEdit/Guard連携、イベントワールド全域保護、試合中はステージ外への移動を禁止
	 *  Valut連携、お金の概念にフック
	 *
	 *  プレイヤーも専用クラスを作る？
	 *
	 *  分かりづらいのでコマンドをサブコマンドでクラスを分ける
	 *
	 *  死亡メッセージをワールド外で非表示にする
	 *
	 */

	// ** Logger **
	public final static Logger log = Logger.getLogger("Minecraft");
	public final static String logPrefix = "[FlagGame] ";
	public final static String msgPrefix = "&6[FlagGame] &f";

	// ** Listener **
	private final FGPlayerListener playerListener = new FGPlayerListener(this);
	private final FGBlockListener blockListener = new FGBlockListener(this);

	// ** Private classes **
	private ConfigurationManager config;

	// ** Variable **
	// 存在するゲーム <String 一意のゲームID, Game>
	public HashMap<String, Game> games = new HashMap<String, Game>();

	// ** Instance **
	private static FlagGame instance;

	/**
	 * プラグイン起動処理
	 */
	public void onEnable(){
		instance  = this;
		PluginManager pm = getServer().getPluginManager();
		config = new ConfigurationManager(this);

		// loadconfig
		try{
			config.loadConfig(true);
		}catch (Exception ex){
			log.warning(logPrefix+"an error occured while trying to load the config file.");
			ex.printStackTrace();
		}

		// Regist Listeners
		pm.registerEvents(playerListener, this);
		pm.registerEvents(blockListener, this);

		// コマンド登録
		getServer().getPluginCommand("flagadmin").setExecutor(new AdminCommand(this));

		// メッセージ表示
		PluginDescriptionFile pdfFile=this.getDescription();
		log.info("["+pdfFile.getName()+"] version "+pdfFile.getVersion()+" is enabled!");
	}

	/**
	 * プラグイン停止処理
	 */
	public void onDisable(){
		// メッセージ表示
		PluginDescriptionFile pdfFile=this.getDescription();
		log.info("["+pdfFile.getName()+"] version "+pdfFile.getVersion()+" is disabled!");
	}

	/* getter */

	/**
	 * ゲームを返す
	 * @param gameName
	 * @return Game
	 */
	public Game getGame(String gameName){
		if (!games.containsKey(gameName)){
			return null;
		}else{
			return games.get(gameName);
		}
	}

	/**
	 * 設定マネージャを返す
	 * @return ConfigurationManager
	 */
	public ConfigurationManager getConfigs() {
		return config;
	}

	/**
	 * インスタンスを返す
	 * @return VoteBanインスタンス
	 */
	public static FlagGame getInstance(){
		return instance;
	}
}
