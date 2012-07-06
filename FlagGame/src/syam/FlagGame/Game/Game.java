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
import org.bukkit.command.CommandSender;
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
	private int teamPlayerLimit = 8; // 各チームの最大プレイヤー数
	private boolean ready = false; // 待機状態フラグ
	private boolean started = false; // 開始状態フラグ

	// フラッグデータ
	private Map<Location, Flag> flags = new HashMap<Location, Flag>();
	//private ArrayList<Flag> flags = new ArrayList<Flag>();

	// 参加プレイヤー
	private Map<GameTeam, Set<Player>> playersMap = new HashMap<GameTeam, Set<Player>>();
	private Set<Player> redPlayers = new HashSet<Player>();
	private Set<Player> bluePlayers = new HashSet<Player>();

	// スポーン地点
	private Map<GameTeam, Location> spawnMap = new HashMap<GameTeam, Location>();

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
	public void ready(CommandSender sender){
		if (started){
			Actions.message(sender, null, "&cこのゲームは既に始まっています");
			return;
		}
		if (ready){
			Actions.message(sender, null, "&cこのゲームは既に参加受付中です");
			return;
		}

		// 一度プレイヤーリスト初期化
		redPlayers.clear();
		bluePlayers.clear();
		// 再マッピング
		mappingPlayersList();

		// スポーン地点チェック
		if (spawnMap.size() != playersMap.size()){
			Actions.message(sender, null, "&cチームスポーン地点が正しく設定されていません");
			return;
		}

		// アナウンス
		Actions.broadcastMessage(msgPrefix+"&2フラッグゲーム'"+getName()+"'が参加受付を開始しました！");
		Actions.broadcastMessage(msgPrefix+"&2 '&6/flag join "+getName()+"&2' コマンドで参加してください");

		// 待機
		ready = true;
	}

	/**
	 * ゲームを開始する
	 */
	public void start(CommandSender sender){
		if (started){
			Actions.message(sender, null, "&cこのゲームは既に始まっています");
			return;
		}

		// チームの人数チェック
		if (redPlayers.size() != bluePlayers.size()){
			Actions.message(sender, null, "&c各チームのプレイヤー数が同じになるまでお待ちください");
			return;
		}
		if (redPlayers.size() <= 0){
			Actions.message(sender, null, "&c参加しているプレイヤーがいないチームがあります");
			return;
		}

		// スポーン地点の再チェック
		if (spawnMap.size() != playersMap.size()){
			Actions.message(sender, null, "&cチームスポーン地点が正しく設定されていません");
			return;
		}
		// 参加プレイヤーをスポーン地点に移動させる
		tpSpawnLocation();

		// 開始
		started = true;
		//TODO: タイマー稼働
	}

	/**
	 * プレイヤーリストをマップにマッピングする
	 */
	private void mappingPlayersList(){
		// 一度クリア
		playersMap.clear();
		// マッピング
		playersMap.put(GameTeam.RED, redPlayers);
		playersMap.put(GameTeam.BLUE, bluePlayers);
	}

	/* チームのプレイヤー操作系 */
	/**
	 * プレイヤーを少ないチームに自動で参加させる 同じなら赤チーム
	 * @param player 参加させるプレイヤー
	 * @return
	 */
	public boolean addPlayer(Player player){
		// 赤チームのが少ないか、または同じなら赤チームに追加 それ以外は青チームに追加
		if (redPlayers.size() <= bluePlayers.size()){
			return addPlayer(player, GameTeam.RED);
		}else{
			return addPlayer(player, GameTeam.BLUE);
		}
	}
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
		// 人数チェック
		if (playersMap.get(team).size() >= teamPlayerLimit){
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

	/**
	 * 参加プレイヤーをスポーン地点へ移動させる
	 */
	public void tpSpawnLocation(){
		// 参加プレイヤーマップを回す
		for (Map.Entry<GameTeam, Set<Player>> entry : playersMap.entrySet()){
			GameTeam team = entry.getKey();
			Location loc = getSpawn(team);
			// チームのスポーン地点が未設定の場合何もしない
			if (loc == null) continue;
			// チームの全プレイヤー(null/オフラインを除く)をスポーン地点にテレポート
			for (Player player : entry.getValue()){
				if (player != null && player.isOnline())
					player.teleport(loc);
			}
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
	 * チームごとのプレイヤー数上限を返す
	 * @return teamPlayerLimit
	 */
	public int getTeamPlayerLimit(){
		return teamPlayerLimit;
	}
	/**
	 * 開始待機中かどうか返す
	 * @return
	 */
	public boolean isReady(){
		return ready;
	}
	/**
	 * 開始中かどうか返す
	 * @return
	 */
	public boolean isStarting(){
		return started;
	}

	/**
	 * プレイヤーマップを返す
	 * @return
	 */
	public Map<GameTeam, Set<Player>> getPlayersMap(){
		return playersMap;
	}

	/**
	 * 指定したチームのプレイヤーセットを返す
	 * @param team 取得するチーム
	 * @return プレイヤーセット またはnull
	 */
	public Set<Player> getPlayersSet(GameTeam team){
		if (team == null || !playersMap.containsKey(team))
			return null;

		return playersMap.get(team);
	}

	/**
	 * チームのスポーン地点を設置/取得する
	 * @param loc
	 */
	public void setSpawn(GameTeam team, Location loc){
		spawnMap.put(team, loc);
	}
	public Location getSpawn(GameTeam team){
		if (team == null || !spawnMap.containsKey(team))
			return null;
		return spawnMap.get(team);
	}
	public Map<GameTeam, Location> getSpawns(){
		return spawnMap;
	}
	public void setSpawns(Map<GameTeam, Location> spawns){
		// クリア
		this.spawnMap.clear();
		// セット
		this.spawnMap.putAll(spawns);
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
