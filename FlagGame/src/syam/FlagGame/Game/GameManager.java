package syam.FlagGame.Game;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import syam.FlagGame.FlagGame;

public class GameManager {
	// Logger
	public static final Logger log = FlagGame.log;
	private static final String logPrefix = FlagGame.logPrefix;
	private static final String msgPrefix = FlagGame.msgPrefix;

	// Files
	private FileConfiguration confFile = new YamlConfiguration();
	private File file;

	private final FlagGame plugin;
	public GameManager(final FlagGame plugin){
		this.plugin = plugin;
	}

	// 選択中のゲーム
	private static Map<String, Game> selectedGame = new HashMap<String, Game>();
	// 選択中のチーム
	private static Map<String, GameTeam> selectedTeam = new HashMap<String, GameTeam>();
	// 選択中のブロック
	private static Map<String, Location> selectedBlock = new HashMap<String, Location>();

	// ゲームマネージャモードのリスト
	private static List<String> fgManager = new ArrayList<String>();


	/* ゲームデータ保存/読み出し */
	public void saveGames(){
		String fileDir = plugin.getDataFolder() + System.getProperty("file.separator") +
				"gameData" + System.getProperty("file.separator");

		for (Game game : plugin.games.values()){
			this.file = new File(fileDir + game.getName() + ".yml");

			// フラッグデータ変換
			List<String> flagList = convertFlagMap(game.getFlags());

			// 保存するデータをここに
			confFile.set("GameName", game.getName());
			confFile.set("Flags", flagList);

			try {
				confFile.save(file);
			} catch (IOException ex) {
				log.warning(logPrefix+ "Couldn't write Game data!");
				ex.printStackTrace();
			}
		}
	}

	/**
	 * ハッシュマップからリストに変換
	 * @param flags フラッグマップ
	 * @return フラッグ情報文字列のリスト
	 */
	private List<String> convertFlagMap(Map<Location, Flag> flags){
		List<String> ret = new ArrayList<String>();
		ret.clear();

		for (Flag flag : flags.values()){
			// 331,41,213@IRON@44:3 みたいな感じに
			// → GOLD@44:3@331,41,213に修正
			String s = flag.getFlagType().name() + "@";
			s = s + flag.getOriginBlockID() + ":" + flag.getOriginBlockData() + "@";

			Location loc = flag.getLocation();
			s = s + loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ();


			// フラッグ追加
			ret.add(s);
		}

		return ret;
	}

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
	 * 指定したチームを選択中にする
	 * @param player プレイヤー
	 * @param team 対象チーム
	 */
	public static void setSelectedTeam(Player player, GameTeam team){
		selectedTeam.put(player.getName(), team);
	}
	/**
	 * 選択中のチームを返す
	 * @param player 対象プレイヤー
	 * @return null または対象チーム
	 */
	public static GameTeam getSelectedTeam(Player player){
		if (player == null || !selectedTeam.containsKey(player.getName())){
			return null;
		}else{
			return selectedTeam.get(player.getName());
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
	 * プレイヤーをマネージモードにする/しない
	 * @param player 対象のプレイヤー
	 * @param state true = 管理モードにする/false = しない
	 */
	public static void setManager(Player player, boolean state){
		if (state){
			if (!fgManager.contains(player.getName()))
				fgManager.add(player.getName());
		}else{
			if (fgManager.contains(player.getName()))
				fgManager.remove(player.getName());
		}

	}
	/**
	 * プレイヤーがゲームマネージモードかどうか返す
	 * @param player チェックするプレイヤー
	 * @return trueなら管理モード、falseなら管理モードでない
	 */
	public static boolean isManager(Player player){
		if(player != null && fgManager.contains(player.getName())){
			return true;
		}else{
			return false;
		}
	}
}
