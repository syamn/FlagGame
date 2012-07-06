package syam.FlagGame.Game;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import syam.FlagGame.FlagGame;

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
			List<String> flagList = convertFlagMapToList(game.getFlags());
			List<String> spawnList = convertSpawnMapToList(game.getSpawns());

			// 保存するデータをここに
			confFile.set("GameName", game.getName());
			confFile.set("Spawns", spawnList);
			confFile.set("Flags", flagList);

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

		// 取得データ
		String name;
		for (File file : files){
			try{
				confFile.load(file);

				// 読むデータキー
				name = confFile.getString("GameName", null);

				// ゲーム追加
				Game game = new Game(plugin, name);
				// スポーン地点追加
				game.setSpawns(convertSpawnListToMap(confFile.getStringList("Spawns")));
				// フラッグ追加
				game.setFlags(convertFlagListToMap(confFile.getStringList("Flags"), game));

				log.info(logPrefix+ "Loaded Game: "+file.getName()+" ("+name+")");

			}catch (Exception ex){
				ex.printStackTrace();
			}
		}
	}

	/**
	 * ハッシュマップからリストに変換
	 * @param flags フラッグ　マップ
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
	 * リストからハッシュマップに変換
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

}
