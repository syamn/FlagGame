/**
 * FlagGame - Package: syam.FlagGame.Enum
 * Created: 2012/09/02 0:09:41
 */
package syam.FlagGame.Enum;

/**
 * PlayerStat (PlayerStat.java)
 * @author syam
 */
public enum PlayerStat {
	PLAYED("ゲーム参加回数", "played", "回"),
	EXIT("ゲーム途中離脱回数", "exit", "回"),

	WIN("勝利回数", "win", "回"),
	LOSE("敗北回数", "lose", "回"),
	DRAW("引き分け回数", "draw", "回"),

	PLACE("フラッグ設置回数", "place", "フラッグ"),
	BREAK("フラッグ破壊回数", "break", "フラッグ"),

	KILL("キル数", "kill", "Kill"),
	DEATH("デス数", "death", "Death"),
	;

	String description;
	String column;
	String suffix;

	PlayerStat(String desc, String col, String suffix){
		this.description = desc;
		this.column = col;
		this.suffix = suffix;
	}

	PlayerStat(String desc, String col){
		this(desc, col, "");
	}

	/**
	 * 内容の説明を返す
	 * @return description
	 */
	public String getDescription(){
		return this.description;
	}

	/**
	 * データベースのカラム名を返す
	 * @return column
	 */
	public String getColumnName(){
		return this.column;
	}

	/**
	 * 接尾語を返す 単位など
	 * @return suffix
	 */
	public String getSuffix(){
		return this.suffix;
	}

	/**
	 * 指定した項目があれば返す なければnull
	 * @param name 取得するPlayerStatの文字列
	 * @return PlayerStat or null
	 */
	public static PlayerStat getStat(String name){
		PlayerStat ret = null;

		for (PlayerStat ps : PlayerStat.values()){
			if (ps.name().equalsIgnoreCase(name)){
				ret = ps;
				break;
			}
		}

		return ret;
	}
}
