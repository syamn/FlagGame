package syam.FlagGame.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

import syam.FlagGame.FlagGame;

public class Database {
	// Logger
	public static final Logger log = FlagGame.log;
	private static final String logPrefix = FlagGame.logPrefix;
	private static final String msgPrefix = FlagGame.msgPrefix;

	private static FlagGame plugin;

	private static String connectionString = "jdbc:mysql://" + plugin.getConfigs().mysqlAddress + ":" + plugin.getConfigs().mysqlPort
			+ "/" + plugin.getConfigs().mysqlDBName + "?user=" + plugin.getConfigs().mysqlUserName + "&password=" + plugin.getConfigs().mysqlUserPass;
	private static String tablePrefix = plugin.getConfigs().mysqlTablePrefix;
	private static Connection connection = null;
	private static long reconnectTimestamp = 0;

	/**
	 * コンストラクタ
	 * @param plugin FlagGameプラグインインスタンス
	 */
	public Database(final FlagGame plugin){
		this.plugin = plugin;

		conncet(); // 接続

		// ドライバを読み込む
		try{
			Class.forName("com.mysql.jdbc.Driver");
			DriverManager.getConnection(connectionString);
		}catch (ClassNotFoundException ex1){
			log.severe(ex1.getLocalizedMessage());
		}catch (SQLException ex2){
			log.severe(ex2.getLocalizedMessage());
			printErrors(ex2);
		}
	}

	/**
	 * データベースに接続する
	 */
	public static void conncet(){
		try{
			log.info(logPrefix+ "Attempting connection to MySQL..");

			Properties connectionProperties = new Properties();
			connectionProperties.put("autoReconnect", "false");
			connectionProperties.put("maxReconnects", "0");
			connection = DriverManager.getConnection(connectionString, connectionProperties);

			log.info(logPrefix+ "Connected MySQL database!");
		}catch (SQLException ex){
			log.severe(logPrefix+ "Could not connect MySQL database!");
			ex.printStackTrace();
			printErrors(ex);
		}
	}

	public void createStructure(){

	}

	/**
	 * SQLクエリーを発行する
	 * @param sql 発行するSQL文
	 * @return クエリ成功ならtrue、他はfalse
	 */
	public boolean write(String sql){
		if (isConnected()){
			try{
				PreparedStatement statement = connection.prepareStatement(sql);
				statement.executeUpdate();
				statement.close();
				return true;
			}catch (SQLException ex){
				printErrors(ex);
				return false;
			}
		}
		// 未接続
		else{
			attemptReconnect();
		}

		return false;
	}

	/**
	 * int型の値を取得します。
	 * @param sql 発行するSQL文
	 * @return 最初のローにある数値
	 */
	public int getInt(String sql){
		ResultSet resultSet;
		int result = 0;

		if (isConnected()){
			try{
				PreparedStatement statement = connection.prepareStatement(sql);
				resultSet = statement.executeQuery();

				if (resultSet.next()){
					result = resultSet.getInt(1);
				}else{
					result = 0;
				}

				statement.close();
			}catch (SQLException ex){
				printErrors(ex);
			}
		}else{
			attemptReconnect();
		}

		return result;
	}

	/**
	 * 接続状況を返す
	 * @return 接続中ならtrue、タイムアウトすればfalse
	 */
	public static boolean isConnected(){
		if (connection == null){
			return false;
		}

		try{
			return connection.isValid(3);
		}catch (SQLException ex){
			return false;
		}
	}

	/**
	 * MySQLデータベースへ再接続を試みる
	 */
	public static void attemptReconnect(){
		final int RECONNECT_WAIT_TICKS = 60000;
		final int RECONNECT_DELAY_TICKS = 1200;

		if (reconnectTimestamp + RECONNECT_WAIT_TICKS < System.currentTimeMillis()){
			reconnectTimestamp = System.currentTimeMillis();
			log.severe(logPrefix+ "Conection to MySQL was lost! Attempting to reconnect 60 seconds...");
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new MySQLReconnect(plugin), RECONNECT_DELAY_TICKS);
		}
	}

	/**
	 * エラーを出力する
	 * @param ex
	 */
	private static void printErrors(SQLException ex){
		log.warning("SQLException:" +ex.getMessage());
		log.warning("SQLState:" +ex.getSQLState());
		log.warning("ErrorCode:" +ex.getErrorCode());
	}
}
