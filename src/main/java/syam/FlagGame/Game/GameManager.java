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

	// 選択中のゲーム
	private static Map<String, OldGame> selectedGame = new HashMap<String, OldGame>();
	// 選択中のブロック
	private static Map<String, Location> selectedBlock = new HashMap<String, Location>();
	// 選択中のフラッグ種類
	private static Map<String, FlagType> selectedFlagType = new HashMap<String, FlagType>();

	// ゲームマネージャモードのリスト
	//private static List<String> fgFlagManager = new ArrayList<String>();
	//private static List<String> fgChestManager = new ArrayList<String>();
	private static Map<String, Configables> managersMap = new HashMap<String, Configables>();

	/**
	 * 全ゲームステージプロファイルを保存する
	 */
	public static void saveAll(){
		for (OldGame game : FlagGame.getInstance().games.values()){
			game.getProfile().save();
		}
	}

	/* getter/setter */

	/**
	 * 指定したゲームを選択中にする
	 * @param player 対象プレイヤー
	 * @param game 対象ゲーム
	 */
	public static void setSelectedGame(Player player, OldGame game){
		selectedGame.put(player.getName(), game);
	}
	/**
	 * 選択中のゲームを返す
	 * @param player 対象のプレイヤー
	 * @return null または対象のゲーム
	 */
	public static OldGame getSelectedGame(Player player){
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
	 * 指定したフラッグタイプを選択中にする
	 * @param player プレイヤー
	 * @param loc 設定するフラッグタイプ
	 */
	public static void setSelectedFlagType(Player player, FlagType type){
		if (type == null){
			if (selectedFlagType.containsKey(player.getName()))
				selectedFlagType.remove(player.getName());
		}
		else{
			selectedFlagType.put(player.getName(), type);
		}
	}
	/**
	 * 選択中のフラッグタイプを返す
	 * @param player 対象プレイヤー
	 * @return null またはフラッグタイプ
	 */
	public static FlagType getSelectedFlagType(Player player){
		if (player == null || !selectedFlagType.containsKey(player.getName())){
			return null;
		}else{
			return selectedFlagType.get(player.getName());
		}
	}

	/**
	 * プレイヤーをマネージモードにする/しない
	 * @param player 対象のプレイヤー
	 * @param state true = 管理モードにする/false = しない
	 */
	public static void setManager(Player player, Configables conf){
		// conf == null ならマネージモード解除
		if (conf == null){
			if (managersMap.containsKey(player.getName()))
				managersMap.remove(player.getName());
		}
		// マネージモード設定
		else{
			managersMap.put(player.getName(), conf);
		}
	}
	/**
	 * プレイヤーがマネージモードかどうか、またその設定種類を返す
	 * @param player チェックするプレイヤー
	 * @return nullなら管理モードでない
	 */
	public static Configables getManager(Player player){
		if (player == null || !managersMap.containsKey(player.getName())){
			return null;
		}else{
			return managersMap.get(player.getName());
		}
	}

	/**
	 * プレイヤーをすべての管理モードマップから削除する
	 * @param player
	 * @param silent
	 */
	public static void removeManager(Player player, boolean silent){
		Configables conf = getManager(player);
		if (conf != null){
			setManager(player, null);
			if (!silent)
				Actions.message(null, player, "&a"+conf.getConfigName()+"管理モードを解除しました！");
		}
		if (getSelectedFlagType(player) != null){
			setSelectedFlagType(player, null);
		}
	}
}