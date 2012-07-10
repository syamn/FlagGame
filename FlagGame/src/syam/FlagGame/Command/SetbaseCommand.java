package syam.FlagGame.Command;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;

import syam.FlagGame.Enum.GameTeam;
import syam.FlagGame.Game.Game;
import syam.FlagGame.Game.GameManager;
import syam.FlagGame.Util.Actions;
import syam.FlagGame.Util.WorldEditHandler;

public class SetbaseCommand extends BaseCommand {
	public SetbaseCommand(){
		bePlayer = true;
		name = "setbase";
		argLength = 1;
		usage = "<team> <- set team base";
	}

	@Override
	public boolean execute() {
		// ゲーム取得
		Game game = GameManager.getSelectedGame(player);
		if (game == null){
			Actions.message(null, player, "&c先に編集するゲームを選択してください");
			return true;
		}

		// チーム取得
		GameTeam team = null;
		for (GameTeam tm : GameTeam.values()){
			if (tm.name().toLowerCase().equalsIgnoreCase(args.get(0)))
			{	team = tm; break;	}
		}
		if (team == null){
			Actions.message(null, player, "&cチーム'"+args.get(0)+"'が見つかりません！");
			return true;
		}

		// WorldEdit選択領域取得
		Block[] corners = WorldEditHandler.getWorldEditRegion(player);
		// エラー プレイヤーへのメッセージ送信はWorldEditHandlerクラスで処理
		if (corners == null || corners.length != 2) return true;

		Block block1 = corners[0];
		Block block2 = corners[1];

		// ワールドチェック
		if (block1.getWorld() != Bukkit.getWorld(plugin.getConfigs().gameWorld)){
			Actions.message(null, player, "&c指定しているエリアはゲームワールドではありません！");
			return true;
		}

		// 拠点設定
		game.setBase(team, block1.getLocation(), block2.getLocation());

		Actions.message(null, player, "&a"+team.getTeamName()+"チームの拠点を設定しました！");
		return true;
	}

	@Override
	public boolean permission() {
		return sender.hasPermission("flag.admin");
	}
}
