package syam.flaggame.listener;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import syam.flaggame.FlagGame;
import syam.flaggame.enums.GameTeam;
import syam.flaggame.game.Game;
import syam.flaggame.manager.GameManager;
import syam.flaggame.util.Actions;

public class FGEntityListener implements Listener {
    public static final Logger log = FlagGame.log;
    private static final String logPrefix = FlagGame.logPrefix;
    private static final String msgPrefix = FlagGame.msgPrefix;

    private final FlagGame plugin;

    public FGEntityListener(final FlagGame plugin) {
        this.plugin = plugin;
    }

    /* 登録するイベントはここから下に */

    // プレイヤーがダメージを受けた
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();

        // ゲーム用ワールドでなければ返す
        if (entity.getWorld() != Bukkit.getWorld(plugin.getConfigs().getGameWorld())) return;

        Player damager = null;
        Player attacker = null;

        // プレイヤー対プレイヤーの直接攻撃
        if ((event.getCause() == DamageCause.ENTITY_ATTACK) && (entity instanceof Player) && (event.getDamager() instanceof Player)) {
            damager = (Player) entity; // 攻撃された人
            attacker = (Player) event.getDamager(); // 攻撃した人
        }
        // 矢・雪球・卵など
        else if ((event.getCause() == DamageCause.PROJECTILE) && (entity instanceof Player) && (event.getDamager() instanceof Projectile)) {
            // プレイヤーが打ったもの
            if (((Projectile) event.getDamager()).getShooter() instanceof Player) {
                damager = (Player) entity; // 攻撃された人
                attacker = (Player) ((Projectile) event.getDamager()).getShooter(); // 攻撃した人
            }
        }

        if (damager == null || attacker == null) return;

        // 設定確認 チーム内PVPを無効にする設定が無効であれば何もしない
        if (!plugin.getConfigs().getDisableTeamPVP()) return;

        // 存在するゲームを回す
        for (Game game : GameManager.getGames().values()) {
            if (!game.isStarting()) continue;
            
            if (game.isJoined(damager) && game.isJoined(attacker)){
                GameTeam damagerTeam = game.getPlayerTeam(damager);
                GameTeam attackerTeam = game.getPlayerTeam(attacker);
                
                boolean cancel = false;
                
                // 無敵時間
                if (game.getGodModeMap().containsKey(damager.getName())){
                    cancel = true;
                    Actions.message(attacker, "&cこのプレイヤーはリスポーン後の無敵時間中です！");
                }
                
                // 同じチームメンバー
                if (damagerTeam == attackerTeam) {
                    cancel = true;
                    Actions.message(attacker, "&c同じチームメンバーには攻撃できません！");
                }
                
                if (cancel){
                    event.setDamage(0);
                    event.setCancelled(true);
                }
                return;
            }
        }
    }

    // 体力が回復した
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityRegainHealth(final EntityRegainHealthEvent event) {
        // ゲーム用ワールドでなければ返す
        if (event.getEntity().getWorld() != Bukkit.getWorld(plugin.getConfigs().getGameWorld())) { return; }

        // 設定確認、プレイヤーならイベントキャンセル
        if (plugin.getConfigs().getDisableRegainHP()) {
            if (event.getEntity() instanceof Player) {
                event.setCancelled(true);
            }
        }
    }
}
