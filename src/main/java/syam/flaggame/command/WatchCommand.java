package syam.flaggame.command;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import syam.flaggame.game.Game;
import syam.flaggame.game.Stage;
import syam.flaggame.manager.GameManager;
import syam.flaggame.manager.StageManager;
import syam.flaggame.permission.Perms;
import syam.flaggame.player.PlayerManager;
import syam.flaggame.util.Actions;

public class WatchCommand extends BaseCommand {
    public WatchCommand() {
        bePlayer = true;
        name = "watch";
        argLength = 0;
        usage = "[stage] <- watch the game";
    }

    @Override
    public void execute() {
        Stage stage = null;

        if (args.size() >= 1) {
            stage = StageManager.getStage(args.get(0));
            if (stage == null) {
                Actions.message(player, "&cステージ'" + args.get(0) + "'が見つかりません");
                return;
            }
        }
        // 引数がなければ自動補完
        else {
            ArrayList<Game> startingGames = GameManager.getStartingGames();
            if (startingGames.size() <= 0) {
                Actions.message(player, "&c現在、始まっているゲームはありません！");
                return;
            } else if (startingGames.size() >= 2) {
                Actions.message(player, "&c複数のゲームが始まっています！観戦するステージを指定してください！");
                return;
            } else {
                stage = startingGames.get(0).getStage();
            }
        }

        Location specSpawn = stage.getSpecSpawn();
        if (specSpawn == null) {
            Actions.message(player, "&cステージ'" + stage.getName() + "'は観戦者のスポーン地点が設定されていません");
            return;
        }

        for (Game check : GameManager.getGames().values()) {
            if (check.getPlayerTeam(player) != null) {
                Actions.message(player, "&cあなたはゲーム'" + check.getName() + "'に参加しているため移動できません！");
                return;
            }
        }

        // テレポート
        if (!player.getWorld().equals(specSpawn.getWorld())) {
            PlayerManager.getProfile(player.getName()).setTpBackLocation(player.getLocation());
        }
        player.teleport(specSpawn, TeleportCause.PLUGIN);
        Actions.message(player, "&aステージ'" + stage.getName() + "'の観戦者スポーン地点へ移動しました！");
        Actions.message(player, "&2 '&6/flag leave&2' コマンドで元の地点へ戻ることができます！");
    }

    @Override
    public boolean permission() {
        return Perms.WATCH.has(sender);
    }
}
