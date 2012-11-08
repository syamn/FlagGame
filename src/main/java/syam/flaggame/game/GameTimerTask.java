/**
 * FlagGame - Package: syam.flaggame.game
 * Created: 2012/10/05 22:46:35
 */
package syam.flaggame.game;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import syam.flaggame.FlagGame;
import syam.flaggame.util.Actions;
import syam.flaggame.util.Cuboid;

/**
 * GameTimerTask (GameTimerTask.java)
 * @author syam(syamn)
 */
public class GameTimerTask implements Runnable{
	// Logger
	public static final Logger log = FlagGame.log;
	private static final String logPrefix = FlagGame.logPrefix;
	private static final String msgPrefix = FlagGame.msgPrefix;

	private final FlagGame plugin;
	private Game game;
	private Cuboid cuboid = null;

	/**
	 * コンストラクタ
	 * @param plugin FlagGame
	 * @param game Game
	 */
	public GameTimerTask(final FlagGame plugin, final Game game){
		this.plugin = plugin;

		this.game = game;
		this.cuboid = game.getStage().getStage();
	}

	@Override
	public void run() {
		/* 1秒ごとに呼ばれる */

		// 残り時間がゼロになった
		if (game.getRemainTime() <= 0){
			game.cancelTimerTask(); // タイマー停止
			game.finish(); // ゲーム終了
			return;
		}

		// 15秒以下
		if (game.getRemainTime() <= 15){
			game.message(msgPrefix+ "&aゲーム終了まで あと "+game.getRemainTime()+" 秒です！");
		}
		// 30秒前
		else if (game.getRemainTime() == 30){
			game.message(msgPrefix+ "&aゲーム終了まで あと "+game.getRemainTime()+" 秒です！");
		}
		// 60秒間隔
		else if ((game.getRemainTime() % 60) == 0){
			game.message(msgPrefix+ "&aゲーム終了まで あと "+game.getRemainTime() / 60+" 分です！");
		}

		// プレイヤーの座標チェック
		if (cuboid != null){
			checkPlayersLocation();
		}

		// remainsec--
		game.tickRemainTime();
	}

	/**
	 * プレイヤーがステージ外に出ていないかチェックを行う
	 */
	private void checkPlayersLocation(){
		// ゲーム参加者リストを回す
		for (String name : game.getPlayersSet()){
			Player player = Bukkit.getPlayer(name);

			if (player == null || !player.isOnline())
				continue;

			// ステージ外に出ていればチームスポーン地点に戻す
			if (!cuboid.isIn(player.getLocation())){
				Entity vehicle = player.getVehicle();
				if (vehicle != null){
					// アイテムに座っている＝イスプラグインを使って座っている
					if (vehicle instanceof Item){
						vehicle.remove(); // アイテム削除
					}else{
						// その他、ボートやマインカートなら単に降りる
						//vehicle.eject();
						player.leaveVehicle();
					}
				}

				Location loc = game.getStage().getSpawn(game.getPlayerTeam(player));
				// ステージエリアの設定有無
				if (loc == null){
					player.setHealth(0);
					Actions.message(player, "&cステージエリア外に出たため死にました！");
				}else{
					player.teleport(loc, TeleportCause.PLUGIN);
					Actions.message(player, "&cステージエリア外に出たためスポーン地点に戻されました！");
				}
			}
		}
	}
}
