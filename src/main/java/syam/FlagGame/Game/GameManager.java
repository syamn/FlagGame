package syam.FlagGame.Game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import syam.FlagGame.FlagGame;
import syam.FlagGame.Enum.FlagType;
import syam.FlagGame.Enum.Config.Configables;
import syam.FlagGame.Util.Actions;

public class GameManager {
	// Logger
	public static final Logger log = FlagGame.log;
	private static final String logPrefix = FlagGame.logPrefix;
	private static final String msgPrefix = FlagGame.msgPrefix;

	private final FlagGame plugin;
	public GameManager(final FlagGame plugin){
		this.plugin = plugin;
	}

	private static HashMap<String, Game> games = new HashMap<String, Game>();

	/**
	 * ゲームマップを返す
	 * @return HashMap<String, Game>
	 */
	public static HashMap<String, Game> getGames(){
		return games;
	}

	/**
	 * ゲームを追加する
	 * @param stageName ステージ名
	 * @param game ゲームインスタンス
	 */
	public static void addGame(String stageName, Game game){
		games.put(stageName, game);
	}
	/**
	 * ゲームを削除する
	 * @param stageName ステージ名
	 */
	public static void removeGame(String stageName){
		games.remove(stageName);
	}


	/**
	 * ステージ名からゲームを返す
	 * @param gameName
	 * @return Game
	 */
	public static Game getGame(String gameName){
		return games.get(gameName);
	}
}
