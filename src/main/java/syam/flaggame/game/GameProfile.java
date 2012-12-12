/**
 * FlagGame - Package: syam.flaggame.game Created: 2012/09/01 17:40:17
 */
package syam.flaggame.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import syam.flaggame.FlagGame;
import syam.flaggame.database.Database;

/**
 * StageProfile (StageProfile.java)
 * 
 * @author syam
 */
public class GameProfile {
    // Logger
    public static final Logger log = FlagGame.log;
    private static final String logPrefix = FlagGame.logPrefix;
    private static final String msgPrefix = FlagGame.msgPrefix;

    private String stageName;
    private boolean loaded = false;

    /* mySQL stuff */
    private int stageID;

    /* Data */
    private Long lastplayed = 0L; // 最終ゲーム開始日時

    /* Stats */
    private int played = 0; // プレイ回数

    private int kill = 0; // Kill数
    private int death = 0; // Death数

    private int flag_place = 0; // Place数
    private int flag_break = 0; // Break数

    /**
     * コンストラクタ
     * 
     * @return
     */
    public GameProfile(String stageName) {
        this.stageName = stageName;

        loadMySQL();
    }

    /**
     * データベースからステージ情報を読み込み
     * 
     * @return 成功すればtrue 違えばfalse
     */
    public boolean loadMySQL() {
        Database db = FlagGame.getDatabases();

        // ステージID(DB割り当て)を読み出す
        stageID = db.getInt("SELECT `stage_id` FROM " + db.getTablePrefix() + "stages WHERE `stage_name` = ?", stageName);

        // テーブルにデータが無ければ新規レコードを作る
        if (stageID == 0) {
            db.write("INSERT INTO " + db.getTablePrefix() + "stages (`stage_name`) VALUES (?)", stageName);
            stageID = db.getInt("SELECT `stage_id` FROM " + db.getTablePrefix() + "stages WHERE `stage_name` = ?", stageName);
        }

        /* *** テーブルデータ読み込み *************** */
        HashMap<Integer, ArrayList<String>> stagesDatas = db.read("SELECT `lastplayed`, `played`, `place`, `break`, `kill`, `death` FROM " + db.getTablePrefix() + "stages WHERE `stage_id` = ?", stageID);
        ArrayList<String> dataValues = stagesDatas.get(1);

        if (dataValues == null) {
            log.severe(stageName + " stage does not exist in the stages table!");
            loaded = false;
            return false;
        } else {
            // データ読み出し
            this.lastplayed = Long.valueOf(dataValues.get(0));
            this.played = Integer.valueOf(dataValues.get(1));
            this.flag_place = Integer.valueOf(dataValues.get(2));
            this.flag_break = Integer.valueOf(dataValues.get(3));
            this.kill = Integer.valueOf(dataValues.get(4));
            this.death = Integer.valueOf(dataValues.get(5));
        }
        dataValues.clear();

        loaded = true;
        return true;
    }

    /**
     * ステージデータをMySQLデータベースに保存
     */
    public void save() {
        Database db = FlagGame.getDatabases();

        /* stagesテーブル */
        db.write("UPDATE " + db.getTablePrefix() + "stages SET " + "`lastplayed` = ?, `played` = ?, `place` = ?, `break` = ?, `kill` = ?, `death` = ? " + "WHERE `stage_id` = ?", lastplayed.intValue(), played, flag_place, flag_break, kill, death, stageID);
    }

    /* getter / setter */
    public int getStageID() {
        return stageID;
    }

    public String getStagename() {
        return stageName;
    }

    public boolean isLoaded() {
        return loaded;
    }

    /* Data */
    // lastplayed
    public void updateLastPlayedStage() {
        this.lastplayed = System.currentTimeMillis() / 1000;
    }

    public long getLastPlayedStage() {
        return this.lastplayed;
    }

    // played
    public void setPlayed(int played) {
        this.played = played;
    }

    public int getPlayed() {
        return this.played;
    }

    public void addPlayed() {
        this.played = this.played + 1;
    }

    // place
    public void setPlace(int flag_place) {
        this.flag_place = flag_place;
    }

    public int getPlace() {
        return this.flag_place;
    }

    public void addPlace() {
        this.flag_place = this.flag_place + 1;
    }

    // break
    public void setBreak(int flag_break) {
        this.flag_break = flag_break;
    }

    public int getBreak() {
        return this.flag_break;
    }

    public void addBreak() {
        this.flag_break = this.flag_break + 1;
    }

    // kill
    public void setKill(int kill) {
        this.kill = kill;
    }

    public int getKill() {
        return this.kill;
    }

    public void addKill() {
        this.kill = this.kill + 1;
    }

    // death
    public void setDeath(int death) {
        this.death = death;
    }

    public int getDeath() {
        return this.death;
    }

    public void addDeath() {
        this.death = this.death + 1;
    }
}
