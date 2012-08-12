package syam.FlagGame.Command;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import syam.FlagGame.Game.Game;
import syam.FlagGame.Util.Actions;

public class WatchCommand extends BaseCommand{
	public WatchCommand(){
		bePlayer = true;
		name = "watch";
		argLength = 1;
		usage = "<game> <- watch the game";
	}

	@Override
	public boolean execute() {
		Game game = plugin.getGame(args.get(0));
		if (game == null){
			Actions.message(null, player, "&cゲーム'"+args.get(0)+"'が見つかりません");
			return true;
		}

		Location specSpawn = game.getSpecSpawn();
		if (specSpawn == null){
			Actions.message(null, player, "&cゲーム'"+args.get(0)+"'は観戦者のスポーン地点が設定されていません");
			return true;
		}

		// テレポート
		player.teleport(specSpawn, TeleportCause.PLUGIN);
		Actions.message(null, player, "&aゲーム'"+args.get(0)+"'の観戦者スポーン地点へ移動しました！");
		
		return true;
	}

	@Override
	public boolean permission() {
		return sender.hasPermission("flag.user.watch");
	}
}
