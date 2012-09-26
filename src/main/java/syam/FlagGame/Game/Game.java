/**
 * FlagGame - Package: syam.FlagGame.Game
 * Created: 2012/09/22 2:34:55
 */
package syam.FlagGame.Game;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import syam.FlagGame.FlagGame;
import syam.FlagGame.Api.IGame;
import syam.FlagGame.Enum.FlagState;
import syam.FlagGame.Enum.FlagType;
import syam.FlagGame.Enum.GameResult;
import syam.FlagGame.Enum.GameTeam;
import syam.FlagGame.Exceptions.GameStateException;
import syam.FlagGame.FGPlayer.PlayerManager;
import syam.FlagGame.FGPlayer.PlayerProfile;
import syam.FlagGame.Util.Actions;

/**
 * Game (Game.java)
 * @author syam(syamn)
 */
public class Game implements IGame{
	// Logger
	public static final Logger log = FlagGame.log;
	private static final String logPrefix = FlagGame.logPrefix;
	private static final String msgPrefix = FlagGame.msgPrefix;

	// プラグインインスタンス
	private final FlagGame plugin;

	private String GameID; // 一意なゲームID ログ用
	private Stage stage;

	private int remainSec; // 1ゲームの制限時間
	private int timerThreadID = -1; // タイマータスクのID
	private int starttimerInSec = 10;
	private int starttimerThreadID = -1;
	private boolean ready = false; // 待機状態フラグ
	private boolean started = false; // 開始状態フラグ

	// 参加プレイヤー
	private Map<GameTeam, Set<String>> playersMap = new ConcurrentHashMap<GameTeam, Set<String>>();
	private Set<String> redPlayers = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
	private Set<String> bluePlayers = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

	// Kill/Death記録
	private Map<GameTeam, Integer> teamKilledCount = new ConcurrentHashMap<GameTeam, Integer>();

	/**
	 * コンストラクタ
	 * @param plugin
	 * @param stage
	 */
	public Game(final FlagGame plugin, final Stage stage){
		this.plugin = plugin;
		this.stage = stage;

		// 例外チェック
		if (!stage.isAvailable()){
			log.severe(logPrefix+ "Stage " + stage.getName() + " is not available!");
			return;
		}
		if (GameManager.getGames().containsKey(stage)){
			log.severe(logPrefix+ "Stage " + stage.getName() + " is duplicate!");
			return;
		}

		this.remainSec = stage.getGameTime();

		// ゲームマネージャにゲーム登録
		GameManager.addGame(stage.getName(), this);
	}

	/**
	 * このゲームを開始待機中にする
	 */
	public void ready(CommandSender sender, boolean random){
		if (started || ready){
			throw new GameStateException("This game is already using!");
		}

		// 一度プレイヤーリスト初期化
		redPlayers.clear();
		bluePlayers.clear();
		// 再マッピング
		mappingPlayersList();

		// ステージエリアチェック
		if (stage.getStage() == null){
			throw new GameStateException("Stage area is not defined properly!");
		}

		// スポーン地点チェック
		if (stage.getSpawns().size() != 2){
			throw new GameStateException("Team spawn area is not defined properly!");
		}

		// 保護チェック
		if (!stage.isStageProtected()){
			stage.setStageProtected(true);
		}

		// 待機
		ready = true;
		stage.setUsing(true);
		stage.setGame(this);

		// 賞金系メッセージ
		String entryFeeMsg = String.valueOf(stage.getEntryFee()) + "Coin";
		String awardMsg = String.valueOf(stage.getAward()) +"Coin";
		if (stage.getEntryFee() <= 0)
			entryFeeMsg = "&7FREE!";
		if (stage.getAward() <= 0)
			awardMsg = "&7なし";

		// アナウンス
		Actions.broadcastMessage(msgPrefix+"&2フラッグゲーム'&6"+stage.getName()+"&2'の参加受付が開始されました！");
		Actions.broadcastMessage(msgPrefix+"&2 参加料:&6 "+entryFeeMsg+ "&2   賞金:&6 "+awardMsg);
		Actions.broadcastMessage(msgPrefix+"&2 '&6/flag join "+stage.getName()+"&2' コマンドで参加してください！");

		// ロギング
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd-HHmmss");
		this.GameID = stage.getName()+"_"+sdf.format(new Date());

		log("========================================");
		log("Sender "+sender.getName()+" Ready to Game");
		log("Stage: "+stage.getName()+ " ("+stage.getFileName()+")");
		log("TeamPlayerLimit: "+stage.getTeamLimit()+" GameTime: "+stage.getGameTime()+" sec");
		log("Award: "+stage.getAward()+" EntryFee:"+stage.getEntryFee());
		log("========================================");
	}

	public void ready(CommandSender sender) {
		this.ready(sender, false);
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
		if (redPlayers.size() <= 0 || bluePlayers.size() <= 0){
			Actions.message(sender, null, "&cプレイヤーが参加していないチームがあります");
			return;
		}

		// ステージエリアチェック
		if (stage.getStage() == null){
			Actions.message(sender, null, "&cステージエリアが正しく設定されていません");
			return;
		}

		// スポーン地点の再チェック
		if (stage.getSpawns().size() != 2){
			Actions.message(sender, null, "&cチームスポーン地点が正しく設定されていません");
			return;
		}

		// フラッグブロックとチェストをロールバックする
		stage.rollbackFlags();
		stage.rollbackChests();

		// 参加プレイヤーをスポーン地点に移動させる
		tpSpawnLocation();

		// チャンクロード
		//getSpawnLocation(GameTeam.RED).getChunk().load();
		//getSpawnLocation(GameTeam.BLUE).getChunk().load();

		// 開始
		timer(); // タイマースタート
		ready = false;
		started = true;

		// プロファイル更新
		stage.getProfile().updateLastPlayedStage();
		stage.getProfile().addPlayed();

		// アナウンス
		Actions.broadcastMessage(msgPrefix+"&2フラッグゲーム'&6"+stage.getName()+"&2'が始まりました！");
		Actions.broadcastMessage(msgPrefix+"&f &a制限時間: &f"+Actions.getTimeString(stage.getGameTime())+"&f | &b青チーム: &f"+bluePlayers.size()+"&b人&f | &c赤チーム: &f"+redPlayers.size()+"&c人");
		if (stage.getSpecSpawn() != null){
			Actions.broadcastMessage(msgPrefix+"&2 '&6/flag watch "+stage.getName()+"&2' コマンドで観戦することができます！");
		}

		// 試合に参加する全プレイヤーを回す
		for (Map.Entry<GameTeam, Set<String>> entry : playersMap.entrySet()){
			GameTeam team = entry.getKey();
			for (String name : entry.getValue()){
				Player player = Bukkit.getPlayer(name);
				// オフラインプレイヤーをスキップ
				if (player == null || !player.isOnline())
					continue;

				// ゲームモード強制変更
				player.setGameMode(GameMode.SURVIVAL);

				// アイテムクリア
				player.getInventory().clear();
				// 頭だけ羊毛に変える
				player.getInventory().setHelmet(new ItemStack(team.getBlockID(), 1, (short)0, team.getBlockData()));
				//player.getInventory().setHelmet(null);
				player.getInventory().setChestplate(null);
				player.getInventory().setLeggings(null);
				player.getInventory().setBoots(null);

				// 回復させる
				player.setHealth(20);
				player.setFoodLevel(20);

				// 効果のあるポーションをすべて消す
				if (player.hasPotionEffect(PotionEffectType.JUMP))
					player.removePotionEffect(PotionEffectType.JUMP);
				if (player.hasPotionEffect(PotionEffectType.SPEED))
					player.removePotionEffect(PotionEffectType.SPEED);
				if (player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE))
					player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
				if (player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE))
					player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
				if (player.hasPotionEffect(PotionEffectType.BLINDNESS))
					player.removePotionEffect(PotionEffectType.BLINDNESS);
				if (player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE))
					player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);

				// 参加カウント追加
				PlayerManager.getProfile(player.getName()).updateLastJoinedGame();
				PlayerManager.getProfile(player.getName()).addPlayed();

				// メッセージ通知
				Actions.message(null, player, msgPrefix+ "&a *** "+team.getColor()+"あなたは "+team.getTeamName()+"チーム です！ &a***");
			}
		}
		String blue = "", red = "";
		for (String name : bluePlayers){
			blue = blue + name + ", ";
		}
		for (String name : redPlayers){
			red = red + name + ", ";
		}
		if (blue != "") blue = blue.substring(0, blue.length() - 2);
		if (red != "") red = red.substring(0, red.length() - 2);

		log("========================================");
		log("Sender "+sender.getName()+" Start Game");
		log("RedTeam("+redPlayers.size()+"): "+red);
		log("BlueTeam("+bluePlayers.size()+"): "+blue);
		log("========================================");
	}

	/**
	 * タイマー終了によって呼ばれるゲーム終了処理
	 */
	public void finish(){
		// ポイントチェック
		int redP = 0, blueP = 0, noneP = 0;
		String redS = "", blueS = "", noneS = "";
		Map<FlagState, HashMap<FlagType, Integer>> pointsMap = stage.checkFlag();
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
		GameTeam loseTeam = null;
		if (redP > blueP){
			winTeam = GameTeam.RED;
			loseTeam = GameTeam.BLUE;
		}else if(blueP > redP){
			winTeam = GameTeam.BLUE;
			loseTeam = GameTeam.RED;
		}

		// アナウンス
		Actions.broadcastMessage(msgPrefix+"&2フラッグゲーム'&6"+stage.getName()+"&2'が終わりました！");
		if (redS != "")
			Actions.broadcastMessage("&c赤チーム: &6"+redP+"&c点&f ("+redS+"&f)");
		if (blueS != "")
			Actions.broadcastMessage("&b青チーム得点: &6"+blueP+"&b点&f ("+blueS+"&f)");
		if (noneS != "")
			Actions.broadcastMessage("&7無効フラッグ: &6"+noneP+"&7点&f ("+noneS+"&f)");
		if (winTeam != null){
			Actions.broadcastMessage(msgPrefix+winTeam.getColor()+winTeam.getTeamName()+"チーム の勝利です！ &7(&c"+redP+"&7 - &b"+blueP+"&7)");
		}else{
			Actions.broadcastMessage(msgPrefix+"&6このゲームは引き分けです！ &7(&c"+redP+"&7 - &b"+blueP+"&7)");
		}
		Actions.broadcastMessage("&c赤チームKill数: &6"+getKillCount(GameTeam.RED)+"&b 青チームKill数: &6"+getKillCount(GameTeam.BLUE));

		log("========================================");

		// 賞金支払い
		if (winTeam != null && stage.getAward() > 0){
			for (String name : playersMap.get(winTeam)){
				Player player = Bukkit.getPlayer(name);
				if (player != null && player.isOnline()){
					// 入金
					if (Actions.addMoney(name, stage.getAward())){
						Actions.message(null, player, "&a[+]おめでとうございます！賞金として"+stage.getAward()+"Coinを得ました！");
						log("+ Player "+name+" received "+stage.getAward()+ "Coin!");
					}else{
						Actions.message(null, player, "&c報酬受け取りにエラーが発生しました。管理人までご連絡ください。");
						log("* [Error] Player "+name+" received "+stage.getAward()+ "Coin ?");
					}
				}
			}
		}

		// カウント
		if (winTeam != null)
			addPlayerResultCounts(GameResult.TEAM_WIN, winTeam);
		else
			addPlayerResultCounts(GameResult.DRAW, null);

		// Logging
		log("========================================");
		log(" * FlagGame Finished");
		log(" RedTeam: "+redP+" point - "+redS);
		log("BlueTeam: "+blueP+" point - "+blueS);
		log(" Invalid: "+noneP+" point - "+noneS);
		log("========================================");
		if (winTeam == null){
			log(" *** DRAW GAME *** ("+redP+" - "+blueP+")");
		}else{
			log(" *** WIN TEAM: "+winTeam.name()+" *** ("+redP+" - "+blueP+")");
		}
		log("========================================");

		// ログの終わり
		GameID = null;

		// 参加プレイヤーをスポーン地点に移動させる
		tpSpawnLocation();

		// 同じゲーム参加者のインベントリをクリア
		for (Set<String> names : playersMap.values()){
			for (String name : names){
				Player player = Bukkit.getPlayer(name);
				// オンラインチェック
				if (player != null && player.isOnline()){
					// アイテムクリア
					player.getInventory().clear();
					player.getInventory().setHelmet(null);
					player.getInventory().setChestplate(null);
					player.getInventory().setLeggings(null);
					player.getInventory().setBoots(null);
				}
			}
		}

		// フラッグブロックロールバック 終了時はロールバックしない
		//rollbackFlags();
		// 初期化
		//init();
	}

	/**
	 * 結果を指定してゲームを終了する
	 * @param result 結果
	 * @param winTeam GameResult.TEAM_WIN の場合の勝利チーム
	 */
	public void finish(GameResult result, GameTeam winTeam, String reason){
		if (result == null || (result == GameResult.TEAM_WIN && winTeam == null)){
			log.warning(logPrefix + "Error on method finish(GameResult, GameTeam)! Please report this!");
			return;
		}

		Actions.broadcastMessage(msgPrefix+"&2フラッグゲーム'&6"+stage.getName()+"&2'は中断されました");

		// 指定した結果で追加処理
		switch(result){
			case TEAM_WIN:
				Actions.broadcastMessage(msgPrefix+"&2このゲームは"+winTeam.getColor()+winTeam.getTeamName()+"チーム&2の勝ちになりました");
				addPlayerResultCounts(GameResult.TEAM_WIN, winTeam);
				break;
			case DRAW:
				Actions.broadcastMessage(msgPrefix+"&2このゲームは引き分けになりました");
				addPlayerResultCounts(GameResult.DRAW, null);
				break;
			case STOP:
				Actions.broadcastMessage(msgPrefix+"&2このゲームは&c無効&2になりました");
				addPlayerResultCounts(GameResult.STOP, null);
				break;
			default:
				log.warning(logPrefix+ "Undefined GameResult! Please report this!");
				return;
		}

		if (reason != null && reason != ""){
			Actions.broadcastMessage(msgPrefix+"&2理由: "+reason);
		}


		// Logging
		log("========================================");
		log(" * FlagGame Finished (Manually)");
		log(" Result: "+result.name());
		if (result == GameResult.TEAM_WIN){
			log("WinTeam: "+winTeam.name());
		}
		log(" Reason: "+reason);
		log("========================================");

		// ログの終わり
		GameID = null;

		// 参加プレイヤーをスポーン地点に移動させる
		tpSpawnLocation();
		// 同じゲーム参加者のインベントリをクリア
		for (Set<String> names : playersMap.values()){
			for (String name : names){
				Player player = Bukkit.getPlayer(name);
				// オンラインチェック
				if (player != null && player.isOnline()){
					// アイテムクリア
					player.getInventory().clear();
					player.getInventory().setHelmet(null);
					player.getInventory().setChestplate(null);
					player.getInventory().setLeggings(null);
					player.getInventory().setBoots(null);
				}
			}
		}

		// 初期化
		//init();
	}

	/**
	 * プレイヤーのプロフィールのゲーム成績を更新する
	 * @param result 結果
	 * @param winTeam 勝利チーム
	 */
	private void addPlayerResultCounts(GameResult result, GameTeam winTeam){
		// オフラインプレイヤーリスト
		List<String> offlines = new ArrayList<String>();
		offlines.clear();

		if (result == GameResult.STOP){
			for (Set<String> names : playersMap.values()){
				for (String name : names){
					PlayerProfile prof = PlayerManager.getProfile(name);
					prof.setPlayed(prof.getPlayed() - 1);
				}
			}
			return;
		}
		else if (result == GameResult.DRAW){
			// 引き分け
			for (Map.Entry<GameTeam, Set<String>> entry : playersMap.entrySet()){
				for (String name : entry.getValue()){
					Player player = Bukkit.getPlayer(name);
					if (player != null && player.isOnline()){
						PlayerManager.getProfile(name).addDraw(); // draw++
					}else{
						offlines.add(name);
					}
				}
			}
		}
		else if (result == GameResult.TEAM_WIN){
			// Set Lose team
			GameTeam loseTeam = null;
			if (winTeam.equals(GameTeam.RED))
				loseTeam = GameTeam.BLUE;
			else if (winTeam.equals(GameTeam.BLUE))
				loseTeam = GameTeam.RED;

			// Win team
			for (String name : playersMap.get(winTeam)){
				Player player = Bukkit.getPlayer(name);
				if (player != null && player.isOnline()){
					PlayerManager.getProfile(name).addWin(); // win++
				}else{
					offlines.add(name);
				}
			}

			// Lose team
			if (loseTeam != null){
				for (String name : playersMap.get(loseTeam)){
					Player player = Bukkit.getPlayer(name);
					if (player != null && player.isOnline()){
						PlayerManager.getProfile(name).addLose(); // win++
					}else{
						offlines.add(name);
					}
				}
			}
		}

		// オフラインユーザーは途中退場カウント追加
		for (String name : offlines){
			PlayerManager.getProfile(name).addExit(); // exit++
		}
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

	/* ***** 参加プレイヤー関係 ***** */

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
		if (playersMap.get(team).size() >= stage.getTeamLimit()){
			return false;
		}
		// 追加
		playersMap.get(team).add(player.getName());
		log("+ Player "+player.getName()+" joined "+team.name()+" Team!");
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
			playersMap.get(team).remove(player.getName());
		}else{
			// チームがnullなら全チームから削除
			for(Set<String> set : playersMap.values()){
				set.remove(player.getName());
			}
		}
		return true;
	}
	public boolean remPlayer(String playerName) {
		// 削除
		for(Set<String> set : playersMap.values()){
			set.remove(playerName);
		}
		return true;
	}

	public boolean remPlayer(Player player) {
		if (player == null) return false;
		return this.remPlayer(player.getName());
	}
	/**
	 * プレイヤーが所属しているチームを返す
	 * @param player 対象のプレイヤー
	 * @return GameTeam または所属なしの場合 null
	 */
	public GameTeam getPlayerTeam(Player player){
		String name = player.getName();
		for(Map.Entry<GameTeam, Set<String>> ent : playersMap.entrySet()){
			// すべてのチームセットを回す
			if(ent.getValue().contains(name)){
				return ent.getKey();
			}
		}
		// 一致なし nullを返す
		return null;
	}
	/**
	 * プレイヤーマップを返す
	 * @return
	 */
	public Map<GameTeam, Set<String>> getPlayersMap(){
		return playersMap;
	}


	public boolean isJoined(String playerName) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	public boolean isJoined(Player player) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	/* ***** 参加しているプレイヤーへのアクション関係 ***** */

	/**
	 * ゲーム参加者全員にメッセージを送る
	 * @param msg メッセージ
	 */
	public void message(String message){
		// イベントワールド全員に送る？ 全チームメンバーに送る？
		// とりあえずワールドキャストする → ワールドキャストの場合同時進行が行えない
		//Actions.worldcastMessage(Bukkit.getWorld(plugin.getConfigs().gameWorld), msg);

		// 全チームメンバーにメッセージを送る
		for (Set<String> set : playersMap.values()){
			for (String name : set){
				if (name == null) continue;
				Player player = Bukkit.getPlayer(name);
				if (player != null && player.isOnline())
					Actions.message(null, player, message);
			}
		}
	}
	/**
	 * 特定チームにのみメッセージを送る
	 * @param msg メッセージ
	 * @param team 対象のチーム
	 */
	public void message(GameTeam team, String message){
		if (team == null || !playersMap.containsKey(team))
			return;

		// チームメンバーでループさせてメッセージを送る
		for (String name : playersMap.get(team)){
			if (name == null) continue;
			Player player = Bukkit.getServer().getPlayer(name);
			if (player != null && player.isOnline())
				Actions.message(null, player, message);
		}
	}
	/**
	 * 参加プレイヤーをスポーン地点へ移動させる
	 */
	public void tpSpawnLocation(){
		// 参加プレイヤーマップを回す
		for (Map.Entry<GameTeam, Set<String>> entry : playersMap.entrySet()){
			GameTeam team = entry.getKey();
			Location loc = stage.getSpawn(team);
			// チームのスポーン地点が未設定の場合何もしない
			if (loc == null) continue;

			// チームの全プレイヤーをスポーン地点にテレポート
			for (String name : entry.getValue()){
				if (name == null) continue;
				final Player player = Bukkit.getPlayer(name);
				if (player != null && player.isOnline()){
					// 現在地点が別ワールドならプレイヤーデータに戻る地点を書き込む
					if (!player.getWorld().equals(loc.getWorld())){
						PlayerManager.getProfile(player.getName()).setTpBackLocation(player.getLocation());
					}

					player.teleport(loc, TeleportCause.PLUGIN);
				}
			}
		}
	}
	/**
	 * 指定したチームのプレイヤーセットを返す
	 * @param team 取得するチーム
	 * @return プレイヤーセット またはnull
	 */
	public Set<String> getPlayersSet(GameTeam team){
		if (team == null || !playersMap.containsKey(team))
			return null;

		return playersMap.get(team);
	}

	/* ***** Kill/Death関係 ***** */
	public void addKillCount(GameTeam team){
		if (!teamKilledCount.containsKey(team)){
			teamKilledCount.put(team, 1);
		}else{
			teamKilledCount.put(team, teamKilledCount.get(team) + 1);
		}
	}
	public int getKillCount(GameTeam team){
		if (!teamKilledCount.containsKey(team)){
			return 0;
		}else{
			return teamKilledCount.get(team);
		}
	}


	/* ***** タイマー関係 ***** */

	/**
	 * 開始時のカウントダウンタイマータスクを開始する
	 */
	public void start_timer(final CommandSender sender){
		// カウントダウン秒をリセット
		starttimerInSec = plugin.getConfigs().getStartCountdownInSec();
		if (starttimerInSec <= 0){
			start(sender);
			return;
		}

		Actions.broadcastMessage(msgPrefix+"&2まもなくゲーム'&6"+stage.getName()+"'&2が始まります！");

		// タイマータスク起動
		//starttimerThreadID = plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable() {
		starttimerThreadID = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run(){
				/* 1秒ごとに呼ばれる */

				// 残り時間がゼロになった
				if (starttimerInSec <= 0){
					cancelTimerTask(); // タイマー停止
					start(sender); // ゲーム開始
					return;
				}

				message(msgPrefix+ "&aあと" +starttimerInSec+ "秒でこのゲームが始まります！");
				starttimerInSec--;
			}
		}, 0L, 20L);
	}

	/**
	 * メインのタイマータスクを開始する
	 */
	public void timer(){
		// タイマータスク
		//timerThreadID = plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable() {
		timerThreadID = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
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
	public void cancelTimerTask(){
		if (ready && starttimerThreadID != -1){
			plugin.getServer().getScheduler().cancelTask(starttimerThreadID);
		}
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


	/* getter / setter */
	public String getName(){
		return this.getStage().getName();
	}

	@Override
	public Set<String> getPlayersSet() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public boolean isReady() {
		return this.ready;
	}
	@Override
	public boolean isStarting() {
		return this.started;
	}

	@Override
	public Stage getStage() {
		return this.stage;
	}

	/**
	 * 各ゲームごとのログを取る
	 * @param line ログ
	 */
	public void log(String line){
		if (GameID != null){
			String filepath = plugin.getConfigs().getDetailDirectory() + GameID + ".log";
			Actions.log(filepath, line);
		}
	}
}