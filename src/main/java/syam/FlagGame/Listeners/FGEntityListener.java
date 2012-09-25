package syam.FlagGame.Listeners;

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

import syam.FlagGame.FlagGame;
import syam.FlagGame.Enum.GameTeam;
import syam.FlagGame.Game.Game;
import syam.FlagGame.Game.GameManager;
import syam.FlagGame.Util.Actions;

public class FGEntityListener implements Listener{
	public static final Logger log = FlagGame.log;
	private static final String logPrefix = FlagGame.logPrefix;
	private static final String msgPrefix = FlagGame.msgPrefix;

	private final FlagGame plugin;

	public FGEntityListener(final FlagGame plugin){
		this.plugin = plugin;
	}

	/* 登録するイベントはここから下に */

	// プレイヤーがダメージを受けた
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onEntityDamageByEntity(final EntityDamageByEntityEvent event){
		Entity entity = event.getEntity();

		Player damager = null;
		Player attacker = null;

		// プレイヤー対プレイヤーの直接攻撃
		if ((event.getCause() == DamageCause.ENTITY_ATTACK) &&
				(entity instanceof Player) && (event.getDamager() instanceof Player)){
			// ゲーム用ワールドでなければ返す
			if (entity.getWorld() != Bukkit.getWorld(plugin.getConfigs().getGameWorld()))
				return;

			damager = (Player) entity; // 攻撃された人
			attacker = (Player) event.getDamager(); // 攻撃した人
		}
		// 矢・雪球・卵など
		else if((event.getCause() == DamageCause.PROJECTILE) &&
					(entity instanceof Player) && (event.getDamager() instanceof Projectile)){
			// ゲーム用ワールドでなければ返す
			if (entity.getWorld() != Bukkit.getWorld(plugin.getConfigs().getGameWorld()))
				return;

			// プレイヤーが打ったもの
			if (((Projectile) event.getDamager()).getShooter() instanceof Player){
				damager = (Player) entity; // 攻撃された人
				attacker = (Player) ((Projectile) event.getDamager()).getShooter(); // 攻撃した人
			}
		}

		// ダメージを受けたプレイヤー、与えたプレイヤーどちらかがnullなら何もしない
		if (damager == null || attacker == null)
			return;

		// 設定確認 チーム内PVPを無効にする設定が無効であれば何もしない
		if (!plugin.getConfigs().getDisableTeamPVP())
			return;

		// 存在するゲームを回す
		for (Game game : GameManager.games.values()){
			if (!game.isStarting()) continue;

			GameTeam damagerTeam = game.getPlayerTeam(damager);
			GameTeam attackerTeam = game.getPlayerTeam(attacker);

			// 同じチームの場合イベントをキャンセルする
			if (damagerTeam != null && attackerTeam != null && damagerTeam == attackerTeam){
				event.setDamage(0);
				event.setCancelled(true);
				Actions.message(null, attacker, "&c同じチームメンバーには攻撃できません！");
			}
		}
	}
}
