package syam.flaggame.player;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import syam.flaggame.FlagGame;

public class PlayerManager {
	// Logger
	public static final Logger log = FlagGame.log;
	private static final String logPrefix = FlagGame.logPrefix;
	private static final String msgPrefix = FlagGame.msgPrefix;

	private static Map<String, FGPlayer> players = new HashMap<String, FGPlayer>();

	/**
	 * プレイヤーを追加します
	 * @param player 追加するプレイヤー
	 * @return プレイヤーオブジェクト {@link FGPlayer}
	 */
	public static FGPlayer addPlayer(Player player){
		FGPlayer fgPlayer = players.get(player.getName());

		if (fgPlayer != null){
			// プレイヤーオブジェクトは接続ごとに違うものなので再設定する
			fgPlayer.setPlayer(player);
		}else{
			// 新規プレイヤー
			fgPlayer = new FGPlayer(player);
			players.put(player.getName(), fgPlayer);
		}

		return fgPlayer;
	}

	/**
	 * 指定したプレイヤーをマップから削除します
	 * @param playerName 削除するプレイヤー名
	 */
	public static void remove(String playerName){
		players.remove(playerName);
	}

	/**
	 * プレイヤーマップを全削除します
	 */
	public static void clearAll(){
		players.clear();
	}

	/**
	 * 全プレイヤーデータを保存する
	 */
	public static void saveAll(){
		for (FGPlayer fgPlayer : players.values()){
			fgPlayer.getProfile().save();
		}
	}

	/**
	 * プレイヤーを取得する
	 * @param playerName 取得対象のプレイヤー名
	 * @return プレイヤー {@link FGPlayer}
	 */
	public static FGPlayer getPlayer(String playerName){
		return players.get(playerName);
	}

	/**
	 * プレイヤーを取得する
	 * @param player 取得対象のプレイヤー
	 * @return プレイヤー {@link FGPlayer}
	 */
	public static FGPlayer getPlayer(Player player){
		return getPlayer(player.getName());
	}

	/**
	 * プレイヤーのプロフィールを取得する
	 * @param player 取得対象のプレイヤー名
	 * @return プレイヤープロフィール {@link PlayerProfile}
	 */
	public static PlayerProfile getProfile(String playerName){
		FGPlayer fgPlayer = players.get(playerName);

		return (fgPlayer != null) ? fgPlayer.getProfile() : null;
	}
	/**
	 * プレイヤーのプロフィールを取得する
	 * @param player 取得対象のプレイヤー
	 * @return プレイヤープロフィール {@link PlayerProfile}
	 */
	public static PlayerProfile getProfile(OfflinePlayer player){
		return getProfile(player.getName());
	}



	/* getter / setter */
	public static Map<String, FGPlayer> getPlayers(){
		return players;
	}
}
