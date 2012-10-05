package syam.flaggame.enums;

import syam.flaggame.exception.FlagGameException;


/**
 * ゲームチーム
 * @author syam
 */
public enum GameTeam {
	RED ("赤", 35, 14, FlagState.RED, "&c"), // 赤チーム
	BLUE ("青", 35, 11, FlagState.BLUE ,"&b"), // 青チーム
	;

	private String teamName;
	private int blockID;
	private byte blockData;
	private FlagState flagState;
	private String colorTag;

	GameTeam(String teamName, int blockID, int blockData, FlagState flagState, String colorTag){
		this.teamName = teamName;

		// 例外回避
		if (blockID < 0)
			blockID = 0;
		if (blockData < 0 || blockData > 127)
			blockData = 0;

		this.blockID = blockID;
		this.blockData = (byte) blockData;

		this.flagState = flagState;
		this.colorTag = colorTag;
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

	/**
	 * チームに関連付けられたフラッグステートを返す
	 * @return nullあり？
	 */
	public FlagState getFlagState(){
		return flagState;
	}

	/**
	 * チームの色タグ "&(char)" を返す
	 * @return
	 */
	public String getColor(){
		return colorTag;
	}

	/**
	 * 相手のGameTeamを返す
	 * @return GameTeam
	 */
	public GameTeam getAgainstTeam(final GameTeam team){
		if (team.equals(GameTeam.RED)){
			return GameTeam.BLUE;
		}
		else if(team.equals(GameTeam.BLUE)){
			return GameTeam.RED;
		}
		else{
			String error = "Request team is not defined";
			if (team != null) error += ": " + team.name();
			throw new FlagGameException(error);
		}
	}
}
