package syam.flaggame.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import syam.flaggame.FlagGame;
import syam.flaggame.database.Database;

public class PlayerProfile {
    // Logger
    public static final Logger log = FlagGame.log;
    private static final String logPrefix = FlagGame.logPrefix;
    private static final String msgPrefix = FlagGame.msgPrefix;

    private String playerName;
    private boolean loaded = false;

    /* mySQL Stuff */
    private int playerID;

    /* Data */
    private Long lastjoingame = 0L; // 最終ゲーム参加日時
    private int status = 0; // プレイヤーステータス

    private Location backLocation = null; // 戻る先の地点

    /* Records */

    private int played = 0; // プレイ回数
    private int exit = 0; // 途中退場回数

    private int win = 0; // win
    private int lose = 0; // lose
    private int draw = 0; // draw

    private int flag_place = 0; // Place数
    private int flag_break = 0; // Break数

    private int kill = 0; // Kill数
    private int death = 0; // Death数

    /**
     * コンストラクタ
     * 
     * @param playerName
     *            プレイヤー名
     * @param addNew
     *            新規プレイヤーとしてデータを読み込むかどうか
     */
    public PlayerProfile(String playerName, boolean addNew) {
        this.playerName = playerName;

        if (!loadMySQL() && addNew) {
            addMySQLPlayer();
            loaded = true;
        }
    }

    /**
     * データベースからプレイヤーデータを読み込み
     * 
     * @return 正常終了すればtrue、基本データテーブルにデータがなければfalse
     */
    public boolean loadMySQL() {
        Database db = FlagGame.getDatabases();

        // プレイヤーID(DB割り当て)を読み出す
        playerID = db.getInt("SELECT `player_id` FROM " + db.getTablePrefix() + "users WHERE `player_name` = ?", playerName);

        // プレイヤー基本テーブルにデータがなければ何もしない
        if (playerID == 0) { return false; }

        /* *** usersテーブルデータ読み込み *************** */
        HashMap<Integer, ArrayList<String>> usersDatas = db.read("SELECT `lastjoingame`, `status` FROM " + db.getTablePrefix() + "users WHERE `player_id` = ?", playerID);
        ArrayList<String> dataValues = usersDatas.get(1);

        if (dataValues == null) {
            log.severe(playerName + "does not exist in the users table! (user: " + playerName + ")");
            return false;
        } else {
            // データ読み出し
            this.lastjoingame = Long.valueOf(dataValues.get(0));
            this.status = Integer.valueOf(dataValues.get(1));
        }
        dataValues.clear();

        /* *** recordsテーブルデータ読み込み *************** */
        HashMap<Integer, ArrayList<String>> recordsDatas = db.read("SELECT `played`, `exit`, `win`, `lose`, `draw`, `place`, `break`, `kill`, `death` FROM " + db.getTablePrefix() + "records " + "WHERE `player_id` = ?", playerID);
        dataValues = recordsDatas.get(1);

        if (dataValues == null) {
            // 新規レコード追加
            db.write("INSERT INTO " + db.getTablePrefix() + "records (`player_id`) VALUES (?)", playerID);
            log.warning(playerName + " does not exist in the records table. Their records will be reset.");
        } else {
            // データ読み出し
            this.played = Integer.valueOf(dataValues.get(0));
            this.exit = Integer.valueOf(dataValues.get(1));
            this.win = Integer.valueOf(dataValues.get(2));
            this.lose = Integer.valueOf(dataValues.get(3));
            this.draw = Integer.valueOf(dataValues.get(4));
            this.kill = Integer.valueOf(dataValues.get(5));
            this.death = Integer.valueOf(dataValues.get(6));
            this.flag_place = Integer.valueOf(dataValues.get(7));
            this.flag_break = Integer.valueOf(dataValues.get(8));
        }
        dataValues.clear();

        /* *** tpbacksテーブルデータ読み込み *************** */
        HashMap<Integer, ArrayList<String>> tpbacksDatas = db.read("SELECT `world`, `x`, `y`, `z`, `pitch`, `yaw` FROM " + db.getTablePrefix() + "tpbacks WHERE `player_id` = ?", playerID);
        dataValues = tpbacksDatas.get(1);

        if (dataValues == null) {
            // 帰る先の地点なし
            backLocation = null;
        } else {
            // データ読み出し
            World world = Bukkit.getWorld(dataValues.get(0));
            if (world == null) {
                log.warning(logPrefix + "World " + dataValues.get(0) + " is Not Found! Removing this location.");
                backLocation = null;
            } else {
                double x, y, z;
                Float pitch = null, yaw = null;

                x = Double.valueOf(dataValues.get(1));
                y = Double.valueOf(dataValues.get(2));
                z = Double.valueOf(dataValues.get(3));

                if (dataValues.get(4) == null || dataValues.get(5) == null) {
                    this.backLocation = new Location(world, x, y, z);

                } else {
                    pitch = Float.valueOf(dataValues.get(4));
                    yaw = Float.valueOf(dataValues.get(5));

                    this.backLocation = new Location(world, x, y, z, pitch, yaw);
                }
            }
        }

        // 読み込み正常終了
        loaded = true;
        return true;
    }

    /**
     * 新規ユーザーデータをMySQLデータベースに追加
     */
    private void addMySQLPlayer() {
        Database db = FlagGame.getDatabases();

        db.write("INSERT INTO " + db.getTablePrefix() + "users (`player_name`) VALUES (?)", playerName); // usersテーブル
        playerID = db.getInt("SELECT `player_id` FROM " + db.getTablePrefix() + "users WHERE `player_name` = ?", playerName);
        db.write("INSERT INTO " + db.getTablePrefix() + "records (`player_id`) VALUES (?)", playerID); // recordsテーブル
    }

    /**
     * プレイヤーデータをMySQLデータベースに保存
     */
    public void save() {
        // Long timestamp = System.currentTimeMillis() / 1000;

        Database db = FlagGame.getDatabases();

        // データベースupdate

        /* usersテーブル */
        db.write("UPDATE " + db.getTablePrefix() + "users SET " + "`lastjoingame` = ?, `status` = ? " + "WHERE `player_id` = ?", lastjoingame.intValue(), status, playerID);

        /* recordsテーブル */
        db.write("UPDATE " + db.getTablePrefix() + "records SET " + "`played` = ?, `exit` = ?, `win` = ?, `lose` = ?, `draw` = ?, `place` = ?, `break` = ?, `kill` = ?, `death` = ? " + "WHERE `player_id` = ?", played, exit, win, lose, draw, flag_place, flag_break, kill, death, playerID);

        /* tpbacksテーブル */
        if (backLocation != null) {
            db.write("REPLACE INTO " + db.getTablePrefix() + "tpbacks SET " + "`player_id` = ?, `world` = ?, `x` = ?, `y` = ?, `z` = ?, `pitch` = ?, `yaw` = ?", playerID, backLocation.getWorld().getName(), backLocation.getX(), backLocation.getY(), backLocation.getZ(), backLocation.getPitch(), backLocation.getYaw());
        } else {
            db.write("DELETE FROM " + db.getTablePrefix() + "tpbacks WHERE `player_id` = ?", playerID);
        }
    }

    /* 特殊データ算出 */
    public double getKD() {
        // kill, death どちらかが0ならk/dは0を返す
        if (kill <= 0 || death <= 0) {
            return 0.0D;
        } else {
            double kd = (double) kill / (double) death;
            return kd;
        }
    }

    public String getKDstring() {
        double kd = getKD();
        String cc = "&7"; // 灰色 (1.0 or 0.0)
        if (kd > 1.0D) {
            cc = "&a"; // 緑色 (1+)
        } else if (kd < 1.0D && kd != 0.0D) {
            cc = "&c"; // 赤色 (1-)
        }
        return cc + String.format("%.3f", kd);
    }

    /* getter / setter */
    public int getPlayerID() {
        return playerID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean isLoaded() {
        return loaded;
    }

    /* Data */

    // lastjoingame
    public void updateLastJoinedGame() {
        this.lastjoingame = System.currentTimeMillis() / 1000;
    }

    public long getLastJoinedGame() {
        return this.lastjoingame;
    }

    // status
    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return this.status;
    }

    // tpBackLocation
    public void setTpBackLocation(Location loc) {
        this.backLocation = loc;
    }

    public Location getTpBackLocation() {
        return this.backLocation;
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

    // exit
    public void setExit(int exit) {
        this.exit = exit;
    }

    public int getExit() {
        return this.exit;
    }

    public void addExit() {
        this.exit = this.exit + 1;
    }

    // win
    public void setWin(int win) {
        this.win = win;
    }

    public int getWin() {
        return this.win;
    }

    public void addWin() {
        this.win = this.win + 1;
    }

    // lose
    public void setLose(int lose) {
        this.lose = lose;
    }

    public int getLose() {
        return this.lose;
    }

    public void addLose() {
        this.lose = this.lose + 1;
    }

    // draw
    public void setDraw(int draw) {
        this.draw = draw;
    }

    public int getDraw() {
        return this.draw;
    }

    public void addDraw() {
        this.draw = this.draw + 1;
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
