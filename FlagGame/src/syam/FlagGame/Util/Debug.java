/**
 * FlagGame - Package: syam.FlagGame.Util
 * Created: 2012/09/19 19:13:25
 */
package syam.FlagGame.Util;

import java.text.DateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;

import syam.FlagGame.FlagGame;

/**
 * Debug (Debug.java)
 * @author syam(syamn)
 */
public class Debug {
	private static Debug instance = null;
	private static long startup = 0L;
	private long timeLog = 0L;

	private Logger log;
	private String logPrefix;
	private String filePath;
	private boolean debug = false;

	/**
	 * 初期化
	 * @param log
	 * @param logPrefix
	 * @param isDebug
	 */
	public void init(Logger log, String logPrefix, boolean isDebug){
		this.log = log;

		this.logPrefix = logPrefix;
		if (logPrefix == null){
			this.logPrefix = "["+log.getName()+"] ";
		}else{
			if(logPrefix.endsWith(" ")){
				this.logPrefix = logPrefix;
			}else{
				this.logPrefix = logPrefix + " ";
			}
		}

		setDebug(isDebug);
	}
	/**
	 * 初期化
	 * @param log
	 * @param logPrefix
	 * @param logFilePath
	 * @param isDebug
	 */
	public void init(Logger log, String logPrefix, String logFilePath, boolean isDebug){
		this.init(log, logPrefix, isDebug);
		this.filePath = logFilePath;
	}

	/**
	 * デバッグログを出力する
	 * @param args
	 */
	public void debug(Object... args){
		if (debug){
			StringBuilder sb = new StringBuilder(logPrefix);

			for(int i = 0; i < args.length; i++){
				sb.append(args[i]);
			}

			log.fine(sb.toString());
		}
	}

	/* getter / setter */

	/**
	 * デバッグ状態を設定する
	 * @param isDebug
	 */
	public void setDebug(boolean isDebug){
		// 未初期化状態
		if (log == null)
			return;

		this.debug = isDebug;
	}
	/**
	 * デバッグ状態を取得する
	 * @return debug
	 */
	public boolean isDebug(){
		return this.debug;
	}

	/**
	 * インスタンスを返す
	 * @return Debug
	 */
	public static Debug getInstance(){
		if (instance == null){
			// ダブルチェック
			synchronized (Debug.class) {
				if (instance == null){
					instance = new Debug();
				}
			}
		}

		return instance;
	}

	/**
	 * デバッグ用フォーマッター
	 * DebugFormatter (Debug.java)
	 * @author syam(syamn)
	 */
	private class DebugFormatter extends Formatter {
		private final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
		private final String newLine = System.getProperty("line.separator");

		@Override
		public String format(LogRecord record){
			StringBuilder sb = new StringBuilder(200);

			sb.append("[");
			sb.append(df.format(new Date(record.getMillis())));
			sb.append("] ");
			sb.append(record.getMessage());
			sb.append(newLine);

			return sb.toString();
		}
	}

	/* ********** static ********** */
	/**
	 * プラグイン起動時に現在のミリ秒を記録する
	 */
	public static void setStartupBeginTime(){
		if (startup <= 0L){
			startup = System.currentTimeMillis();
		}
	}

	/**
	 * デバッグ時刻計測開始
	 * @param actionName
	 * @param useStartup
	 */
	public void debugStartTimer(String actionName, boolean useStartup) {
    	timeLog = System.currentTimeMillis();

    	if (debug){
    		if (useStartup){
    			debug("[Startup Timer] starting " + actionName + " (t+" + (System.currentTimeMillis()-startup) + ")");
    		}else{
    			debug("[Startup Timer] starting " + actionName);
    		}
    	}
    }

	/**
	 * デバッグ時刻計測開始
	 * @param actionName
	 */
	public void debugStartTimer(String actionName){
		this.debugStartTimer(actionName, false);
	}

	/**
	 * デバッグ時刻計測終了
	 * @param actionName
	 */
	public void debugEndTimer(String actionName){
		if (debug){
			debug("[Startup Timer] " + actionName + " finished in " + (System.currentTimeMillis()-timeLog) + "ms");
		}
	}
}
