package syam.FlagGame.Command;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import syam.FlagGame.FGPlayer.PlayerManager;
import syam.FlagGame.Game.Game;
import syam.FlagGame.Permission.Perms;
import syam.FlagGame.Util.Actions;

public class WatchCommand extends BaseCommand{
	public WatchCommand(){
		bePlayer = true;
		name = "watch";
		argLength = 1;
		usage = "<game> <- watch the game";
	}

	@Override
	public void execute() {
		Game game = plugin.getGame(args.get(0));
		if (game == null){
			Actions.message(null, player, "&cゲーム'"+args.get(0)+"'が見つかりません");
			return;
		}

		Location specSpawn = game.getSpecSpawn();
		if (specSpawn == null){
			Actions.message(null, player, "&cゲーム'"+args.get(0)+"'は観戦者のスポーン地点が設定されていません");
			return;
		}

		for (Game check : plugin.games.values()){
			if (check.getPlayerTeam(player) != null){
				Actions.message(null, player, "&cあなたはゲーム'"+check.getName()+"'に参加しているため移動できません！");
				return;
			}
		}

		// テレポート
		if (!player.getWorld().equals(specSpawn.getWorld())){
			PlayerManager.getProfile(player.getName()).setTpBackLocation(player.getLocation());
		}
		player.teleport(specSpawn, TeleportCause.PLUGIN);
		Actions.message(null, player, "&aゲーム'"+args.get(0)+"'の観戦者スポーン地点へ移動しました！");
	}

	@Override
	public boolean permission() {
		return Perms.WATCH.has(sender);
	}
}
