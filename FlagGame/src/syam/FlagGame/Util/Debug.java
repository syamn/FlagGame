/**
 * FlagGame - Package: syam.FlagGame.Util
 * Created: 2012/09/19 19:13:25
 */
package syam.FlagGame.Util;

import java.io.IOException;
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
	private TextFileHandler logFile = null;
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
		this.logFile = new TextFileHandler(logFilePath);
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

			// ファイル出力
			if (logFile != null){
				 try {
					logFile.appendLine(sb.toString());
				} catch (IOException ex) {
					log.warning(logPrefix+ "Could not write debug log file!");
					ex.printStackTrace();
				}
			}
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

		if (this.debug == isDebug)
			return;

		this.debug = isDebug;

		if (isDebug){
			log.info(logPrefix+ "DEBUG MODE ENABLED!");
		}else{
			log.info(logPrefix+ "DEBUG MODE DISABLED!");
		}
	}
	/**
	 * デバッグ状態を取得する
	 * @return debug
	 */
	public boolean isDebug(){
		return this.debug;
	}

	/**
	 * デバッグ出力先ファイルを設定する null無効にする
	 * @param filePath null or FilePath
	 */
	public void setLogFile(String filePath){
		this.logFile = new TextFileHandler(filePath);
	}
	/**
	 * デバッグをファイルに出力するか取得する
	 * @return boolean
	 */
	public boolean isDebugToFile(){
		return (logFile == null) ? false : true;
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
