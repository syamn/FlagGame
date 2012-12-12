package syam.flaggame.player;

import java.util.logging.Logger;

import org.bukkit.entity.Player;

import syam.flaggame.FlagGame;
import syam.flaggame.game.Game;

public class FGPlayer {
    // Logger
    public static final Logger log = FlagGame.log;
    private static final String logPrefix = FlagGame.logPrefix;
    private static final String msgPrefix = FlagGame.msgPrefix;

    // プレイヤーデータ
    private Player player;
    private PlayerProfile profile;

    // 参加中のゲーム
    private Game game = null;

    /**
     * コンストラクタ
     * 
     * @param player
     */
    public FGPlayer(final Player player) {
        this.player = player;
        this.profile = new PlayerProfile(player.getName(), true);
    }

    /* getter / setter */
    public void setPlayingGame(Game game) {
        this.game = game;
    }

    public Game getPlayingGame() {
        return this.game;
    }

    public Player getPlayer() {
        return this.player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public PlayerProfile getProfile() {
        return this.profile;
    }
}
