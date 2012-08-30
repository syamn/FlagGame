package syam.FlagGame.FGPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import syam.FlagGame.FlagGame;
import syam.FlagGame.Database.Database;

public class PlayerProfile {
	// Logger
	public static final Logger log = FlagGame.log;
	private static final String logPrefix = FlagGame.logPrefix;
	private static final String msgPrefix = FlagGame.msgPrefix;

	private String playerName;
	private boolean loaded = false;

	/* mySQL Stuff */
	private int playerID;

	/* Data */
	private long lastjoingame = 0;	// 最終ゲーム参加日時
	private int status = 0;			// プレイヤーステータス

	private int played = 0;			// プレイ回数
	private int exit = 0;			// 途中退場回数

	private int win = 0;			// win
	private int lose = 0;			// lose
	private int draw = 0;			// draw

	private int kill = 0;			// Kill数
	private int death = 0;			// Death数

	/**
	 * コンストラクタ
	 * @param playerName プレイヤー名
	 * @param addNew 新規プレイヤーとしてデータを読み込むかどうか
	 */
	public PlayerProfile(String playerName, boolean addNew){
		this.playerName = playerName;

		if (!loadMySQL() && addNew){
			addMySQLPlayer();
			loaded = true;
		}
	}

	/**
	 * データベースからプレイヤーデータを読み込み
	 * @return 正常終了すればtrue、基本データテーブルにデータがなければfalse
	 */
	public boolean loadMySQL(){
		Database database = FlagGame.getPlayerDatabase();
		String tablePrefix = FlagGame.getInstance().getConfigs().mysqlTablePrefix;

		// プレイヤーID(DB割り当て)を読み出す
		playerID = database.getInt("SELECT player_id FROM " + tablePrefix + "users WHERE player_name = '" + playerName + "'");

		// プレイヤー基本テーブルにデータがなければ何もしない
		if (playerID == 0){
			return false;
		}

		/* *** usersテーブルデータ読み込み *************** */
		HashMap<Integer, ArrayList<String>> usersDatas = database.read("SELECT lastjoingame, status FROM " + tablePrefix + "users WHERE player_id = " + playerID);
		ArrayList<String> dataValues = usersDatas.get(1);

		if (dataValues == null){
			log.severe(playerName + "does not exist in the users table! (user: " + playerName + ")");
			return false;
		}else{
			// データ読み出し
			this.lastjoingame = Long.valueOf(dataValues.get(0));
			this.status = Integer.valueOf(dataValues.get(1));
		}
		dataValues.clear();


		/* *** recordsテーブルデータ読み込み *************** */
		HashMap<Integer, ArrayList<String>> recordsDatas = database.read("SELECT `played`, `exit`, `win`, `lose`, `draw`, `kill`, `death` FROM " + tablePrefix + "records WHERE player_id = " + playerID);
		dataValues = recordsDatas.get(1);

		if (dataValues == null){
			// 新規レコード追加
			database.write("INSERT INTO " + tablePrefix + "records (player_id) VALUES (" + playerID + ")");
			log.warning(playerName + "does not exist in the records table. Their records will be reset.");
		}else{
			// データ読み出し
			this.played = Integer.valueOf(dataValues.get(0));
			this.exit = Integer.valueOf(dataValues.get(1));
			this.win = Integer.valueOf(dataValues.get(2));
			this.lose = Integer.valueOf(dataValues.get(3));
			this.draw = Integer.valueOf(dataValues.get(4));
			this.kill = Integer.valueOf(dataValues.get(5));
			this.death = Integer.valueOf(dataValues.get(6));
		}
		dataValues.clear();

		// 読み込み正常終了
		loaded = true;
		return true;
	}

	/**
	 * 新規ユーザーデータをMySQLデータベースに追加
	 */
	private void addMySQLPlayer(){
		Database database = FlagGame.getPlayerDatabase();
		String tablePrefix = FlagGame.getInstance().getConfigs().mysqlTablePrefix;

		database.write("INSERT INTO " + tablePrefix + "users (player_name) VALUES ('" + playerName + "')"); // usersテーブル
		playerID = database.getInt("SELECT player_id FROM "+tablePrefix + "users WHERE player_name = '" + playerName + "'");
		database.write("INSERT INTO " + tablePrefix + "records (player_id) VALUES (" + playerID + ")"); // recordsテーブル
	}

	/**
	 * プレイヤーデータをMySQLデータベースに保存
	 */
	public void save(){
		Long timestamp = System.currentTimeMillis() / 1000;


	}

	/* getter / setter */
	public int getPlayerID(){
		return playerID;
	}
	public boolean isLoaded(){
		return loaded;
	}

	/* Data */

	// lastjoingame
	public void updateLastJoinedGame(){
		this.lastjoingame = System.currentTimeMillis() / 1000;
	}
	public long getLastJoinedGame(){
		return this.lastjoingame;
	}
	// status
	public void setStatus(int status){
		this.status = status;
	}
	public int getStatus(){
		return this.status;
	}

	// played
	public void setPlayed(int played){
		this.played = played;
	}
	public int getPlayed(){
		return this.played;
	}
	public void addPlayed(){
		this.played = this.played + 1;
	}
	// exit
	public void setExit(int exit){
		this.exit = exit;
	}
	public int getExit(){
		return this.exit;
	}
	public void addExit(){
		this.exit = this.exit + 1;
	}

	// win
	public void setWin(int win){
		this.win = win;
	}
	public int getWin(){
		return this.win;
	}
	public void addWin(){
		this.win = this.win + 1;
	}
	// lose
	public void setLose(int lose){
		this.lose = lose;
	}
	public int getLose(){
		return this.lose;
	}
	public void addLose(){
		this.lose = this.lose + 1;
	}
	// draw
	public void setDraw(int draw){
		this.draw = draw;
	}
	public int getDraw(){
		return this.draw;
	}
	public void addDraw(){
			this.draw = this.draw + 1;
		}

	// kill
	public void setKill(int kill){
		this.kill = kill;
	}
	public int getKill(){
		return this.kill;
	}
	public void addKill(){
		this.kill = this.kill + 1;
	}
	// death
	public void setDeath(int death){
		this.death = death;
	}
	public int getDeath(){
		return this.death;
	}
	public void addDeath(){
		this.death = this.death + 1;
	}

}
