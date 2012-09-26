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
	private static HashMap<String, Stage> stages = new HashMap<String, Stage>();

	/**
	 * 全ステージプロファイルを保存する
	 */
	public static void saveAll(){
		for (Stage stage : StageManager.stages.values()){
			stage.getProfile().save();
		}
	}

	/**
	 * 全ステージのマップを返す
	 * @return HashMap<String, Stage>
	 */
	public static HashMap<String, Stage> getStages(){
		return stages;
	}
	/**
	 * ステージとステージ名を紐付けでマッピングする
	 * @param stageName ステージ名
	 * @param stage ステージインスタンス
	 */
	public static void addStage(String stageName, Stage stage){
		stages.put(stageName, stage);
	}
	/**
	 * 指定したステージをマップから削除する
	 * @param stageName 削除するステージ名
	 */
	public static void removeStage(String stageName){
		stages.remove(stageName);
	}
	/**
	 * ステージマップをクリアする
	 */
	public static void removeStages(){
		stages.clear();
	}


	/**
	 * ステージ名からステージを返す
	 * @param stageName
	 * @return Game
	 */
	public static Stage getStage(String stageName){
		return stages.get(stageName);
	}



}