package syam.FlagGame.Database;

import syam.FlagGame.FlagGame;

public class MySQLReconnect implements Runnable{
	private final FlagGame plugin;

	public MySQLReconnect(final FlagGame plugin){
		this.plugin = plugin;
	}

	@Override
	public void run(){
		if (!Database.isConnected()){
			Database.conncet();
			if (Database.isConnected()){
				/**
				 * TODO:
				 *  ここでプレイヤーデータを保存する
				 */
			}
		}
	}
}
