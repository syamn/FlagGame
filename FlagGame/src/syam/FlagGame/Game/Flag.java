package syam.FlagGame.Game;

import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.block.Block;

import syam.FlagGame.FlagGame;

public class Flag {
	// Logger
	public static final Logger log = FlagGame.log;
	private static final String logPrefix = FlagGame.logPrefix;
	private static final String msgPrefix = FlagGame.msgPrefix;

	private final FlagGame plugin;

	/* フラッグデータ */
	private Game game = null; // フラッグが所属するゲーム

	private Location loc = null; // フラッグ座標
	private FlagType type = null; // フラッグの種類

	// 元のブロックデータ
	private int blockID = 0;
	private byte blockData = 0;

	/**
	 * コンストラクタ
	 * @param plugin
	 */
	public Flag(final FlagGame plugin, final Game game, final Location loc, final FlagType type, final int blockID, final byte blockData){
		this.plugin = plugin;

		// フラッグデータ登録
		this.game = game;
		this.loc = loc;
		this.type = type;

		Block block = loc.getBlock();
		this.blockID = block.getTypeId();
		this.blockData = block.getData();

		// ゲームに設定
		init();
	}

	public Flag(final FlagGame plugin, final Game game, final Location loc, final FlagType type){
		this(plugin, game, loc, type, 0, (byte) 0);
	}

	/**
	 * ゲームクラスにこのフラッグデータを登録
	 */
	private void init(){
		game.setFlag(loc, this);
	}

	/**
	 * 今のブロックデータを返す
	 * @return Block
	 */
	public Block getNowBlock(){
		return loc.getBlock();
	}

	/* フラッグ設定系 */
	/**
	 * このフラッグの点数を返す
	 * @return フラッグの点数
	 */
	public int getFlagPoint(){
		return type.getPoint();
		/*
		int point = 0;
		switch(type){
			case NORMAL:
				point = 0; break;
			case IRON:
				point = 1; break;
			case GOLD:
				point = 2; break;
			case DIAMOND:
				point = 3; break;
			default:
				log.warning(logPrefix+ "Undefined FlagPoint: "+ type.name());
				break;
		}
		return point;
		*/
	}

	public String getTypeName(){
		return type.getTypeName();
	}

	/* getter / setter */
	public Game getGame(){
		return game;
	}

	public Location getLocation(){
		return loc;
	}
	public FlagType getFlagType(){
		return type;
	}

	public int getOriginBlockID(){
		return blockID;
	}
	public byte getOriginBlockData(){
		return blockData;
	}
}
