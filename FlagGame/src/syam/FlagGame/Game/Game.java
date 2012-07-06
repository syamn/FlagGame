package syam.FlagGame.Game;

import java.awt.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import syam.FlagGame.Actions;
import syam.FlagGame.FlagGame;

public class Game {
	// Logger
	public static final Logger log = FlagGame.log;
	private static final String logPrefix = FlagGame.logPrefix;
	private static final String msgPrefix = FlagGame.msgPrefix;

	private final FlagGame plugin;

	/* ゲームデータ */
	private String gameName; // ゲーム名
	private boolean isReady = false; // 待機状態フラグ
	private boolean started = true; // 開始状態フラグ

	// フラッグデータ
	private Map<Location, Flag> flags = new HashMap<Location, Flag>();
	//private ArrayList<Flag> flags = new ArrayList<Flag>();

	// 参加プレイヤー
	//private Map<GameTeam, ArrayList<Player>> playersMap = new HashMap<GameTeam, ArrayList<Player>>();
	//private ArrayList<Player> playersRed = new ArrayList<Player>();
	//private ArrayList<Player> playersBlue = new ArrayList<Player>();
	private Map<GameTeam, Set<Player>> playersMap = new HashMap<GameTeam, Set<Player>>();
	private Set<Player> playersRed = new HashSet<Player>();
	private Set<Player> playersBlue = new HashSet<Player>();

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

	/**
	 * このゲームを開始待機中にする
	 */
	public void ready(){
		// 一度プレイヤーリスト初期化
		playersRed.clear();
		playersBlue.clear();
		// 再マッピング
		mappingPlayersList();
	}

	/**
	 * プレイヤーリストをマップにマッピングする
	 */
	private void mappingPlayersList(){
		// 一度クリア
		playersMap.clear();
		// マッピング
		playersMap.put(GameTeam.RED, playersRed);
		playersMap.put(GameTeam.BLUE, playersBlue);
	}

	/* チームのプレイヤー操作系 */
	/**
	 * プレイヤーリストにプレイヤーを追加する
	 * @param player 追加するプレイヤー
	 * @param team 追加するチーム
	 * @return 成功すればtrue, 失敗すればfalse
	 */
	public boolean addPlayer(Player player, GameTeam team){
		// チームの存在確認
		if (player == null || team == null || !playersMap.containsKey(team)){
			return false;
		}
		// 追加
		playersMap.get(team).add(player);
		return true;
	}
	/**
	 * プレイヤーリストからプレイヤーを削除する
	 * @param player 対象のプレイヤー
	 * @param team 対象チーム nullなら全チームから
	 * @return エラーならfalse 違えばtrue
	 */
	public boolean remPlayer(Player player, GameTeam team){
		if (player == null || (team != null && !playersMap.containsKey(team)))
			return false;

		// 削除
		if (team != null){
			playersMap.get(team).remove(player);
		}else{
			// チームがnullなら全チームから削除
			for(Set<Player> set : playersMap.values()){
				set.remove(player);
			}
		}
		return true;
	}
	/**
	 * プレイヤーが所属しているチームを返す
	 * @param player 対象のプレイヤー
	 * @return GameTeam または所属なしの場合 null
	 */
	public GameTeam getPlayerTeam(Player player){
		for(Map.Entry<GameTeam, Set<Player>> ent : playersMap.entrySet()){
			// すべてのチームセットを回す
			if(ent.getValue().contains(player)){
				return ent.getKey();
			}
		}
		// 一致なし nullを返す
		return null;
	}
	/**
	 * フラッグワールドにのみメッセージを送る
	 * @param msg メッセージ
	 */
	public void message(String msg){
		// イベントワールド全員に送る？ 全チームメンバーに送る？
		// とりあえずワールドキャストする
		Actions.worldcastMessage(Bukkit.getWorld(plugin.getConfigs().gameWorld), msg);
	}
	/**
	 * 特定チームにのみメッセージを送る
	 * @param msg メッセージ
	 * @param team 対象のチーム
	 */
	public void message(String msg, GameTeam team){
		if (team == null || !playersMap.containsKey(team))
			return;

		// チームメンバーでループさせてメッセージを送る
		for (Player player : playersMap.get(team)){
			Actions.message(null, player, msg);
		}
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
	 * 開始待機中かどうか返す
	 * @return
	 */
	public boolean isReady(){
		return isReady;
	}
	/**
	 * 開始中かどうか返す
	 * @return
	 */
	public boolean isStarting(){
		return started;
	}

	/**
	 * フラッグブロックとそのチームを設定する
	 * @param loc 設定するブロック座標
	 * @param team 設定するGameTeam
	 */
	public void setFlag(Location loc, Flag flag){
		flags.put(loc, flag);
	}
	/**
	 * フラッグブロックのフラッグを返す
	 * @param loc 調べるブロックの座標
	 * @return GameTeam または存在しない場合 null
	 */
	public Flag getFlag(Location loc){
		if (flags.containsKey(loc)){
			return flags.get(loc);
		}else{
			return null;
		}
	}
	/**
	 * フラッグブロックを削除する
	 * @param loc 削除するフラッグのブロック座標
	 */
	public void removeFlag(Location loc){
		flags.remove(loc);
	}
	/**
	 * フラッグマップを一括取得
	 * @return
	 */
	public Map<Location, Flag> getFlags(){
		return flags;
	}
	/**
	 * フラッグマップを一括設定
	 * @param flags
	 */
	public void setFlags(Map<Location, Flag> flags){
		// クリア
		this.flags.clear();
		// セット
		this.flags.putAll(flags);
	}
}
