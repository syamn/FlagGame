package syam.FlagGame.Game;

import java.awt.List;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.omg.CORBA.PUBLIC_MEMBER;

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
	private int teamPlayerLimit = 2; // 各チームの最大プレイヤー数
	private int gameTimeInSeconds = 61; // 1ゲームの制限時間
	private int remainSec = gameTimeInSeconds; // 1ゲームの制限時間
	private int timerThreadID = -1; // タイマータスクのID
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

		// 待機
		ready = true;

		// アナウンス
		Actions.broadcastMessage(msgPrefix+"&2フラッグゲーム'&6"+getName()+"&2'の参加受付が開始されました！");
		Actions.broadcastMessage(msgPrefix+"&2 '&6/flag join "+getName()+"&2' コマンドで参加してください！");
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
		/*
		if (redPlayers.size() != bluePlayers.size()){
			Actions.message(sender, null, "&c各チームのプレイヤー数が同じになるまでお待ちください");
			return;
		}
		*/
		if (redPlayers.size() <= 0){
			Actions.message(sender, null, "&cプレイヤーが参加していないチームがあります");
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
		timer(); // タイマースタート
		started = true;

		// アナウンス
		Actions.broadcastMessage(msgPrefix+"&2フラッグゲーム'&6"+getName()+"&2'が始まりました！");
		Actions.broadcastMessage(msgPrefix+"&f &a制限時間: &f"+gameTimeInSeconds+"&a秒&f | &b青チーム: &f"+bluePlayers.size()+"&b人&f | &c赤チーム: &f"+redPlayers.size()+"&c人");
	}
	/**
	 * タイマー終了によって呼ばれるゲーム終了処理
	 */
	private void finish(){
		// ポイントチェック
		int redP = 0, blueP = 0, noneP = 0;
		String redS = "", blueS = "", noneS = "";
		Map<FlagState, HashMap<FlagType, Integer>> pointsMap = checkFlag();
		// 赤チームチェック
		if (pointsMap.containsKey(FlagState.RED)){
			HashMap<FlagType, Integer> points = pointsMap.get(FlagState.RED);
			for (Map.Entry<FlagType, Integer> entry : points.entrySet()){
				FlagType ft = entry.getKey();
				// 総得点に加算
				redP = redP + (ft.getPoint() * entry.getValue());
				// 文章組み立て
				redS = redS + ft.getColor() + entry.getKey().getTypeName() + "フラッグ: &f" + entry.getValue()+" | ";
			}
			redS = redS.substring(0, redS.length() - 3);
		}
		// 青チームチェック
		if (pointsMap.containsKey(FlagState.BLUE)){
			HashMap<FlagType, Integer> points = pointsMap.get(FlagState.BLUE);
			for (Map.Entry<FlagType, Integer> entry : points.entrySet()){
				FlagType ft = entry.getKey();
				// 総得点に加算
				blueP = blueP + (ft.getPoint() * entry.getValue());
				// 文章組み立て
				blueS = blueS + ft.getColor() + entry.getKey().getTypeName() + "フラッグ: &f" + entry.getValue()+" | ";
			}
			blueS = blueS.substring(0, blueS.length() - 3);
		}
		// NONEチームチェック
		if (pointsMap.containsKey(FlagState.NONE)){
			HashMap<FlagType, Integer> points = pointsMap.get(FlagState.NONE);
			for (Map.Entry<FlagType, Integer> entry : points.entrySet()){
				FlagType ft = entry.getKey();
				// 総得点に加算
				noneP = noneP + (ft.getPoint() * entry.getValue());
				// 文章組み立て
				noneS = noneS + ft.getColor() + entry.getKey().getTypeName() + "フラッグ: &f" + entry.getValue()+" | ";
			}
			noneS = noneS.substring(0, noneS.length() - 3);
		}

		// 勝敗判定
		GameTeam winTeam = null;
		if (redP > blueP)
			winTeam = GameTeam.RED;
		else if(blueP > redP)
			winTeam = GameTeam.BLUE;


		// アナウンス
		Actions.broadcastMessage(msgPrefix+"&2フラッグゲーム'&6"+getName()+"&2'が終わりました！");
		if (redS != "")
			Actions.broadcastMessage("&c赤チーム得点: &6"+redP+"&f - "+redS);
		if (blueS != "")
			Actions.broadcastMessage("&b青チーム得点: &6"+blueP+"&f - "+blueS);
		if (noneS != "")
			Actions.broadcastMessage("&7無効フラッグ: &6"+noneP+"&f - "+noneS);
		Actions.broadcastMessage(msgPrefix+winTeam.getColor()+winTeam.getTeamName()+"チーム の勝利です！ &7(&c"+redP+"&7 - &b"+blueP+"&7)");

		// フラグ初期化
		ready = false;
		started = false;
	}

	private Map<FlagState, HashMap<FlagType, Integer>> checkFlag(){
		// 各チームのポイントを格納する
		Map<FlagState, HashMap<FlagType, Integer>> ret = new HashMap<FlagState, HashMap<FlagType, Integer>>();

		// 全フラッグを回す
		flag:
		for (Flag flag : flags.values()){
			Block block = flag.getNowBlock(); // フラッグ座標のブロックを取得
			FlagType ft = flag.getFlagType();
			FlagState state = null; // フラッグの現在状態
			int i = 0; // 加算後と加算後の得点

			// 全チームを回す
			for (GameTeam gt : GameTeam.values()){

				// チームのフラッグデータと一致すればそのチームにカウント
				if (gt.getBlockID() == block.getTypeId() && gt.getBlockData() == block.getData()){
					state = gt.getFlagState();

					// get - FlagType, Integer
					HashMap<FlagType, Integer> hm = ret.get(state);
					if (hm == null){
						hm = new HashMap<FlagType, Integer>();
					}

					// get Integer
					if (hm.containsKey(ft)){
						i = hm.get(ft);
					}

					// 個数加算
					i++;

					// put
					hm.put(ft, i); // num
					ret.put(state, hm); // state

					// 先に進まないように
					continue flag;
				}
			}
			// 一致しなかった、どちらのブロックでもない場合
			state = FlagState.NONE;
			HashMap<FlagType, Integer> hm = ret.get(state);
			if (hm == null) hm = new HashMap<FlagType, Integer>(); // get
			if (hm.containsKey(ft)) i = hm.get(ft); // get
			i++; // 個数加算
			hm.put(ft, i); // put
			ret.put(state, hm); // put
		}

		return ret;
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
	 * ゲーム参加者全員にメッセージを送る
	 * @param msg メッセージ
	 */
	public void message(String msg){
		// イベントワールド全員に送る？ 全チームメンバーに送る？
		// とりあえずワールドキャストする → ワールドキャストの場合同時進行が行えない
		//Actions.worldcastMessage(Bukkit.getWorld(plugin.getConfigs().gameWorld), msg);

		// 全チームメンバーにメッセージを送る
		for (Set<Player> set : playersMap.values()){
			for (Player player : set){
				if (player != null && player.isOnline())
					Actions.message(null, player, msg);
			}
		}
	}
	/**
	 * 特定チームにのみメッセージを送る
	 * @param msg メッセージ
	 * @param team 対象のチーム
	 */
	public void message(GameTeam team, String msg){
		if (team == null || !playersMap.containsKey(team))
			return;

		// チームメンバーでループさせてメッセージを送る
		for (Player player : playersMap.get(team)){
			if (player != null && player.isOnline())
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

	/* timer */
	/**
	 * メインのタイマータスクを開始する
	 */
	public void timer(){
		// タイマータスク
		timerThreadID = plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable() {
			public void run(){
				/* 1秒ごとに呼ばれる */

				// 残り時間がゼロになった
				if (remainSec <= 0){
					cancelTimerTask(); // タイマー停止
					finish(); // ゲーム終了
					return;
				}

				// 15秒以下
				if (remainSec <= 15){
					message(msgPrefix+ "&aゲーム終了まで あと "+remainSec+" 秒です！");
				}
				// 30秒前
				else if (remainSec == 30){
					message(msgPrefix+ "&aゲーム終了まで あと "+remainSec+" 秒です！");
				}
				// 60秒間隔
				else if ((remainSec % 60) == 0){
					int remainMin = remainSec / 60;
					message(msgPrefix+ "&aゲーム終了まで あと "+remainMin+" 分です！");
				}

				remainSec--;
			}
		}, 0L, 20L);
	}

	/**
	 * タイマータスクが稼働中の場合停止する
	 */
	private void cancelTimerTask(){
		if (started && timerThreadID != -1){
			// タスクキャンセル
			plugin.getServer().getScheduler().cancelTask(timerThreadID);
		}
	}

	/**
	 * このゲームの残り時間(秒)を取得する
	 * @return 残り時間(秒)
	 */
	public int getRemainTime(){
		return remainSec;
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
