/**
 * FlagGame - Package: syam.FlagGame.Util
 * Created: 2012/09/20 23:59:40
 */
package syam.FlagGame.Permission;

import java.util.List;
import java.util.logging.Logger;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

import syam.FlagGame.FlagGame;

/**
 * PermissionHandler (PermissionHandler.java)
 * @author syam(syamn)
 */
public class PermissionHandler {
	/**
	 * 対応している権限管理プラグインの列挙
	 * Type (PermissionHandler.java)
	 * @author syam(syamn)
	 */
	public enum PermType {
		SUPERPERMS,
		OPS,
		;
	}

	// Logger
	private static final Logger log = FlagGame.log;
	private static final String logPrefix = FlagGame.logPrefix;
	private static final String msgPrefix = FlagGame.msgPrefix;

	// シングルトンインスタンス
	private static PermissionHandler instance;

	private final FlagGame plugin;
	private PermType usePermType = null;
	//private Debug debug = null;

	// TODO: write here..

	/**
	 * コンストラクタ
	 * @param plugin FlagGameプラグイン
	 */
	private PermissionHandler(final FlagGame plugin){
		this.plugin = plugin;
		instance = this;
	}

	/**
	 * 権限管理プラグインをセットアップする
	 * @param debug デバッグモードかどうか
	 */
	public void setupPermissions(final boolean message){
		List<String> prefs = plugin.getConfigs().getPermissions();

		// 一致する権限管理プラグインを取得 上にあるほど高優先度
		for (String pname : prefs){
			if ("superperms".equalsIgnoreCase(pname)){
				usePermType = PermType.SUPERPERMS;
				break;
			}
			else if ("ops".equalsIgnoreCase(pname)){
				usePermType = PermType.OPS;
				break;
			}
		}

		// デフォルトはSuperPerms リストに有効な記述が無かった場合
		if (usePermType == null){
			usePermType = PermType.SUPERPERMS;
			if (message){
				log.warning(logPrefix+ "Valid permissions name not selected! Using SuperPerms for permissions.");
			}
		}

		// メッセージ送信
		if (message){
			log.info(logPrefix+ "Using " + usePermType.name() + " for permissions");
		}
	}

	/**
	 * 指定したpermissibleが権限を持っているかどうか
	 * @param permissible Permissible. CommandSender, Player etc
	 * @param permission Node
	 * @return boolean
	 */
	public boolean has(Permissible permissible, final String permission){
		// コンソールは常にすべての権限を保有する
		if (permissible instanceof ConsoleCommandSender){
			return true;
		}
		// プレイヤーでもなければfalseを返す
		Player player = null;
		if (permissible instanceof Player){
			player = (Player) permissible;
		}else{
			return false;
		}

		// 使用中の権限プラグインによって処理を分ける
		switch (usePermType){
			// SuperPerms
			case SUPERPERMS:
				return player.hasPermission(permission);

			// Ops
			case OPS:
				return player.isOp();

			// Other Types, forgot add here
			default:
				log.warning(logPrefix+ "Plugin author forgot add to integration to this permission plugin! Please report this!");
				return false;
		}
	}

	/**
	 * 使用している権限管理プラグインを返す
	 * @return PermType
	 */
	public PermType getUsePermType(){
		return usePermType;
	}

	/**
	 * シングルトンインスタンスを返す
	 * @return PermissionHandler
	 */
	public static PermissionHandler getInstance(){
		if (instance == null){
			synchronized (PermissionHandler.class) {
				if (instance == null){
					instance = new PermissionHandler(FlagGame.getInstance());
				}
			}
		}
		return instance;
	}
}
