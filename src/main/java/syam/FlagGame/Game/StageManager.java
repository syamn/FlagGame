/**
 * FlagGame - Package: syam.FlagGame.Game
 * Created: 2012/09/23 4:58:28
 */
package syam.FlagGame.Game;

import java.util.HashMap;

/**
 * StageManager (StageManager.java)
 * @author syam(syamn)
 */
public class StageManager {
	public static HashMap<String, Stage> stages = new HashMap<String, Stage>();

	/**
	 * ステージを返す
	 * @param stageName
	 * @return Game
	 */
	public static Stage getStage(String stageName){
		return stages.get(stageName);
	}
}
