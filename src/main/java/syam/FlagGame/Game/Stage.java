/**
 * FlagGame - Package: syam.FlagGame.Game
 * Created: 2012/09/22 1:29:14
 */
package syam.FlagGame.Game;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import syam.FlagGame.FlagGame;
import syam.FlagGame.Api.IStage;
import syam.FlagGame.Enum.GameTeam;
import syam.FlagGame.Util.Actions;
import syam.FlagGame.Util.Cuboid;

/**
 * Stage (Stage.java)
 * @author syam(syamn)
 */
public class Stage implements IStage{
	// Logger
	public static final Logger log = FlagGame.log;
	private static final String logPrefix = FlagGame.logPrefix;
	private static final String msgPrefix = FlagGame.msgPrefix;

	// プラグインインスタンス
	private final FlagGame plugin;

	// ステージ情報
	private GameProfile profile;

	// ***** ステージデータ *****
	private String fileName;
	private String stageName;
	private int teamPlayerLimit = 16;

	private int gameTimeInSec = 6 * 60;
	private int remainSec = gameTimeInSec;
	private int timerThreadID = -1;

	private int startCountdownSec = 10;
	private int startTimerThreadID = -1;

	private int award = 300;
	private int entryFee = 100;

	private boolean busy = false;
	private boolean available = false;

	// フラッグ・チェスト
	private Map<Location, Flag> flags = new ConcurrentHashMap<Location, Flag>();
	private Set<Location> chests = Collections.newSetFromMap(new ConcurrentHashMap<Location, Boolean>());

	// 地点・エリア
	private Cuboid stageArea = null;
	private boolean stageProtect = true;
	private Map<GameTeam, Location> spawnMap = new ConcurrentHashMap<GameTeam, Location>();
	private Map<GameTeam, Cuboid> baseMap = new ConcurrentHashMap<GameTeam, Cuboid>();
	private Location specSpawn = null;

	/**
	 * コンストラクタ
	 * @param plugin
	 * @param name
	 */
	public Stage(final FlagGame plugin, final String name){
		this.plugin = plugin;

		this.stageName = name;
		this.fileName = this.stageName + ".yml";

		this.profile = new GameProfile(name);

		// ステージマネージャにステージ登録
		StageManager.stages.put(this.stageName, this);
	}

	/* ロールバックメソッド */
	/**
	 * このゲームの全ブロックをロールバックする
	 * @return
	 */
	public int rollbackFlags(){
		int count = 0;
		for (Flag flag : flags.values()){
			if (flag.rollback())
				count++;
		}
		return count;
	}
	/**
	 * コンテナブロックを2ブロック下の同じコンテナから要素をコピーする
	 */
	public int rollbackChests(){
		int count = 0;
		for (Location loc : chests){
			Block toBlock = loc.getBlock();
			Block fromBlock = toBlock.getRelative(BlockFace.DOWN, 2);

			// インベントリインターフェースを持たないブロックはスキップ
			if (!(toBlock.getState() instanceof InventoryHolder)){
				log.warning(logPrefix+ "Block is not InventoryHolder!Rollback skipping.. Block: "+ Actions.getBlockLocationString(fromBlock.getLocation()));
				continue;
			}
			// 2ブロック下とブロックIDが違えばスキップ
			if (toBlock.getTypeId() != fromBlock.getTypeId()){
				log.warning(logPrefix+ "BlockID unmatched!Rollback skipping.. Block: "+ Actions.getBlockLocationString(fromBlock.getLocation()));
				continue;
			}

			// 各チェストがインベントリホルダにキャスト出来ない場合例外にならないようtryで囲う
			InventoryHolder toContainer = null;
			InventoryHolder fromContainer = null; // チェストでなければここで例外 修正予定 → 7/22修正済み
			try{
				toContainer = (InventoryHolder) toBlock.getState();
				fromContainer = (InventoryHolder) fromBlock.getState();
			}catch(ClassCastException ex){
				log.warning(logPrefix+ "Container can't cast to InventoryHolder! Rollback skipping.. ToBlock: "+ Actions.getBlockLocationString(fromBlock.getLocation()));
				continue;
			}

			// チェスト内容コピー
			ItemStack[] oldIs = fromContainer.getInventory().getContents().clone();
			ItemStack[] newIs = new ItemStack[oldIs.length];
			for (int i = 0; i < oldIs.length; i++){
				if (oldIs[i] == null) continue;
				// newIs[i] = oldIs[i].clone(); // ItemStackシャローコピー不可
				newIs[i] = new ItemStack(oldIs[i]); // ディープコピー
			}

			toContainer.getInventory().setContents(newIs);
			count++;
		}
		return count;
	}


	/* ***** フラッグ関係 ***** */

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
		return flags.get(loc);
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
		this.flags.clear();
		this.flags.putAll(flags);
	}

	/* ***** チェスト関係 ***** */

	/**
	 * チェストを設定する
	 * @param loc チェストの座標
	 */
	public void setChest(Location loc){
		chests.add(loc);
	}
	/**
	 * チェストブロックを返す
	 * @param loc 調べるブロックの座標
	 * @return GameTeam または存在しない場合 null
	 */
	public Block getChest(Location loc){
		if (chests.contains(loc)){
			return loc.getBlock();
		}else{
			return null;
		}
	}
	/**
	 * チェストブロックを削除する
	 * @param loc 削除するチェストのブロック座標
	 */
	public void removeChest(Location loc){
		chests.remove(loc);
	}
	/**
	 * チェストブロックマップを一括取得する
	 * @return チェストブロックマップ Map<Location, Block>
	 */
	public Set<Location> getChests(){
		return chests;
	}
	/**
	 * チェストブロックマップを一括設定する
	 * @param chests 設定する元のLocation, Blockマップ
	 */
	public void setChests(Set<Location> chests){
		this.chests.clear();
		this.chests.addAll(chests);
	}

	/* ***** スポーン地点関係 ***** */

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
	public Location getSpecSpawn(){
		return this.specSpawn;
	}
	public void setSpecSpawn(Location loc){
		this.specSpawn = loc;
	}

	/* ***** エリア関係 ***** */

	// ステージ
	public void setStage(Location pos1, Location pos2){
		stageArea = new Cuboid(pos1, pos2);
	}
	public void setStage(Cuboid cuboid){
		this.stageArea = cuboid;
	}
	public Cuboid getStage(){
		return this.stageArea;
	}
	public void setStageProtected(boolean protect){
		this.stageProtect = protect;
	}
	public boolean isStageProtected(){
		return this.stageProtect;
	}
	// 拠点
	public void setBase(GameTeam team, Location pos1, Location pos2){
		baseMap.put(team, new Cuboid(pos1, pos2));
	}
	public void setBase(GameTeam team, Cuboid cuboid){
		baseMap.put(team, cuboid);
	}
	public Cuboid getBase(GameTeam team){
		if (team == null || !baseMap.containsKey(team))
			return null;
		return baseMap.get(team);
	}
	public Map<GameTeam, Cuboid> getBases(){
		return baseMap;
	}
	public void setBases(Map<GameTeam, Cuboid> bases){
		this.baseMap.clear();
		this.baseMap.putAll(bases);
	}


	/* getter / setter */
	/**
	 * ゲームステージプロファイルを返す
	 * @return GameProfile
	 */
	public GameProfile getProfile(){
		return this.profile;
	}



	/**
	 * ファイル名を設定
	 * @param filename
	 */
	public void setFileName(String filename){
		this.fileName = filename;
	}
	/**
	 * ファイル名を取得
	 * @return
	 */
	public String getFileName(){
		return fileName;
	}

	/**
	 * ゲーム名を返す
	 * @return このゲームの名前
	 */
	public String getName(){
		return stageName;
	}

	/**
	 * このゲームの制限時間(秒)を設定する
	 * @param sec 制限時間(秒)
	 */
	public void setGameTime(int sec){
		// もしゲーム中なら何もしない
		if (!busy){
			//cancelTimerTask();
			gameTimeInSec = sec;
			remainSec = gameTimeInSec;
		}
	}
	/**
	 * このゲームの制限時間(秒)を返す
	 * @return
	 */
	public int getGameTime(){
		return gameTimeInSec;
	}

	/**
	 * チーム毎の人数上限を設定する
	 * @param limit チーム毎の人数上限
	 */
	public void setTeamLimit(int limit){
		this.teamPlayerLimit = limit;
	}
	/**
	 * チーム毎の人数上限を取得
	 * @return チーム毎の人数上限
	 */
	public int getTeamLimit(){
		return teamPlayerLimit;
	}

	/**
	 * 賞金を設定する
	 * @param award 賞金
	 */
	public void setAward(int award){
		if (award < 0) award = 0;
		this.award = award;
	}
	/**
	 * 賞金を取得する
	 * @return 賞金
	 */
	public int getAward(){
		return award;
	}

	/**
	 * 参加料を設定する
	 * @param entryFee 参加料
	 */
	public void setEntryFee(int entryFee){
		if (entryFee < 0) entryFee = 0;
		this.entryFee = entryFee;
	}
	/**
	 * 参加料を取得する
	 * @return 参加料
	 */
	public int getEntryFee(){
		return entryFee;
	}
}