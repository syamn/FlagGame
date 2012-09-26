/**
 * FlagGame - Package: syam.FlagGame.Api
 * Created: 2012/09/22 2:40:53
 */
package syam.FlagGame.Api;

import java.util.Map;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import syam.FlagGame.Enum.GameResult;
import syam.FlagGame.Enum.GameTeam;
import syam.FlagGame.Game.Stage;

/**
 * IGame (IGame.java)
 * @author syam(syamn)
 */
public interface IGame {
	// ゲーム進行関係
	void ready(CommandSender sender);

	void start(CommandSender sender);

	void finish();
	void finish(GameResult result, GameTeam winTeam, String reason);


	// 参加者関係
	boolean addPlayer(Player player);
	boolean addPlayer(Player player, GameTeam team);
	boolean remPlayer(String playerName);
	boolean remPlayer(Player player);

	boolean isJoined(String playerName);
	boolean isJoined(Player player);
	GameTeam getPlayerTeam(Player player);

	Map<GameTeam, Set<String>> getPlayersMap();
	Set<String> getPlayersSet();
	Set<String> getPlayersSet(GameTeam team);


	// 参加者へのアクション関係
	void message(String message);
	void message(GameTeam team, String message);
	void tpSpawnLocation();


	// タイマー関係
	void start_timer(final CommandSender sender);
	void timer();
	void cancelTimerTask();
	int getRemainTime();


	// kill/death
	void addKillCount(final GameTeam team);
	int getKillCount(final GameTeam team);


	// ログ取り
	void log(String line);


	/* getter / setter */
	boolean isReady();
	boolean isStarting();

	Stage getStage();
}
