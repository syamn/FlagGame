package syam.FlagGame.Command;

import org.bukkit.Location;
import org.bukkit.Material;

import syam.FlagGame.Actions;
import syam.FlagGame.Game.Flag;
import syam.FlagGame.Game.FlagType;
import syam.FlagGame.Game.Game;
import syam.FlagGame.Game.GameManager;

public class SetflagCommand extends BaseCommand {
	public SetflagCommand(){
		bePlayer = false;
		name = "setflag";
		argLength = 0;
		usage = "[FlagType] <- create new flag";
	}

	@Override
	public boolean execute() {
		// 引数なしの場合はフラッグ管理モードの切り替えを行う
		if (args.size() == 0){
			if (GameManager.isManager(player)){
				// フラッグ管理モード終了
				GameManager.setManager(player, false);
				GameManager.setSelectedBlock(player, null);
				Actions.message(null, player, "&aフラッグ管理モードを終了しました。");
			}else{
				// フラッグ管理モード開始
				GameManager.setManager(player, true);
				String tool = Material.getMaterial(plugin.getConfigs().toolID).name();
				Actions.message(null, player, "&aフラッグ管理モードを開始しました。選択ツール: " + tool);
			}
			return true;
		}else{
			// それ以上の場合は指定済みブロックをフラッグにする
			// fa setflag (flag-type)
			Game game = GameManager.getSelectedGame(player);
			Location loc = GameManager.getSelectedBlock(player);
			FlagType type = null;

			if (game == null){
				Actions.message(null, player, "&c先に編集するゲームを選択してください");
				return true;
			}
			if (loc == null){
				Actions.message(null, player, "&c先に編集するブロックを編集アイテムで右クリックしてください");
				return true;
			}

			for (FlagType ft : FlagType.values()){
				if (ft.name().toLowerCase().equalsIgnoreCase(args.get(0)))
					type = ft;
			}
			if (type == null){
				Actions.message(null, player, "&cフラッグの種類を正しく指定してください！");
				return true;
			}

			new Flag(plugin, game, loc, type);

			Actions.message(null, player, "&aゲーム'"+game.getName()+"'の"+type.getTypeName()+"フラッグを登録しました！");
			return true;
		}
	}

	@Override
	public boolean permission() {
		return sender.hasPermission("flag.admin");
	}
}

