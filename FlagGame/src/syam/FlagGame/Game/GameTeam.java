package syam.FlagGame.Game;

/**
 * ゲームを行うためのチーム
 * @author syam
 *
 */
public enum GameTeam {
	RED ("赤"), // 赤チーム
	BLUE ("青"), // 青チーム
	;

	private String teamName;

	GameTeam(String teamName){
		this.teamName = teamName;
	}

	/**
	 * このチームの名前を返す
	 * @return
	 */
	public String getTeamName(){
		return teamName;
	}
}
