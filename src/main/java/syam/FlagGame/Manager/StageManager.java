/**
 * FlagGame - Package: syam.FlagGame.Game
 * Created: 2012/09/23 4:58:28
 */
package syam.FlagGame.Manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import syam.FlagGame.Game.Stage;

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
	 * 実行可能なステージリストを返す
	 * @return List<Stage>
	 */
	public static ArrayList<Stage> getAvailableStages(){
		ArrayList<Stage> ret = new ArrayList<Stage>();

		for (Stage stage : stages.values()){
			if (stage.isAvailable() && !stage.isUsing()){
				ret.add(stage);
			}
		}

		return ret;
	}

	/**
	 * 実行可能なステージからランダムで1つ抽出する
	 * @return Stage
	 */
	public static Stage getRandomAvailableStage(){
		Random rnd = new Random();
		ArrayList<Stage> availables = getAvailableStages();

		if (availables.size() <= 0){
			return null;
		}

		return availables.get(rnd.nextInt(availables.size()));
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