package syam.FlagGame.Command;

import java.util.Random;

import syam.FlagGame.Game.Game;
import syam.FlagGame.Game.GameManager;
import syam.FlagGame.Game.Stage;
import syam.FlagGame.Game.StageManager;
import syam.FlagGame.Permission.Perms;
import syam.FlagGame.Util.Actions;

public class ReadyCommand extends BaseCommand {
	public ReadyCommand(){
		bePlayer = false;
		name = "ready";
		argLength = 1;
		usage = "<stage> <- ready game";
	}

	@Override
	public void execute() {
		// flag ready - ゲームを開始準備中にする
		if (args.size() == 0){
			Actions.message(sender, null, "&cステージ名を入力してください！");
			return;
		}

		Stage stage = null;

		// ランダム
		if (args.get(0).equalsIgnoreCase("random")){
			stage = StageManager.getStage(getRandomStage());
			// TODO: 開始中のゲームが選択される可能性 ランダムアナウンスができない
		}
		// 通常のゲーム
		else{
			stage = StageManager.getStage(args.get(0));
		}

		if (stage == null){
			Actions.message(sender, null, "&cステージ'"+args.get(0)+"'が見つかりません");
			return;
		}

		// ** ステージチェック **
		if (!stage.isAvailable()){
			Actions.message(sender, null, "&cステージ'"+stage.getName()+"'は現在使えません");
			return;
		}

		if (stage.isUsing()){
			if (stage.getGame() == null){
				Actions.message(sender, null, "&cステージ'"+stage.getName()+"'は現在使用中です");
			}else{
				if (stage.getGame().isStarting()){
					Actions.message(sender, null, "&cこのゲームは既に始まっています");
				}
				else if (stage.getGame().isReady()){
					Actions.message(sender, null, "&cこのゲームは既に参加受付中です");
				}
			}
			return;
		}

		// ステージエリアチェック
		if (stage.getStage() == null){
			Actions.message(sender, null, "&cステージエリアが正しく設定されていません");
			return;
		}

		// スポーン地点チェック
		if (stage.getSpawns().size() != 2){
			Actions.message(sender, null, "&cチームスポーン地点が正しく設定されていません");
			return;
		}

		// ready
		Game game = new Game(plugin, stage);
		game.ready(sender);
	}

	/**
	 * ランダムなステージ名を返す
	 * @return ステージ名
	 */
	private String getRandomStage(){
		Random rnd = new Random();

		String[] stages = StageManager.stages.keySet().toArray(new String[StageManager.stages.size()]);
		String stage = stages[rnd.nextInt(stages.length)];

		return stage;
	}

	@Override
	public boolean permission() {
		return Perms.READY.has(sender);
	}
}
