/**
 * FlagGame - Package: syam.flaggame.game
 * Created: 2012/10/05 22:46:35
 */
package syam.flaggame.game;

import java.util.logging.Logger;

import syam.flaggame.FlagGame;

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

	public GameTimerTask(final FlagGame plugin, final Game game){
		this.plugin = plugin;
		this.game = game;
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
			int remainMin = game.getRemainTime() / 60;
			game.message(msgPrefix+ "&aゲーム終了まで あと "+remainMin+" 分です！");
		}

		// remainsec--
		game.tickRemainTime();
	}
}
