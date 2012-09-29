/**
 * FlagGame - Package: syam.flaggame.api
 * Created: 2012/09/22 3:12:09
 */
package syam.flaggame.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Block;

import syam.flaggame.enums.FlagState;
import syam.flaggame.enums.FlagType;
import syam.flaggame.enums.GameTeam;
import syam.flaggame.game.Flag;
import syam.flaggame.game.GameProfile;
import syam.flaggame.util.Cuboid;

/**
 * IStage (IStage.java)
 * @author syam(syamn)
 */
public interface IStage {

	// ロールバックメソッド
	int rollbackFlags();
	int rollbackChests();


	// フラッグ関係
	void setFlag(Location loc, Flag flag);
	void removeFlag(Location loc);
	Flag getFlag(Location loc);

	void setFlags(Map<Location, Flag> flags);
	Map<Location, Flag> getFlags();

	public Map<FlagState, HashMap<FlagType, Integer>> checkFlag();

	// チェスト関係
	void setChest(Location loc);
	void removeChest(Location loc);
	Block getChest(Location loc);

	void setChests(Set<Location> chests);
	Set<Location> getChests();

	/* 座標設定 */
	// スポーン地点
	void setSpawn(GameTeam team, Location loc);
	Location getSpawn(GameTeam team);

	void setSpawns(Map<GameTeam, Location> spawns);
	Map<GameTeam, Location> getSpawns();

	// 観戦席
	void setSpecSpawn(Location loc);
	Location getSpecSpawn();


	/* エリア設定 */
	// ステージ
	void setStage(Location pos1, Location pos2);
	void setStage(Cuboid cuboid);
	Cuboid getStage();

	void setStageProtected(boolean protect);
	boolean isStageProtected();

	// 拠点
	void setBase(GameTeam team, Location pos1, Location pos2);
	void setBase(GameTeam team, Cuboid cuboid);
	Cuboid getBase(GameTeam team);

	void setBases(Map<GameTeam, Cuboid> bases);
	Map<GameTeam, Cuboid> getBases();


	/* getter / setter */
	void setFileName(String filename);
	String getFileName();

	String getName();
	GameProfile getProfile();

	// ステージ制限時間
	void setGameTime(int sec);
	int getGameTime();

	// ステージ人数制限
	void setTeamLimit(int limit);
	int getTeamLimit();

	// 賞金
	void setAward(int award);
	int getAward();

	// 参加費
	void setEntryFee(int entryFee);
	int getEntryFee();

	void setAvailable(boolean available);
	boolean isAvailable();
}
