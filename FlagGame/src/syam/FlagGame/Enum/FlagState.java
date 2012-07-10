package syam.FlagGame.Enum;


/**
 * フラッグの現在状態を表すクラス
 * @author syam
 *
 */
public enum FlagState {
	NONE (null), // 所有チームなし
	RED (GameTeam.RED), // 赤チーム所有
	BLUE (GameTeam.BLUE), // 青チーム所有
	;

	private GameTeam team;

	FlagState(GameTeam team){
		this.team = team;
	}

	/**
	 * 親チームを返す 所属なしの場合null
	 * @return nullあり
	 */
	public GameTeam getTeam(){
		return team;
	}

	/**
	 * 親チームのチーム名を返す
	 * @return nullあり
	 */
	public String getTeamName(){
		if (team == null) return null; // check null
		return team.getTeamName();
	}
}
