package syam.FlagGame.Enum.Config;

/**
 * ゲーム毎に設定可能な設定を表す /flag set コマンドで使用する列挙クラス
 * @author syam
 */
public enum Configables {
	// 一般
	STAGE("ステージエリア", ConfigType.AREA), // エリア保護を行うステージ全体の領域設定
	BASE("拠点エリア", ConfigType.AREA), // 各チーム拠点の領域設定
	SPAWN("スポーン地点", ConfigType.POINT), // 各チームのスポーン地点
	FLAG("フラッグ", ConfigType.MANAGER), // 各チーム拠点の領域設定
	CHEST("チェスト", ConfigType.MANAGER), // 各チーム拠点の領域設定

	SPECSPAWN("観戦者スポーン地点", ConfigType.POINT), // 観戦時にテレポートする位置 SPECTATE / SPEC / SSPAWN ..etc?

	// オプション
	GAMETIME ("ゲームの制限時間(秒)", ConfigType.SIMPLE),
	TEAMLIMIT ("チーム毎の人数制限", ConfigType.SIMPLE),
	AWARD ("勝利チームへの賞金", ConfigType.SIMPLE),
	ENTRYFEE ("参加料", ConfigType.SIMPLE),
	PROTECT ("ステージ保護", ConfigType.SIMPLE),
	AVAILABLE ("ステージ有効", ConfigType.SIMPLE),
	;

	private String configName;
	private ConfigType configType;

	Configables(String configName, ConfigType configType){
		this.configName = configName;
		this.configType = configType;
	}

	/**
	 * 設定名を返す
	 * @return String
	 */
	public String getConfigName(){
		return this.configName;
	}

	/**
	 * 設定種類を返す
	 * @return ConfigType
	 */
	public ConfigType getConfigType(){
		return this.configType;
	}
}
