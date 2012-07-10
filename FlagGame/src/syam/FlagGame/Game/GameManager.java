package syam.FlagGame.Game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import syam.FlagGame.FlagGame;

public class GameManager {
	// Logger
	public static final Logger log = FlagGame.log;
	private static final String logPrefix = FlagGame.logPrefix;
	private static final String msgPrefix = FlagGame.msgPrefix;

	private final FlagGame plugin;
	public GameManager(final FlagGame plugin){
		this.plugin = plugin;
	}

	// 選択中のゲーム
	private static Map<String, Game> selectedGame = new HashMap<String, Game>();
	// 選択中のブロック
	private static Map<String, Location> selectedBlock = new HashMap<String, Location>();

	// ゲームマネージャモードのリスト
	private static List<String> fgFlagManager = new ArrayList<String>();
	private static List<String> fgChestManager = new ArrayList<String>();

	/* getter/setter */

	/**
	 * 指定したゲームを選択中にする
	 * @param player 対象プレイヤー
	 * @param game 対象ゲーム
	 */
	public static void setSelectedGame(Player player, Game game){
		selectedGame.put(player.getName(), game);
	}
	/**
	 * 選択中のゲームを返す
	 * @param player 対象のプレイヤー
	 * @return null または対象のゲーム
	 */
	public static Game getSelectedGame(Player player){
		if (player == null || !selectedGame.containsKey(player.getName())){
			return null;
		}else{
			return selectedGame.get(player.getName());
		}
	}

	/**
	 * 指定したブロックを選択中にする
	 * @param player プレイヤー
	 * @param loc 対象ブロックの座標
	 */
	public static void setSelectedBlock(Player player, Location loc){
		selectedBlock.put(player.getName(), loc);
	}
	/**
	 * 選択中のブロックの座標を返す
	 * @param player 対象プレイヤー
	 * @return null または対象ブロックLocation
	 */
	public static Location getSelectedBlock(Player player){
		if (player == null || !selectedBlock.containsKey(player.getName())){
			return null;
		}else{
			return selectedBlock.get(player.getName());
		}
	}

	/**
	 * プレイヤーをフラッグマネージモードにする/しない
	 * @param player 対象のプレイヤー
	 * @param state true = 管理モードにする/false = しない
	 */
	public static void setFlagManager(Player player, boolean state){
		if (state){
			if (!fgFlagManager.contains(player.getName()))
				fgFlagManager.add(player.getName());
		}else{
			if (fgFlagManager.contains(player.getName()))
				fgFlagManager.remove(player.getName());
		}
	}
	/**
	 * プレイヤーがゲームマネージモードかどうか返す
	 * @param player チェックするプレイヤー
	 * @return trueなら管理モード、falseなら管理モードでない
	 */
	public static boolean isFlagManager(Player player){
		if(player != null && fgFlagManager.contains(player.getName())){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * プレイヤーをチェストマネージモードにする/しない
	 * @param player 対象のプレイヤー
	 * @param state true = 管理モードにする/false = しない
	 */
	public static void setChestManager(Player player, boolean state){
		if (state){
			if (!fgChestManager.contains(player.getName()))
				fgChestManager.add(player.getName());
		}else{
			if (fgChestManager.contains(player.getName()))
				fgChestManager.remove(player.getName());
		}
	}
	/**
	 * プレイヤーがチェストマネージモードかどうか返す
	 * @param player チェックするプレイヤー
	 * @return trueなら管理モード、falseなら管理モードでない
	 */
	public static boolean isChestManager(Player player){
		if(player != null && fgChestManager.contains(player.getName())){
			return true;
		}else{
			return false;
		}
	}
}
