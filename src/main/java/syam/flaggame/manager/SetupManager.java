/**
 * FlagGame - Package: syam.flaggame.game
 * Created: 2012/09/26 22:54:17
 */
package syam.flaggame.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import syam.flaggame.FlagGame;
import syam.flaggame.enums.FlagType;
import syam.flaggame.enums.config.Configables;
import syam.flaggame.game.Stage;
import syam.flaggame.util.Actions;

/**
 * SetupManager (SetupManager.java)
 * @author syam(syamn)
 */
public class SetupManager {
	// Logger
	public static final Logger log = FlagGame.log;
	private static final String logPrefix = FlagGame.logPrefix;
	private static final String msgPrefix = FlagGame.msgPrefix;


	// 選択中のステージ
	private static Map<String, Stage> selectedStage = new HashMap<String, Stage>();
	// 選択中のブロック
	private static Map<String, Location> selectedBlock = new HashMap<String, Location>();
	// 選択中のフラッグ種類
	private static Map<String, FlagType> selectedFlagType = new HashMap<String, FlagType>();

	// ゲームマネージャモードのリスト
	//private static List<String> fgFlagManager = new ArrayList<String>();
	//private static List<String> fgChestManager = new ArrayList<String>();
	private static Map<String, Configables> managersMap = new HashMap<String, Configables>();

	/**
	 * 指定したステージを選択中にする
	 * @param player 対象プレイヤー
	 * @param game 対象ステージ
	 */
	public static void setSelectedStage(Player player, Stage stage){
		selectedStage.put(player.getName(), stage);
	}
	/**
	 * 選択中のステージを返す
	 * @param player 対象のプレイヤー
	 * @return null または対象のステージ
	 */
	public static Stage getSelectedStage(Player player){
		if (player == null || !selectedStage.containsKey(player.getName())){
			return null;
		}else{
			return selectedStage.get(player.getName());
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
