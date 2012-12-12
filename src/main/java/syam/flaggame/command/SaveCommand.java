package syam.flaggame.command;

import syam.flaggame.manager.StageManager;
import syam.flaggame.permission.Perms;
import syam.flaggame.player.PlayerManager;
import syam.flaggame.util.Actions;

public class SaveCommand extends BaseCommand {
    public SaveCommand() {
        bePlayer = false;
        name = "save";
        argLength = 0;
        usage = "<- save map data";
    }

    @Override
    public void execute() {
        // データ保存
        plugin.getFileManager().saveStages();
        StageManager.saveAll();
        PlayerManager.saveAll();

        Actions.message(sender, "&aStages/Players Saved!");
    }

    @Override
    public boolean permission() {
        return Perms.SAVE.has(sender);
    }
}
