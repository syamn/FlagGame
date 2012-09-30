package syam.flaggame.command;

import java.util.Random;

import syam.flaggame.game.Game;
import syam.flaggame.game.Stage;
import syam.flaggame.manager.GameManager;
import syam.flaggame.manager.StageManager;
import syam.flaggame.permission.Perms;
import syam.flaggame.util.Actions;

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
		boolean random = false;

		// ランダムゲーム
		if (args.get(0).equalsIgnoreCase("random")){
			if(GameManager.getRandomGame() == null){
				if (GameManager.getRandomGame().isReady()){
					Actions.message(sender, null, "&c現在既にランダムステージが参加受付中です");
					return;
				}else{
					GameManager.setRandomGame(null);
				}
			}

			stage = StageManager.getRandomAvailableStage();
			random = true;
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
		Game game = new Game(plugin, stage, random);
		game.ready(sender);
	}

	@Override
	public boolean permission() {
		return Perms.READY.has(sender);
	}
}
