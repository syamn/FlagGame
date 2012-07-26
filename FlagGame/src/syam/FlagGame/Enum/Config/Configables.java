package syam.FlagGame.Enum.Config;

/**
 * ゲーム毎に設定可能な設定を表す /flag set コマンドで使用する列挙クラス
 * @author syam
 */
public enum Configables {
	STAGE("ステージエリア", ConfigType.AREA), // エリア保護を行うステージ全体の領域設定
	BASE("拠点エリア", ConfigType.AREA), // 各チーム拠点の領域設定
	FLAG("フラッグ", ConfigType.MANAGER), // 各チーム拠点の領域設定
	CHEST("チェスト", ConfigType.MANAGER), // 各チーム拠点の領域設定
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
