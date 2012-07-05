package syam.FlagGame.Game;

public enum FlagType {
	NORMAL (0, "ノーマル"), // ノーマルフラッグ
	IRON (1, "鉄"), // 鉄フラッグ
	GOLD (2, "金"), // 金フラッグ
	DIAMOND (3, "ダイヤモンド"), // ダイヤフラッグ
	;

	private int point;
	private String typeName;

	FlagType(int point, String typeName){
		/* TODO:
		 * ポイントを設定ファイルで変更可能にする場合は、
		 * この間にpointの値を書き換える？
		 */
		this.point = point;
		this.typeName = typeName;
	}

	/**
	 * このフラッグ種類のポイントを返す
	 * @return
	 */
	public int getPoint(){
		return point;
	}

	/**
	 * このフラッグ種類の名前を返す
	 * @return
	 */
	public String getTypeName(){
		return typeName;
	}
}

