package syam.flaggame.enums;

public enum FlagType {
    NORMAL(0, "ノーマル", "&7"), // ノーマルフラッグ
    IRON(1, "鉄", "&3"), // 鉄フラッグ
    GOLD(2, "金", "&6"), // 金フラッグ
    DIAMOND(3, "ダイヤモンド", "&b"), // ダイヤフラッグ
    ;

    private int point;
    private String typeName;
    private String colorTag;

    FlagType(int point, String typeName, String colorTag) {
        /*
         * TODO: ポイントを設定ファイルで変更可能にする場合は、 この間にpointの値を書き換える？
         */
        this.point = point;
        this.typeName = typeName;
        this.colorTag = colorTag;
    }

    /**
     * このフラッグ種類のポイントを返す
     * 
     * @return
     */
    public int getPoint() {
        return point;
    }

    /**
     * このフラッグ種類の名前を返す
     * 
     * @return
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * フラッグの色タグ "&(char)" を返す
     * 
     * @return
     */
    public String getColor() {
        return colorTag;
    }
}
