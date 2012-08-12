package syam.FlagGame.Game;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import syam.FlagGame.FlagGame;
import syam.FlagGame.Enum.FlagType;
import syam.FlagGame.Enum.GameTeam;
import syam.FlagGame.Util.Cuboid;

public class GameFileManager {
	// Logger
	public static final Logger log = FlagGame.log;
	private static final String logPrefix = FlagGame.logPrefix;
	private static final String msgPrefix = FlagGame.msgPrefix;

	private final FlagGame plugin;

	public GameFileManager(final FlagGame plugin){
		this.plugin = plugin;
	}

	/* ゲームデータ保存/読み出し */
	public void saveGames(){
		FileConfiguration confFile = new YamlConfiguration();
		String fileDir = plugin.getDataFolder() + System.getProperty("file.separator") +
				"gameData" + System.getProperty("file.separator");

		for (Game game : plugin.games.values()){
			File file = new File(fileDir + game.getName() + ".yml");

			// マップデータをリストに変換
			String stage = null;
			if (game.getStage() != null) stage = convertStageCuboidToString(game.getStage());
			List<String> flagList = convertFlagMapToList(game.getFlags());
			List<String> spawnList = convertSpawnMapToList(game.getSpawns());
			List<String> baseList = convertBaseMapToList(game.getBases());
			List<String> chestList = convertChestMapToList(game.getChests());

			// 保存するデータをここに
			confFile.set("GameName", game.getName());
			confFile.set("GameTime", game.getGameTime());
			confFile.set("TeamLimit", game.getTeamLimit());
			confFile.set("Award", game.getAward());
			confFile.set("EntryFee", game.getEntryFee());
			confFile.set("StageProtected", game.isStageProtected());

			confFile.set("Stage", stage);
			confFile.set("Spawns", spawnList);
			confFile.set("SpecSpawn", convertPlayerLocation(game.getSpecSpawn()));
			confFile.set("Flags", flagList);
			confFile.set("Bases", baseList);
			confFile.set("Chests", chestList);

			try {
				confFile.save(file);
			} catch (IOException ex) {
				log.warning(logPrefix+ "Couldn't write Game data!");
				ex.printStackTrace();
			}
		}
	}

	public void loadGames(){
		FileConfiguration confFile = new YamlConfiguration();
		String fileDir = plugin.getDataFolder() + System.getProperty("file.separator") + "gameData";

		File dir = new File(fileDir);
		File[] files = dir.listFiles();

		// ゲームデータクリア
		plugin.games.clear();

		// ファイルなし
		if (files == null || files.length == 0)
			return;

		// 取得データ
		String name;
		for (File file : files){
			try{
				confFile.load(file);

				// 読むデータキー
				name = confFile.getString("GameName", null);

				// ゲーム追加
				Game game = new Game(plugin, name);

				// ファイル名設定
				game.setFileName(file.getName());

				// 各設定やマップを追加
				game.setGameTime(confFile.getInt("GameTime", 60 * 10));
				game.setTeamLimit(confFile.getInt("TeamLimit", 8));
				game.setAward(confFile.getInt("Award", 1000));
				game.setEntryFee(confFile.getInt("EntryFee", 100));
				game.setStageProtected(confFile.getBoolean("StageProtected", true));

				Cuboid stage = convertStageStringToCuboid(confFile.getString("Stage")); // ステージエリア
				if (stage != null) game.setStage(stage);
				game.setSpawns(convertSpawnListToMap(confFile.getStringList("Spawns"))); // スポーン地点
				game.setSpecSpawn(convertPlayerLocation(confFile.getString("SpecSpawn", null))); // 観戦者スポーン地点
				game.setFlags(convertFlagListToMap(confFile.getStringList("Flags"), game)); // フラッグ
				game.setBases(convertBaseListToMap(confFile.getStringList("Bases"))); // 拠点エリア
				game.setChests(convertChestListToMap(confFile.getStringList("Chests"))); // チェスト

				log.info(logPrefix+ "Loaded Game: "+file.getName()+" ("+name+")");

			}catch (Exception ex){
				ex.printStackTrace();
			}
		}
	}

	/* ステージ領域を変換 */
	private String convertStageCuboidToString(Cuboid stage) {
		String ret = "";

		// x,y,z@x,y,z
		Location pos1 = stage.getPos1();
		Location pos2 = stage.getPos2();

		ret = pos1.getBlockX()+","+pos1.getBlockY()+","+pos1.getBlockZ()+"@";
		ret = ret + pos2.getBlockX()+","+pos2.getBlockY()+","+pos2.getBlockZ();

		return ret;
	}
	private Cuboid convertStageStringToCuboid(String stage) {
		if (stage == null)
			return null;

		String[] data;
		String[] pos1;
		String[] pos2;

		// デリミタ分割
		data = stage.split("@");
		if (data.length != 2){
			log.warning(logPrefix+ "Skipping StageLine: incorrect format (@)");
			return null;
		}

		// data[0] : 座標形式チェック
		pos1 = data[0].split(",");
		if (pos1.length != 3){
			log.warning(logPrefix+ "Skipping StageLine: incorrect 1st coord format (,)");
			return null;
		}

		// data[1] : 座標形式チェック
		pos2 = data[1].split(",");
		if (pos1.length != 3){
			log.warning(logPrefix+ "Skipping StageLine: incorrect 2nd coord format (,)");
			return null;
		}

		World world = Bukkit.getWorld(plugin.getConfigs().gameWorld);
		return new Cuboid(
				new Location(world, Double.parseDouble(pos1[0]), Double.parseDouble(pos1[1]), Double.parseDouble(pos1[2])),
				new Location(world, Double.parseDouble(pos2[0]), Double.parseDouble(pos2[1]), Double.parseDouble(pos2[2]))
				);
	}

	/* フラッグデータを変換 */
	/**
	 * フラッグデータをハッシュマップからリストに変換
	 * @param flags フラッグマップ
	 * @return フラッグ情報文字列のリスト
	 */
	private List<String> convertFlagMapToList(Map<Location, Flag> flags){
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
	/**
	 * フラッグデータをリストからハッシュマップに変換
	 * @param flags
	 * @param game
	 * @return
	 */
	private Map<Location, Flag> convertFlagListToMap(List<String> flags, Game game){
		Map<Location, Flag> ret = new HashMap<Location, Flag>();
		ret.clear();

		String[] data;
		String[] block;
		String[] coord;

		World world = Bukkit.getWorld(plugin.getConfigs().gameWorld);

		int line = 0;
		for (String s : flags){
			line++;
			// デリミタで分ける
			data = s.split("@");
			if (data.length != 3){
				log.warning(logPrefix+ "Skipping FlagLine "+line+": incorrect format (@)");
				continue;
			}

			// data[0] : フラッグ種類チェック
			FlagType type = null;
			for (FlagType ft : FlagType.values()){
				if (ft.name().equalsIgnoreCase(data[0])){
					type = ft;
				}
			}
			if (type == null){
				log.warning(logPrefix+ "Skipping FlagLine "+line+": undefined FlagType");
				continue;
			}

			// data[1] : ブロックID・データ値チェック
			block = data[1].split(":");
			if (block.length != 2){
				log.warning(logPrefix+ "Skipping FlagLine "+line+": incorrect block format (:)");
				continue;
			}

			// data[2] : 座標形式チェック
			coord = data[2].split(",");
			if (coord.length != 3){
				log.warning(logPrefix+ "Skipping FlagLine "+line+": incorrect coord format (,)");
				continue;
			}

			Location loc = new Location(world, new Double(coord[0]), new Double(coord[1]), new Double(coord[2])).getBlock().getLocation();
			ret.put(loc, new Flag(plugin, game, loc, type, Integer.parseInt(block[0]), Byte.parseByte(block[1])));
		}

		return ret;
	}

	/* スポーン地点データを変換 */
	private List<String> convertSpawnMapToList(Map<GameTeam, Location> spawns){
		List<String> ret = new ArrayList<String>();
		ret.clear();

		for (Map.Entry<GameTeam, Location> entry : spawns.entrySet()){
			// RED@x,y,z,pitch,yaw
			String s = entry.getKey().name()+"@";
			Location loc = entry.getValue();
			s = s + loc.getX()+","+loc.getY()+","+loc.getZ()+","+loc.getYaw()+","+loc.getPitch();

			// リストに追加
			ret.add(s);
		}

		return ret;
	}
	private Map<GameTeam, Location> convertSpawnListToMap(List<String> spawns){
		Map<GameTeam, Location> ret = new HashMap<GameTeam, Location>();
		ret.clear();

		String[] data;
		String[] coord;

		World world = Bukkit.getWorld(plugin.getConfigs().gameWorld);

		int line = 0;
		for (String s : spawns){
			line++;
			// デリミタ分割
			data = s.split("@");
			if (data.length != 2){
				log.warning(logPrefix+ "Skipping SpawnLine "+line+": incorrect format (@)");
				continue;
			}

			// data[0] : チームチェック
			GameTeam team = null;
			for (GameTeam gt : GameTeam.values()){
				if (gt.name().equalsIgnoreCase(data[0])){
					team = gt;
				}
			}
			if (team == null){
				log.warning(logPrefix+ "Skipping SpawnLine "+line+": undefined TeamName");
				continue;
			}

			// data[1] : 座標形式チェック
			coord = data[1].split(",");
			if (coord.length != 5){
				log.warning(logPrefix+ "Skipping SpawnLine "+line+": incorrect coord format (,)");
				continue;
			}

			Location loc = new Location(world, Double.valueOf(coord[0]), Double.valueOf(coord[1]), Double.valueOf(coord[2]), Float.valueOf(coord[3]), Float.valueOf(coord[4]));
			ret.put(team, loc);
		}

		return ret;
	}

	/* 拠点データを変換 */
	private List<String> convertBaseMapToList(Map<GameTeam, Cuboid> bases){
		List<String> ret = new ArrayList<String>();
		ret.clear();

		for (Map.Entry<GameTeam, Cuboid> entry : bases.entrySet()){
			// RED@x,y,z@x,y,z
			String s = entry.getKey().name()+"@";

			Cuboid cuboid = entry.getValue();
			Location pos1 = cuboid.getPos1();
			Location pos2 = cuboid.getPos2();

			s = s + pos1.getBlockX()+","+pos1.getBlockY()+","+pos1.getBlockZ()+"@";
			s = s + pos2.getBlockX()+","+pos2.getBlockY()+","+pos2.getBlockZ();

			// リストに追加
			ret.add(s);
		}

		return ret;
	}
	private Map<GameTeam, Cuboid> convertBaseListToMap(List<String> bases){
		Map<GameTeam, Cuboid> ret = new HashMap<GameTeam, Cuboid>();
		ret.clear();

		String[] data;
		String[] pos1;
		String[] pos2;

		World world = Bukkit.getWorld(plugin.getConfigs().gameWorld);

		int line = 0;
		for (String s : bases){
			line++;
			// デリミタ分割
			data = s.split("@");
			if (data.length != 3){
				log.warning(logPrefix+ "Skipping BaseLine "+line+": incorrect format (@)");
				continue;
			}

			// data[0] : チームチェック
			GameTeam team = null;
			for (GameTeam gt : GameTeam.values()){
				if (gt.name().equalsIgnoreCase(data[0])){
					team = gt;
				}
			}
			if (team == null){
				log.warning(logPrefix+ "Skipping BaseLine "+line+": undefined TeamName");
				continue;
			}

			// data[1] : 座標形式チェック
			pos1 = data[1].split(",");
			if (pos1.length != 3){
				log.warning(logPrefix+ "Skipping BaseLine "+line+": incorrect 1st coord format (,)");
				continue;
			}

			// data[2] : 座標形式チェック
			pos2 = data[2].split(",");
			if (pos2.length != 3){
				log.warning(logPrefix+ "Skipping BaseLine "+line+": incorrect 2nd coord format (,)");
				continue;
			}

			ret.put(team, new Cuboid(
					new Location(world, Double.parseDouble(pos1[0]), Double.parseDouble(pos1[1]), Double.parseDouble(pos1[2])),
					new Location(world, Double.parseDouble(pos2[0]), Double.parseDouble(pos2[1]), Double.parseDouble(pos2[2]))
					));
		}

		return ret;
	}

	/* チェストデータを変換 */
	private List<String> convertChestMapToList(Set<Location> chests){
		List<String> ret = new ArrayList<String>();
		ret.clear();

		for (Location loc : chests){
			// x,y,z
			String s = loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ();

			// リストに追加
			ret.add(s);
		}

		return ret;
	}
	private Set<Location> convertChestListToMap(List<String> chests){
		Set<Location> ret = new HashSet<Location>();
		ret.clear();

		String[] coord;

		World world = Bukkit.getWorld(plugin.getConfigs().gameWorld);

		int line = 0;
		for (String s : chests){
			line++;
			// 座標形式チェック
			coord = s.split(",");
			if (coord.length != 3){
				log.warning(logPrefix+ "Skipping ChestLine "+line+": incorrect coord format (,)");
				continue;
			}

			ret.add(new Location(world, Double.parseDouble(coord[0]), Double.parseDouble(coord[1]), Double.parseDouble(coord[2])));
		}

		return ret;
	}

	// プレイヤーのLocationオブジェクトから文字列に変換
	private String convertPlayerLocation(Location loc){
		if (loc == null) return null;
		return loc.getX()+","+loc.getY()+","+loc.getZ()+","+loc.getYaw()+","+loc.getPitch();
	}
	// convertPlayerLocationToStringで変換したプレイヤーLocationに戻す
	private Location convertPlayerLocation(String loc){
		if (loc == null) return null;
		String[] coord = loc.split(",");
		if (coord.length != 5) return null;
		return new Location(
				Bukkit.getWorld(plugin.getConfigs().gameWorld),
				Double.valueOf(coord[0]),
				Double.valueOf(coord[1]),
				Double.valueOf(coord[2]),
				Float.valueOf(coord[3]),
				Float.valueOf(coord[4])
				);
	}
}
