package syam.FlagGame.Game;

/**
 * ゲームを行うためのチーム
 * @author syam
 *
 */
public enum GameTeam {
	RED ("赤", 35, 14), // 赤チーム
	BLUE ("青", 35, 11), // 青チーム
	;

	private String teamName;
	private int blockID;
	private byte blockData;

	GameTeam(String teamName, int blockID, int blockData){
		this.teamName = teamName;

		// 例外回避
		if (blockID < 0)
			blockID = 0;
		if (blockData < 0 || blockData > 127)
			blockData = 0;

		this.blockID = blockID;
		this.blockData = (byte) blockData;
	}

	/**
	 * このチームの名前を返す
	 * @return
	 */
	public String getTeamName(){
		return teamName;
	}

	/**
	 * このチームのブロックIDを返す
	 * @return
	 */
	public int getBlockID(){
		return blockID;
	}
	/**
	 * このチームのブロックデータ値を返す
	 * @return
	 */
	public byte getBlockData(){
		return blockData;
	}
}
