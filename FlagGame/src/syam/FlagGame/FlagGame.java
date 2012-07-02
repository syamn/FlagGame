package syam.FlagGame;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import syam.FlagGame.Command.AdminCommand;
import syam.FlagGame.Game.Game;

public class FlagGame extends JavaPlugin{
	// ** Logger **
	public final static Logger log = Logger.getLogger("Minecraft");
	public final static String logPrefix = "[FlagGame] ";
	public final static String msgPrefix = "&c[FlagGame] &f";

	// ** Listener **

	// ** Private classes **
	private ConfigurationManager config;

	// ** Variable **
	public final String GameWorld = "flag";
	// 存在するゲーム <String 一意のゲームID, Game>
	public HashMap<String, Game> games = new HashMap<String, Game>();

	// ** Instance **
	private static FlagGame instance;

	/**
	 * プラグイン起動処理
	 */
	public void onEnable(){
		instance  = this;
		config = new ConfigurationManager(this);

		// loadconfig
		try{
			config.loadConfig(true);
		}catch (Exception ex){
			log.warning(logPrefix+"an error occured while trying to load the config file.");
			ex.printStackTrace();
		}
		
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
