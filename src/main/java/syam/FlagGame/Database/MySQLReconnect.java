package syam.FlagGame.Database;

import org.bukkit.entity.Player;

import syam.FlagGame.FlagGame;
import syam.FlagGame.FGPlayer.PlayerManager;
import syam.FlagGame.Game.GameManager;

public class MySQLReconnect implements Runnable{
	private final FlagGame plugin;

	public MySQLReconnect(final FlagGame plugin){
		this.plugin = plugin;
	}

	@Override
	public void run(){
		if (!Database.isConnected()){
			Database.connect();
			if (Database.isConnected()){
				// プレイヤープロファイルを更新
				PlayerManager.saveAll();
				PlayerManager.clearAll();

				for (Player player : plugin.getServer().getOnlinePlayers()){
					PlayerManager.addPlayer(player);
				}

				// ゲームステージプロファイルを保存
				GameManager.saveAll();
			}
		}
	}
}
