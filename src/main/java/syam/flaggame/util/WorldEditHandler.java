package syam.flaggame.util;

import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import syam.flaggame.FlagGame;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.CuboidRegionSelector;
import com.sk89q.worldedit.regions.RegionSelector;

/**
 * WorldEditの選択領域を取得するためのWorldEditハンドラ
 * 
 * @author syam
 * 
 */
public class WorldEditHandler {
    // Logger
    public static final Logger log = FlagGame.log;
    private static final String logPrefix = FlagGame.logPrefix;
    private static final String msgPrefix = FlagGame.msgPrefix;

    /**
     * WorldEditプラグインインスタンスを返す
     * 
     * @param bPlayer
     *            BukkitPlayer
     * @return WorldEditPlugin or null
     */
    private static WorldEditPlugin getWorldEdit(final Player bPlayer) {
        // WorldEditプラグイン取得
        Plugin plugin = FlagGame.getInstance().getServer().getPluginManager().getPlugin("WorldEdit");

        // プラグインが見つからない
        if (plugin == null) {
            if (bPlayer != null && bPlayer.isOnline()) Actions.message(bPlayer, msgPrefix + "&cWorldEdit is not loaded!");
            return null;
        }

        return (WorldEditPlugin) plugin;
    }

    /**
     * WorldEditがインストールされ使用可能かどうかチェックする
     * 
     * @return 使用可能ならtrue, 違えばfalse
     */
    public static boolean isAvailable() {
        WorldEditPlugin we = getWorldEdit(null);
        if (we == null) {
            return false;
        } else {
            return we.isEnabled();
        }
    }

    /**
     * 指定したプレイヤーが選択中のWorldEdit領域を取得する
     * 
     * @param bPlayer
     *            WorldEditで領域を指定しているプレイヤー
     * @return 選択された領域の両端のブロック配列[2] エラーならnull
     */
    @SuppressWarnings("deprecation")
    public static Block[] getWorldEditRegion(final Player bPlayer) {
        WorldEditPlugin we = getWorldEdit(bPlayer);
        if (we == null) return null;

        LocalPlayer player = new BukkitPlayer(we, we.getServerInterface(), bPlayer);
        LocalSession session = we.getWorldEdit().getSession(player);

        // セレクタが立方体セレクタか判定
        if (!(session.getRegionSelector() instanceof CuboidRegionSelector)) {
            Actions.message(bPlayer, msgPrefix + "&cFlagGame supports only cuboid regions!");
            return null;
        }

        CuboidRegionSelector selector = (CuboidRegionSelector) session.getRegionSelector();

        try {
            CuboidRegion region = selector.getRegion();

            // 選択範囲の端と端のブロックを格納する配列
            Block[] corners = new Block[2];

            Vector v1 = region.getPos1();
            Vector v2 = region.getPos2();

            corners[0] = bPlayer.getWorld().getBlockAt(v1.getBlockX(), v1.getBlockY(), v1.getBlockZ());
            corners[1] = bPlayer.getWorld().getBlockAt(v2.getBlockX(), v2.getBlockY(), v2.getBlockZ());

            // 角のブロック配列[2]を返す
            return corners;
        } catch (IncompleteRegionException ex) {
            // 正しく領域が選択されていない例外
            Actions.message(bPlayer, msgPrefix + "&cWorldEdit region is not fully selected!");
        } catch (Exception ex) {
            // その他一般例外
            log.warning(logPrefix + "Error while retreiving WorldEdit region: " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 指定したプレイヤーでWorldEditの領域を設定する
     * 
     * @param bPlayer
     *            BukkitPlayer
     * @param pos1
     *            Location Pos1
     * @param pos2
     *            Location Pos2
     * @return 成功すればtrue, 失敗すればfalse
     */
    public static boolean selectWorldEditRegion(final Player bPlayer, final Location pos1, final Location pos2) {
        // 不正な引数
        if (bPlayer == null || pos1 == null || pos2 == null) { return false; }
        if (!pos1.getWorld().equals(pos2.getWorld()) || !pos1.getWorld().equals(bPlayer.getWorld())) { return false; }

        WorldEditPlugin we = getWorldEdit(bPlayer);
        if (we == null) return false;

        LocalPlayer player = new BukkitPlayer(we, we.getServerInterface(), bPlayer);
        LocalSession session = we.getWorldEdit().getSession(player);

        try {
            CuboidRegionSelector selector = new CuboidRegionSelector(player.getWorld());

            selector.selectPrimary(new Vector(pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ()));
            selector.selectSecondary(new Vector(pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ()));

            session.setRegionSelector(player.getWorld(), selector);
            session.dispatchCUISelection(player);
        } catch (Exception ex) {
            // 一般例外
            log.warning(logPrefix + "Error while selecting WorldEdit region: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * 指定したプレイヤーでWorldEditの領域を設定する
     * 
     * @param bPlayer
     *            BukkitPlayer
     * @param pos1
     *            Block pos1
     * @param pos2
     *            Block Pos2
     * @return 成功すればtrue, 失敗すればfalse
     */
    public static boolean selectWorldEditRegion(final Player bPlayer, final Block pos1, final Block pos2) {
        if (pos1 == null || pos2 == null) return false;
        return selectWorldEditRegion(bPlayer, pos1.getLocation(), pos2.getLocation());
    }
}
