package syam.FlagGame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import syam.FlagGame.Command.BaseCommand;
import syam.FlagGame.Command.CreateCommand;
import syam.FlagGame.Command.DeleteCommand;
import syam.FlagGame.Command.HelpCommand;
import syam.FlagGame.Command.InfoCommand;
import syam.FlagGame.Command.JoinCommand;
import syam.FlagGame.Command.ReadyCommand;
import syam.FlagGame.Command.ReloadCommand;
import syam.FlagGame.Command.SaveCommand;
import syam.FlagGame.Command.SelectGameCommand;
import syam.FlagGame.Command.SetbaseCommand;
import syam.FlagGame.Command.SetchestCommand;
import syam.FlagGame.Command.SetflagCommand;
import syam.FlagGame.Command.SetoptionCommand;
import syam.FlagGame.Command.SetspawnCommand;
import syam.FlagGame.Command.StartCommand;
import syam.FlagGame.Command.TpCommand;
import syam.FlagGame.Game.Game;
import syam.FlagGame.Game.GameFileManager;
import syam.FlagGame.Game.GameManager;
import syam.FlagGame.Listeners.DeathNotifierListener;
import syam.FlagGame.Listeners.FGBlockListener;
import syam.FlagGame.Listeners.FGEntityListener;
import syam.FlagGame.Listeners.FGPlayerListener;

public class FlagGame extends JavaPlugin{

	/*
	 * TODO:
	 *  プレイヤーも専用クラスを作る？
	 *
	 *  順位表
	 *
	 *
	 *
	 *  定期的な状況告知
	 *
	 *  設定コマンドの見直し
	 *  フラッグ設定方法の見直し
	 *
	 */

	/*
	 * DONE:
	 * フラッグは新規クラス Flag を作って各ゲームに List<Flag> または HashMap<Location, Flag> で持たせる(後者のがいい？)
	 * → どちらのチームのものか(またはセンターか)、元と今のブロックのIDとデータ値、そのフラッグのポイント(ゲームの勝敗判定に使う点数)
	 *
	 * 分かりづらいのでコマンドをサブコマンドでクラスを分ける
	 *
	 * 死亡メッセージをワールド外で非表示にする (DeathNotify開発)
	 *
	 * Valut連携、お金の概念にフック
	 *
	 * ゲーム参加者だけにメッセージキャストする等で使うので、一つの変数(リスト？マップ？配列はナシ)に参加プレイヤーを格納したい
	 *  → チーム毎も必須か 現状維持で HashMap<List,List>を使う
	 *
	 * WorldEdit/Guard連携、イベントワールド全域保護、試合中はステージ外への移動を禁止 → onMoveでチェックすると重そうなので使わない
	 *
	 * スタート時のカウントダウン
	 *
	 */

	// ** Logger **
	public final static Logger log = Logger.getLogger("Minecraft");
	public final static String logPrefix = "[FlagGame] ";
	public final static String msgPrefix = "&6[FlagGame] &f";

	// ** Listener **
	private final FGPlayerListener playerListener = new FGPlayerListener(this);
	private final FGBlockListener blockListener = new FGBlockListener(this);
	private final FGEntityListener entityListener = new FGEntityListener(this);
	private final DeathNotifierListener dnListener = new DeathNotifierListener(this);

	// ** Commands **
	public static List<BaseCommand> commands = new ArrayList<BaseCommand>();

	// ** Private classes **
	private ConfigurationManager config;
	private GameManager gm;
	private GameFileManager gfm;

	// ** Variable **
	// 存在するゲーム <String 一意のゲームID, Game>
	public HashMap<String, Game> games = new HashMap<String, Game>();

	// ** Instance **
	private static FlagGame instance;

	// Hookup plugins
	public boolean usingDeathNotifier = false;
	public static Vault vault = null;
	public static Economy economy = null;

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

		// プラグインフック
		// Vault
		setupVault();
		// DeathNotifier
		Plugin p = pm.getPlugin("DeathNotifier");
		if (p != null){
			pm.registerEvents(dnListener, this); // Regist Listener
			usingDeathNotifier = true; //フラグ
			log.info(logPrefix+ "Hooked to DeathNotifier!");
		}

		// プラグインを無効にした場合進まないようにする
		if (!pm.isPluginEnabled(this)){
			return;
		}

		// Regist Listeners
		pm.registerEvents(playerListener, this);
		pm.registerEvents(blockListener, this);
		pm.registerEvents(entityListener, this);

		// コマンド登録
		registerCommands();

		// マネージャ
		gm = new GameManager(this);
		gfm = new GameFileManager(this);

		// ゲーム読み込み
		gfm.loadGames();

		// メッセージ表示
		PluginDescriptionFile pdfFile=this.getDescription();
		log.info("["+pdfFile.getName()+"] version "+pdfFile.getVersion()+" is enabled!");
	}

	/**
	 * プラグイン停止処理
	 */
	public void onDisable(){
		// 開始中のゲームをすべて終わらせる
		for (Game game : games.values()){
			if (game.isStarting()){
				game.cancelTimerTask();
				game.finish();
				game.log("Game finished because disabling plugin..");
			}
		}


		// ゲームデータ保存
		if (gfm != null){
			gfm.saveGames();
		}

		// メッセージ表示
		PluginDescriptionFile pdfFile=this.getDescription();
		log.info("["+pdfFile.getName()+"] version "+pdfFile.getVersion()+" is disabled!");
	}

	/**
	 * Vaultプラグインにフック
	 */
	private void setupVault(){
		Plugin plugin = this.getServer().getPluginManager().getPlugin("Vault");
		if(plugin != null & plugin instanceof Vault) {
			RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
			// 経済概念のプラグインがロードされているかチェック
			if(economyProvider==null){
	        	log.warning(logPrefix+"Economy plugin not Fount. Disabling plugin.");
		        getPluginLoader().disablePlugin(this);
		        return;
			}

			try{
				vault = (Vault) plugin;
				economy = economyProvider.getProvider();
			} // 例外チェック
			catch(Exception e){
				log.warning(logPrefix+"Could NOT be hook to Vault. Disabling plugin.");
		        getPluginLoader().disablePlugin(this);
		        return;
			}
			log.info(logPrefix+"Hooked to Vault!");
		} else {
			// Vaultが見つからなかった
	        log.warning(logPrefix+"Vault was NOT found! Disabling plugin.");
	        getPluginLoader().disablePlugin(this);
	        return;
	    }
	}

	/**
	 * コマンドを登録
	 */
	private void registerCommands(){
		// Intro Commands
		commands.add(new HelpCommand());
		commands.add(new InfoCommand());
		commands.add(new JoinCommand());

		// Start Commands
		commands.add(new ReadyCommand());
		commands.add(new StartCommand());

		// Admin Commands
		commands.add(new CreateCommand());
		commands.add(new DeleteCommand());
		commands.add(new SelectGameCommand());
		commands.add(new SetflagCommand());
		commands.add(new SetchestCommand());
		commands.add(new TpCommand());
		commands.add(new SetspawnCommand());
		commands.add(new SetbaseCommand());
		commands.add(new SetoptionCommand());
		commands.add(new SaveCommand());
		commands.add(new ReloadCommand());
	}

	/**
	 * コマンドが呼ばれた
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[]){
		if (cmd.getName().equalsIgnoreCase("flag")){
			if(args.length == 0){
				// 引数ゼロはヘルプ表示
				args = new String[]{"help"};
			}

			outer:
			for (BaseCommand command : commands.toArray(new BaseCommand[0])){
				String[] cmds = command.name.split(" ");
				for (int i = 0; i < cmds.length; i++){
					if (i >= args.length || !cmds[i].equalsIgnoreCase(args[i])){
						continue outer;
					}
					// 実行
					return command.run(this, sender, args, commandLabel);
				}
			}
			// 有効コマンドなし ヘルプ表示
			new HelpCommand().run(this, sender, args, commandLabel);
			return true;
		}
		return false;
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
	 * ゲームマネージャを返す
	 * @return GameManager
	 */
	public GameManager getManager(){
		return gm;
	}

	/** ゲームファイルマネージャを返す
	 * @return GameManager
	 */
	public GameFileManager getFileManager(){
		return gfm;
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
