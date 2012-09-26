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
	 * ステージ名からステージを返す
	 * @param stageName
	 * @return Game
	 */
	public static Stage getStage(String stageName){
		return stages.get(stageName);
	}

	/**
	 * 全ステージプロファイルを保存する
	 */
	public static void saveAll(){
		for (Stage stage : StageManager.stages.values()){
			stage.getProfile().save();
		}
	}
}
