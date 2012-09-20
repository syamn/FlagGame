/**
 * FlagGame - Package: syam.FlagGame.Util
 * Created: 2012/09/20 23:59:40
 */
package syam.FlagGame.Permission;

import java.util.List;
import java.util.logging.Logger;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;
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
		VAULT,
		PEX,
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

	// 外部権限管理プラグイン
	private net.milkbowl.vault.permission.Permission vaultPermission = null; // 混同する可能性があるのでパッケージをimportしない
	private PermissionsEx pex = null;

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
			if ("vault".equalsIgnoreCase(pname)){
				if (setupVaultPermission()){
					usePermType = PermType.VAULT;
					break;
				}
			}
			else if ("pex".equals(pname)){
				if (setupPEXPermission()){
					usePermType = PermType.PEX;
					break;
				}
			}
			else if ("superperms".equalsIgnoreCase(pname)){
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
			log.info(logPrefix+ "Using " + getUsePermTypeString() + " for permissions");
		}
	}

	/**
	 * 指定したpermissibleが権限を持っているかどうか
	 * @param permissible Permissible. CommandSender, Player etc
	 * @param permission Node
	 * @return boolean
	 */
	public boolean has(final Permissible permissible, final String permission){
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
			// Vault
			case VAULT:
				return vaultPermission.has(player, permission);

			// PEX
			case PEX:
				return pex.has(player, permission);

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
	 * 指定したプレイヤー名が指定したワールドで権限を持っているかどうか
	 * @param worldName ワールド名
	 * @param playerName プレイヤー名
	 * @param permission 権限ノード
	 * @return boolean
	 */
	public boolean has(final String worldName, final String playerName, final String permission){
		// 使用中の権限プラグインによって処理を分ける
		switch (usePermType){
			// Vault
			case VAULT:
				return vaultPermission.has(worldName, playerName, permission);

			// PEX
			case PEX:
				PermissionUser user = PermissionsEx.getPermissionManager().getUser(playerName);
				if (user == null){ return false; }
				return user.has(permission, worldName);

			// SuperPerms
			case SUPERPERMS: {
				// SuperPermsはクロスワールドな権限システムではないので、このチェックは正しく動作しません
				// これに起因して不具合が発生するようなら、他の権限プラグインに乗り換えてください
				Player player = plugin.getServer().getPlayer(playerName);
				if (player == null) return false;
				else return player.hasPermission(permission);
			}
			// Ops
			case OPS:{
				Player player = plugin.getServer().getPlayer(playerName);
				if (player == null) return false;
				else return player.isOp();
			}
			// Other Types, forgot add here
			default:
				log.warning(logPrefix+ "Plugin author forgot add to integration to this permission plugin! Please report this!");
				return false;
		}
	}

	/**
	 * 指定したプレイヤー名のプライマリグループ名を取得する
	 * @param worldName ワールド名
	 * @param playerName プレイヤー名
	 * @return プライマリグループ名
	 */
	public String getPlayersGroup(final String worldName, final String playerName){
		// 使用中の権限プラグインによって処理を分ける
		switch (usePermType){
			// Vault
			case VAULT:
				return vaultPermission.getPrimaryGroup(worldName, playerName);

			// PEX
			case PEX:
				PermissionUser user = PermissionsEx.getPermissionManager().getUser(playerName);
				if (user == null){ return null; }
				String[] groups = user.getGroupsNames();
				if (groups != null && groups.length > 0){
					return groups[0];
				}else{
					return null;
				}

			// SuperPerms
			case SUPERPERMS: {
				// SuperPerms not support group
				return null;
			}
			// Ops
			case OPS:{
				// ops not support group
				return null;
			}
			// Other Types, forgot add here
			default:
				log.warning(logPrefix+ "Plugin author forgot add to integration to this permission plugin! Please report this!");
				return null;
		}
	}

	/**
	 * 使用中の権限管理システム名を返す
	 * @return string
	 */
	public String getUsePermTypeString(){
		// 使用中の権限プラグインによって処理を分ける
		switch (usePermType){
			// Vault
			case VAULT:
				return "Vault: " + Bukkit.getServer().getServicesManager().getRegistration(Permission.class).getProvider().getName();

			// PEX
			case PEX:
				return "PermissionsEx";

			// Ops
			case OPS:
				return "OPs";

			// SuperPerms And Other Types, forgot add here
			case SUPERPERMS:
			default:
				return "SuperPerms";
		}
	}

	// 権限管理プラグインセットアップメソッド ここから
	/**
	 * Vault権限管理システム セットアップ
	 * @return boolean
	 */
	private boolean setupVaultPermission(){
		Plugin vault = plugin.getServer().getPluginManager().getPlugin("Vault");
		if (vault == null) vault = plugin.getServer().getPluginManager().getPlugin("vault");
		if (vault == null) return false;
		try{
			RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> permissionProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
			if (permissionProvider != null){
				vaultPermission = permissionProvider.getProvider();
			}
		}catch (Exception ex){
			log.warning(logPrefix+ "Unexpected error trying to setup Vault permissions!");
			ex.printStackTrace();
		}

		return (vaultPermission != null);
	}
	/**
	 * PermissionsEx権限管理システム セットアップ
	 * @return boolean
	 */
	private boolean setupPEXPermission(){
		Plugin testPex = plugin.getServer().getPluginManager().getPlugin("PermissionsEx");
		if (testPex == null) testPex = plugin.getServer().getPluginManager().getPlugin("permissionsex");
		if (testPex == null) return false;
		try{
			pex = (PermissionsEx) testPex;
		}catch (Exception ex){
			log.warning(logPrefix+ "Unexpected error trying to setup PEX permissions!");
			ex.printStackTrace();
		}

		return (pex != null);
	}
	// ここまで

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
