package syam.FlagGame.Enum.Config;

/**
 * Configable列挙に紐付ける設定種類を表す列挙クラスです
 * @author syam
 */
public enum ConfigType {
	AREA, // エリア指定を行う設定
	POINT, // プレイヤーの現在値を取得する設定
	MANAGER, // マネージャモードに入る設定
	SIMPLE, // お金など単にそのコマンドだけで変更可能な設定
	;
}
