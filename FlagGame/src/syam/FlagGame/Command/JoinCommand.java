package syam.FlagGame.Command;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;

import syam.FlagGame.Game.Game;
import syam.FlagGame.Game.GameTeam;
import syam.FlagGame.Util.Actions;

public class JoinCommand extends BaseCommand {
	public JoinCommand(){
		bePlayer = true;
		name = "join";
		argLength = 1;
		usage = "<game> <- join game";
	}

	@Override
	public boolean execute() {
		Game game = plugin.getGame(args.get(0));
		if (game == null){
			Actions.message(null, player, "&cゲーム'"+args.get(0)+"'が見つかりません");
			return true;
		}

		if (!game.isReady()){
			Actions.message(null, player, "&cゲーム'"+args.get(0)+"'は現在参加受付中ではありません");
			return true;
		}

		// 人数チェック
		int limit = game.getTeamPlayerLimit();
		if ((game.getPlayersSet(GameTeam.RED).size() >= limit) && (game.getPlayersSet(GameTeam.BLUE).size() >= limit)){
			Actions.message(null, player, "&cこのゲームは参加可能な定員に達しています！");
			return true;
		}

		// join
		game.addPlayer(player);

		// 所属チーム取得
		GameTeam team = game.getPlayerTeam(player);
		Actions.broadcastMessage(msgPrefix+"&aプレイヤー'&6"+player.getName()+"'&aが&6"+team.getTeamName()+"チーム&aに参加しました！");
		return true;
	}

	@Override
	public boolean permission() {
		return sender.hasPermission("flag.user.join");
	}
}
