package syam.FlagGame.Command;

import org.bukkit.Material;

import syam.FlagGame.Game.GameManager;
import syam.FlagGame.Util.Actions;

public class SetchestCommand extends BaseCommand {
	public SetchestCommand(){
		bePlayer = true;
		name = "setchest";
		argLength = 0;
		usage = "<- create new chest";
	}

	@Override
	public boolean execute() {
		// 引数なしの場合はフラッグ管理モードの切り替えを行う
		if (args.size() == 0){
			if (GameManager.isChestManager(player)){
				// フラッグ管理モード終了
				GameManager.setChestManager(player, false);
				Actions.message(null, player, "&aチェスト管理モードを終了しました。");
			}else{
				if (GameManager.isFlagManager(player)){
					Actions.message(null, player, "&aフラッグ管理モードを終了します。");
					GameManager.setFlagManager(player, false);
				}
				// フラッグ管理モード開始
				GameManager.setChestManager(player, true);
				String tool = Material.getMaterial(plugin.getConfigs().toolID).name();
				Actions.message(null, player, "&aチェスト管理モードを開始しました。選択ツール: " + tool);
			}
		}
		return true;
	}

	@Override
	public boolean permission() {
		return sender.hasPermission("flag.admin");
	}
}
