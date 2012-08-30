package syam.FlagGame.Database;

import org.bukkit.entity.Player;

import syam.FlagGame.FlagGame;
import syam.FlagGame.FGPlayer.PlayerManager;

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
				// プレイヤーデータを更新
				PlayerManager.saveAll();
				PlayerManager.clearAll();

				for (Player player : plugin.getServer().getOnlinePlayers()){
					PlayerManager.addPlayer(player);
				}
			}
		}
	}
}
