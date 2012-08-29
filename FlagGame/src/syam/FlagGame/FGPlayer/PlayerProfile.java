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
	private int kill = 0; // Kill数
	private int death = 0; // Death数

	private int played = 0; // プレイ回数
	private int exit = 0; // 途中退場回数

	private int win = 0; // win
	private int lose = 0; // lose
	private int draw = 0; // draw


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

	public boolean loadMySQL(){
		Database database = FlagGame.getPlayerDatabase();
		String tablePrefix = FlagGame.getInstance().getConfigs().mysqlTablePrefix;

		// プレイヤーID(DB割り当て)を読み出す
		playerID = database.getInt("SELECT id FROM " + tablePrefix + "users WHERE player_name = '" + playerName + "'");

		// プレイヤー基本テーブルにデータがなければ何もしない
		if (playerID == 0){
			return false;
		}

		//

		// recordsデータ読み込み
		HashMap<Integer, ArrayList<String>> recordsDatas = database.read("SELECT played, exit, win, lose, draw, kill, death FROM " + tablePrefix + "records WHERE player_id = " + playerID);
		ArrayList<String> dataValues = recordsDatas.get(1);

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


		return true;
	}

	private void addMySQLPlayer(){

	}

}
