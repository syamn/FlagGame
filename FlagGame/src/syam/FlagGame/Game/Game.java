package syam.FlagGame.Game;

import java.awt.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import syam.FlagGame.FlagGame;

public class Game {
	// Logger
	public static final Logger log = FlagGame.log;
	private static final String logPrefix = FlagGame.logPrefix;
	private static final String msgPrefix = FlagGame.msgPrefix;

	private final FlagGame plugin;

	/* ゲームデータ */
	// ゲーム名
	private String gameName;

	// ゲーム開始時のフラッグデータ
	private Map<Location, GameTeam> defFlags = new HashMap<Location, GameTeam>();
	// 現在のフラッグデータ
	private Map<Location, GameTeam> nowFlags = new HashMap<Location, GameTeam>();

	// 参加プレイヤー
	private  Map<GameTeam, ArrayList<Player>> playersMap = new HashMap<GameTeam, ArrayList<Player>>();
	private ArrayList<Player> playersRed = new ArrayList<Player>();
	private ArrayList<Player> playersBlue = new ArrayList<Player>();

	/**
	 * コンストラクタ
	 * @param plugin
	 * @param name
	 */
	public Game(final FlagGame plugin, final String name){
		this.plugin = plugin;

		// ゲームデータ設定
		this.gameName = name;

		// ゲームをメインクラスに登録
		plugin.games.put(this.gameName, this);
	}

	/* getter/setter */

	/**
	 * ゲーム名を返す
	 * @return このゲームの名前
	 */
	public String getName(){
		return gameName;
	}

	/**
	 * デフォルトのフラッグブロックとそのチームを設定する
	 * @param loc 設定するブロック座標
	 * @param team 設定するGameTeam
	 */
	public void setDefFlagBlock(Location loc, GameTeam team){
		defFlags.put(loc, team);
	}
	/**
	 * デフォルトのフラッグブロックのチームを返す
	 * @param loc 調べるブロックの座標
	 * @return GameTeam または存在しない場合 null
	 */
	public GameTeam getDefFlagBlock(Location loc){
		if (defFlags.containsKey(loc)){
			return defFlags.get(loc);
		}else{
			return null;
		}
	}
	/**
	 * デフォルトフラッグブロックを削除する
	 * @param loc 削除するフラッグのブロック座標
	 */
	public void removeFlag(Location loc){
		defFlags.remove(loc);
	}
}
