/**
 * FlagGame - Package: syam.FlagGame.Enum
 * Created: 2012/09/20 22:32:47
 */
package syam.FlagGame.Enum;

import org.bukkit.permissions.Permissible;

import syam.FlagGame.FlagGame;

/**
 * Permission (Permission.java)
 * @author syam(syamn)
 */
public enum Perms {
	/* 権限ノード */

	/* コマンド系 */
	// User Commands
	INFO			("user.info"),
	JOIN			("user.join"),
	LEAVE_GAME		("user.leave.game"),
	LEAVE_READY		("user.leave.ready"),
	LEAVE_SPECTATE	("user.leave.spectate"),
	STATS_SELF		("user.stats.self"),
	STATS_OTHER		("user.stats.other"),
	TOP				("user.top"),
	WATCH			("user.watch"),

	// Admin Commands
	CHECK	("admin.setup.check"),
	CREATE	("admin.setup.create"),
	DELETE	("admin.setup.delete"),
	READY	("admin.ready"),
	RELOAD	("admin.reload"),
	SAVE	("admin.save"),
	SELECT	("admin.select"),
	SET		("admin.setup.set"),
	START	("admin.start"),
	TP		("admin.tp"),

	/* 特殊系 */
	IGNORE_PROTECT	("ignoreWorldProtect"),
	IGNORE_INTERACT	("ignoreInteractEvent"),
	SIGN			("admin.sign"),
	;

	// ノードヘッダー
	final String HEADER = "flag.";
	private String node;

	/**
	 * コンストラクタ
	 * @param node 権限ノード
	 */
	Perms(final String node){
		this.node = HEADER + node;
	}

	/**
	 * 指定したプレイヤーが権限を持っているか
	 * @param player Permissible. Player, CommandSender etc
	 * @return boolean
	 */
	public boolean has(final Permissible perm){
		if (perm == null) return false;
		return perm.hasPermission(this.node);
	}

	/**
	 * 指定したプレイヤーが権限を持っているか(String)
	 * @param player PlayerName
	 * @return boolean
	 */
	public boolean has(final String playerName){
		if (playerName == null) return false;
		return has(FlagGame.getInstance().getServer().getPlayer(playerName));
	}
}
