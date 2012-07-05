package syam.FlagGame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.block.Action;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import syam.FlagGame.Command.BaseCommand;
import syam.FlagGame.Command.CreateCommand;
import syam.FlagGame.Command.HelpCommand;
import syam.FlagGame.Command.ReadyCommand;
import syam.FlagGame.Command.ReloadCommand;
import syam.FlagGame.Command.SaveCommand;
import syam.FlagGame.Command.SelectGameCommand;
import syam.FlagGame.Command.SelectTeamCommand;
import syam.FlagGame.Command.SetflagCommand;
import syam.FlagGame.Game.Game;
import syam.FlagGame.Game.GameManager;
import syam.FlagGame.Listeners.FGBlockListener;
import syam.FlagGame.Listeners.FGPlayerListener;

public class FlagGame extends JavaPlugin{

	/*
	 * TODO:
	 *  ゲーム参加者だけにメッセージキャストする等で使うので、一つの変数(リスト？マップ？配列はナシ)に参加プレイヤーを格納したい
	 *  → チーム毎も必須か 現状維持で HashMap<List,List>を使う
	 *
	 *
	 *  WorldEdit/Guard連携、イベントワールド全域保護、試合中はステージ外への移動を禁止
	 *  Valut連携、お金の概念にフック
	 *
	 *  プレイヤーも専用クラスを作る？
	 *
	 *  死亡メッセージをワールド外で非表示にする
	 *
	 */

	/*
	 * DONE:
	 * フラッグは新規クラス Flag を作って各ゲームに List<Flag> または HashMap<Location, Flag> で持たせる(後者のがいい？)
	 * → どちらのチームのものか(またはセンターか)、元と今のブロックのIDとデータ値、そのフラッグのポイント(ゲームの勝敗判定に使う点数)
	 *
	 * 分かりづらいのでコマンドをサブコマンドでクラスを分ける
	 *
	 */

	// ** Logger **
	public final static Logger log = Logger.getLogger("Minecraft");
	public final static String logPrefix = "[FlagGame] ";
	public final static String msgPrefix = "&6[FlagGame] &f";

	// ** Listener **
	private final FGPlayerListener playerListener = new FGPlayerListener(this);
	private final FGBlockListener blockListener = new FGBlockListener(this);

	// ** Commands **
	public static List<BaseCommand> commands = new ArrayList<BaseCommand>();

	// ** Private classes **
	private ConfigurationManager config;
	private GameManager gm;

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
		//getServer().getPluginCommand("flagadmin").setExecutor(new AdminCommand(this));
		registerCommands();

		// ゲームマネージャ
		gm = new GameManager(this);

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

	/**
	 * コマンドを登録
	 */
	private void registerCommands(){
		commands.add(new HelpCommand());
		commands.add(new CreateCommand());
		commands.add(new ReloadCommand());
		commands.add(new SelectGameCommand());
		commands.add(new SelectTeamCommand());
		commands.add(new SetflagCommand());
		commands.add(new ReadyCommand());
		commands.add(new SaveCommand());
	}

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
