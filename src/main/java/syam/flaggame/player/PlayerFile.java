package syam.flaggame.player;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import syam.flaggame.FlagGame;

@Deprecated
public class PlayerFile {
	// Logger
	public static final Logger log = FlagGame.log;
	private static final String logPrefix = FlagGame.logPrefix;
	private static final String msgPrefix = FlagGame.msgPrefix;

	private boolean saved = true;
	private FileConfiguration conf = new YamlConfiguration();
	private File file;

	private String playerName = null;

	/* プレイヤー固有データ */
	private int kill = 0; // Kill数
	private int death = 0; // Death数

	private int played = 0; // プレイ回数
	private int exit = 0; // 途中退場回数

	private int win = 0; // win
	private int lose = 0; // lose
	private int draw = 0; // draw


	/**
	 * コンストラクタ
	 * @param name
	 */
	public PlayerFile(final String name){
		this.playerName = name;
		String filename = FlagGame.getInstance().getDataFolder() + System.getProperty("file.separator") +
				"userData" + System.getProperty("file.separator") + name + ".yml";

		this.file = new File(filename);

		load();
	}

	/**
	 * ファイルからプレイヤーデータを読み込む
	 * @return
	 */
	public boolean load(){
		if (!file.exists()){
			if (!file.getParentFile().exists()){
				file.getParentFile().mkdir();
			}
			saved = false;
			if (!save()){
				throw new IllegalArgumentException(logPrefix+ "Could not create player data!");
			}
		}

		try{
			conf.load(file);

			// 読むデータキー
			kill = conf.getInt("kill", 0);
			death = conf.getInt("death", 0);

			played = conf.getInt("played", 0);
			exit = conf.getInt("exit", 0);

			win = conf.getInt("win", 0);
			lose = conf.getInt("lose", 0);
			draw = conf.getInt("draw", 0);

		}catch (Exception ex){
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * プレイヤーデータをファイルに保存する
	 * @return
	 */
	public boolean save(){
		if (!saved){
			try{
				// 保存するデータキー
				conf.set("kill", kill);
				conf.set("death", death);

				conf.set("played", played);
				conf.set("exit", exit);

				conf.set("win", win);
				conf.set("lose", lose);
				conf.set("draw", draw);

				// 保存
				conf.save(file);
			}catch (Exception ex){
				ex.printStackTrace();
				return false;
			}
		}
		return true;
	}

	//
	// ****** setter / getter ******
	//

	public boolean isSaved(){
		return saved;
	}
	public String getPlayerName(){
		return playerName;
	}

	// kill
	public void setKill(int kill){
		saved = false;
		this.kill = kill;
	}
	public int getKill(){
		return this.kill;
	}
	public void addKill(){
		saved = false;
		this.kill = this.kill + 1;
	}
	// death
	public void setDeath(int death){
		saved = false;
		this.death = death;
	}
	public int getDeath(){
		return this.death;
	}
	public void addDeath(){
		saved = false;
		this.death = this.death + 1;
	}

	// played
	public void setPlayed(int played){
		saved = false;
		this.played = played;
	}
	public int getPlayed(){
		return this.played;
	}
	public void addPlayed(){
		saved = false;
		this.played = this.played + 1;
	}
	// exit
	public void setExit(int exit){
		saved = false;
		this.exit = exit;
	}
	public int getExit(){
		return this.exit;
	}
	public void addExit(){
		saved = false;
		this.exit = this.exit + 1;
	}

	// win
	public void setWin(int win){
		saved = false;
		this.win = win;
	}
	public int getWin(){
		return this.win;
	}
	public void addWin(){
		saved = false;
		this.win = this.win + 1;
	}
	// lose
	public void setLose(int lose){
		saved = false;
		this.lose = lose;
	}
	public int getLose(){
		return this.lose;
	}
	public void addLose(){
		saved = false;
		this.lose = this.lose + 1;
	}
	// draw
	public void setDraw(int draw){
		saved = false;
		this.draw = draw;
	}
	public int getDraw(){
		return this.draw;
	}
	public void addDraw(){
		saved = false;
		this.draw = this.draw + 1;
	}

}