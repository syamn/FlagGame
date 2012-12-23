package syam.flaggame.listener;

import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import syam.flaggame.FlagGame;
import syam.flaggame.enums.FlagType;
import syam.flaggame.enums.GameTeam;
import syam.flaggame.enums.SignAction;
import syam.flaggame.enums.config.Configables;
import syam.flaggame.game.Flag;
import syam.flaggame.game.Game;
import syam.flaggame.game.Stage;
import syam.flaggame.manager.GameManager;
import syam.flaggame.manager.SetupManager;
import syam.flaggame.manager.StageManager;
import syam.flaggame.permission.Perms;
import syam.flaggame.player.PlayerManager;
import syam.flaggame.util.Actions;
import syam.flaggame.util.Cuboid;

public class FGPlayerListener implements Listener {
    public static final Logger log = FlagGame.log;
    private static final String logPrefix = FlagGame.logPrefix;
    private static final String msgPrefix = FlagGame.msgPrefix;

    private final FlagGame plugin;

    public FGPlayerListener(final FlagGame plugin) {
        this.plugin = plugin;
    }

    /* 登録するイベントはここから下に */

    // プレイヤーがブロックをクリックした
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block == null) { return; }

        // 管理モードで権限を持ち、かつ設定したツールでブロックを右クリックした
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && SetupManager.getManager(player) != null && player.getItemInHand().getTypeId() == plugin.getConfigs().getToolID() && Perms.SET.has(player)) {
            Configables conf = SetupManager.getManager(player);
            Stage stage = SetupManager.getSelectedStage(player);
            if (stage == null) {
                Actions.message(player, "&c先に編集するゲームを選択してください！");
                return;
            }

            Location loc = block.getLocation();

            // ゲーム用ワールドでなければ返す
            if (loc.getWorld() != Bukkit.getWorld(plugin.getConfigs().getGameWorld())) {
                Actions.message(player, "&cここはゲーム用ワールドではありません！");
                return;
            }

            switch (conf) {
            // フラッグモード
                case FLAG:
                    // 既にフラッグブロックなら解除する
                    if (stage.isFlag(loc)) {
                        stage.removeFlag(loc);
                        Actions.message(player, "&aステージ'" + stage.getName() + "'のフラッグを削除しました！");
                        return;
                    }

                    // フラッグタイプを取得
                    FlagType type = SetupManager.getSelectedFlagType(player);
                    if (type == null) {
                        Actions.message(player, "&cフラッグの種類が指定されていません！");
                        return;
                    }

                    // 新規フラッグ登録
                    new Flag(plugin, stage, loc, type, 0, (byte) 0);
                    Actions.message(player, "&aステージ'" + stage.getName() + "'の" + type.getTypeName() + "フラッグを登録しました！");
                    break;

                // チェストモード
                case CHEST:
                    // チェスト、かまど、ディスペンサーのどれかでなければ返す
                    if (block.getType() != Material.CHEST && block.getType() != Material.FURNACE && block.getType() != Material.DISPENSER) {
                        Actions.message(player, "&cこのブロックはコンテナインターフェースを持っていません！");
                        return;
                    }
                    // 既にチェストブロックになっているか判定
                    if (stage.isChest(loc)) {
                        // 削除
                        stage.removeChest(loc);
                        Actions.message(player, "&aステージ'" + stage.getName() + "'のチェストを削除しました！");
                    } else {
                        // 選択
                        stage.setChest(loc);
                        Actions.message(player, "&aステージ'" + stage.getName() + "'のチェストを設定しました！");
                    }
                    break;

                // 他は何もしない
                default:
                    break;
            }

            event.setCancelled(true);
            event.setUseInteractedBlock(Result.DENY);
            event.setUseItemInHand(Result.DENY);
        }

        // 看板を右クリックした
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            // 1行目チェック
            if (sign.getLine(0).equals("§a[FlagGame]")) {
                clickFlagSign(player, block);
            }
        }
    }

    // プレイヤーがブロックをクリックした
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerOpen(final PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        // ゲーム用ワールドでなければ返す
        if (block.getWorld() != Bukkit.getWorld(plugin.getConfigs().getGameWorld())) return;

        if (block != null) {
            Material type = block.getType();
            // ブロックを左または右クリックした
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                // クリックしたブロックがドア関係
                if (type == Material.WOODEN_DOOR || type == Material.IRON_DOOR || type == Material.FENCE_GATE) {
                    // 使用可能かチェック
                    if (!canUseBlock(player, block, true, true)) {
                        event.setUseInteractedBlock(Result.DENY);
                        event.setUseItemInHand(Result.DENY);
                        event.setCancelled(true);
                    }
                    return;
                }
            }

            // ブロックを右クリックした
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                // ブロックがコンテナ関係か看板関係
                if (type == Material.CHEST || type == Material.FURNACE || type == Material.DISPENSER || type == Material.JUKEBOX) {
                    // 使用可能かチェック
                    if (!canUseBlock(player, block, true, false)) {
                        event.setUseInteractedBlock(Result.DENY);
                        event.setUseItemInHand(Result.DENY);
                        event.setCancelled(true);
                    }
                    return;
                }
            }
        }
    }

    // プレイヤーがリスポーンした
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // プレイヤーがフラッグワールド以外なら何もしない
        if (player.getWorld() != Bukkit.getWorld(plugin.getConfigs().getGameWorld())) { return; }

        // ゲーム参加チェック
        for (Game game : GameManager.getGames().values()) {
            // 開始されていないゲームはチェックしない
            if (!game.isStarting()) continue;

            GameTeam team = game.getPlayerTeam(player);
            if (team != null) {
                Location loc = game.getStage().getSpawn(team);
                if (loc == null) {
                    // 所属チームのスポーン地点設定なし
                    Actions.message(player, msgPrefix + "&cあなたのチームのスポーン地点が設定されていません");
                    log.warning(logPrefix + "Player " + player.getName() + " died, But undefined spawn-location. Game: " + game.getStage().getName() + " Team: " + team.name());

                    event.setRespawnLocation(Bukkit.getWorld(plugin.getConfigs().getGameWorld()).getSpawnLocation());
                } else {
                    // 設定あり
                    Actions.message(player, msgPrefix + "&c[*]&6このゲームはあと &a" + Actions.getTimeString(game.getRemainTime()) + "&6 残っています！");

                    event.setRespawnLocation(loc);
                    player.getInventory().setHelmet(new ItemStack(team.getBlockID(), 1, (short) 0, team.getBlockData()));
                }
                // リスポン後無敵時間設定
                game.getGodModeMap().put(player.getName(), System.currentTimeMillis() / 1000);
                
                return; // 複数ゲーム所属はあり得ないのでここで返す
            }
        }

        // フラッグゲームワールドスポーンに戻す
        event.setRespawnLocation(Bukkit.getWorld(plugin.getConfigs().getGameWorld()).getSpawnLocation());
    }

    // プレイヤーがコマンドを使おうとした
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        // ワールドチェック
        if (player.getWorld() != Bukkit.getWorld(plugin.getConfigs().getGameWorld())) return;

        String cmdMsg = event.getMessage().trim();
        String cmds[] = cmdMsg.split(" ");
        String cmd = null;

        if (cmds.length > 1) {
            cmd = cmds[0].trim();
        } else { // cmds.length == 1
            cmd = cmdMsg;
        }

        // 存在するゲームを回す
        for (Game game : GameManager.getGames().values()) {
            if (game.isJoined(player)) {
                // ゲーム中のプレイヤー 禁止コマンドを操作
                for (String s : plugin.getConfigs().getDisableCommands()) {
                    // 禁止コマンドと同じコマンドがある
                    if (s.trim().equalsIgnoreCase(cmd)) {
                        // コマンド実行キャンセル
                        event.setCancelled(true);
                        Actions.message(player, msgPrefix + "このコマンドは試合中に使えません！");
                        return;
                    }
                }
            }
        }
    }

    // プレイヤーが死んだ
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDeath(final PlayerDeathEvent event) {
        Player deader = event.getEntity();

        if (deader.getWorld() != Bukkit.getWorld(plugin.getConfigs().getGameWorld())) { return; }

        // ゲームワールドでは通常の死亡メッセージ非表示
        event.setDeathMessage(null);
        String deathMsg = "";

        EntityDamageEvent cause = event.getEntity().getLastDamageCause();
        Player killer = null;

        // 死亡理由不明は何もしない
        if (cause == null) return;

        // エンティティによって殺された
        if (cause instanceof EntityDamageByEntityEvent) {
            Entity killerEntity = ((EntityDamageByEntityEvent) cause).getDamager();

            // プレイヤーによって直接殺された
            if (killerEntity instanceof Player) {
                killer = (Player) killerEntity;
            }
            // プレイヤーによって発射された物(矢など)によって殺された
            else if (killerEntity instanceof Projectile && ((Projectile) killerEntity).getShooter() instanceof Player) {
                killer = (Player) ((Projectile) killerEntity).getShooter();
            }
        }

        // 存在するゲームを回す
        for (Game game : GameManager.getGames().values()) {
            if (!game.isStarting()) continue;

            // ダメージを受けたプレイヤーがゲームに参加しているプレイヤーか
            if (game.isJoined(deader)) {
                PlayerManager.getProfile(deader.getName()).addDeath(); // death数追加
                game.getStage().getProfile().addDeath();

                // 頭の羊毛ブロックをドロップさせない
                ItemStack helmet = deader.getInventory().getHelmet();
                if (helmet != null && helmet.getType() == Material.WOOL) {
                    deader.getInventory().setHelmet(null);
                }
            } else {
                continue;
            }

            // プレイヤーによって殺され、そのプレイヤーが同じゲームに参加しているか
            if (killer == null) return;

            if (game.isJoined(killer)) {
                GameTeam aTeam = game.getPlayerTeam(killer);
                GameTeam dTeam = game.getPlayerTeam(deader);

                deathMsg = msgPrefix + "&6[" + game.getStage().getName() + "] " + aTeam.getColor() + killer.getName() + "&6 が " + dTeam.getColor() + deader.getName() + "&6 を倒しました！";
                Actions.worldcastMessage(Bukkit.getWorld(plugin.getConfigs().getGameWorld()), deathMsg);
                // game.message(deathMsg); ワールドキャストでも邪魔ならこっちでゲーム参加者にだけキャスト

                // チームキル数追加
                game.addKillCount(aTeam);

                PlayerManager.getProfile(killer.getName()).addKill(); // kill数追加
                game.getStage().getProfile().addKill();

                game.log(" Player (" + aTeam.name() + ")" + killer.getName() + " Killed (" + dTeam.name() + ")" + deader.getName() + "!");
                return;
            }
        }
    }

    // プレイヤーがログアウトした
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        Player player = event.getPlayer();

        /* TODO: GC here */

        for (Game game : GameManager.getGames().values()) {
            if (!game.isStarting()) continue;

            // チームに所属していてこの設定が有効なら、アナウンスしてHPをゼロにする
            if (game.isJoined(player) && plugin.getConfigs().getDeathWhenLogout()) {
                player.setHealth(0);

                GameTeam team = game.getPlayerTeam(player);
                String deathMsg = msgPrefix + team.getColor() + team.getTeamName() + "チーム &6のプレイヤー'" + team.getColor() + player.getName() + "&6'がログアウトしたため死亡しました";
                Actions.worldcastMessage(Bukkit.getWorld(plugin.getConfigs().getGameWorld()), deathMsg);
                // game.message(deathMsg);

                game.log(" Player " + player.getName() + " Died because Logged out!");
            }
        }
    }

    // プレイヤーがログインした
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        // ログイン時のMOTDなどの最後に表示別スレッドで実行する
        plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                for (Game game : GameManager.getGames().values()) {
                    // ゲーム参加ユーザは何もしない
                    if (game.isJoined(player)) { return; }
                }

                for (Game game : GameManager.getGames().values()) {
                    // 待機中ゲーム
                    if (game.isReady()) {
                        // 賞金系メッセージ
                        String entryFeeMsg = String.valueOf(game.getStage().getEntryFee()) + "Coin";
                        String awardMsg = String.valueOf(game.getStage().getAward()) + "Coin";
                        if (game.getStage().getEntryFee() <= 0) entryFeeMsg = "&7FREE!";
                        if (game.getStage().getAward() <= 0) awardMsg = "&7なし";

                        // アナウンス
                        if (!game.isRandom()) {
                            Actions.message(player, "&b* ===================================");
                            Actions.message(player, msgPrefix + "&2フラッグゲーム'&6" + game.getName() + "&2'の参加受付が行われています！");
                            Actions.message(player, msgPrefix + "&2 参加料:&6 " + entryFeeMsg + "&2   賞金:&6 " + awardMsg);
                            Actions.message(player, msgPrefix + "&2 '&6/flag join " + game.getName() + "&2' コマンドで参加してください！");
                            Actions.message(player, "&b* ===================================");
                        } else {
                            Actions.message(player, "&b* ===================================");
                            Actions.message(player, msgPrefix + "&2フラッグゲーム'&6ランダムステージ&2'の参加受付が行われています！");
                            Actions.message(player, msgPrefix + "&2 参加料:&6 " + entryFeeMsg + "&2   賞金:&6 " + awardMsg);
                            Actions.message(player, msgPrefix + "&2 '&6/flag join random&2' コマンドで参加してください！");
                            Actions.message(player, "&b* ===================================");
                        }

                    }
                    // 開始中ゲーム
                    else if (game.isStarting()) {
                        // 観戦アナウンス
                        Actions.message(player, "&b* ===================================");
                        Actions.message(player, msgPrefix + "&2フラッグゲーム'&6" + game.getName() + "&2'が始まっています！");
                        Actions.message(player, msgPrefix + "&2 '&6/flag watch " + game.getName() + "&2' コマンドで観戦することができます！");
                        Actions.message(player, "&b* ===================================");
                    }
                }
            }
        }, 20L);
    }

    // プレイヤーがログインしようとした
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLogin(final PlayerLoginEvent event) {
        // プレイヤー追加
        PlayerManager.addPlayer(event.getPlayer());
    }

    /* methods */

    private boolean canUseBlock(Player player, Block block, Boolean sendFalseMessage, Boolean door) {
        // ワールドがゲーム用ワールドでなければ常にtrueを返す
        if (block.getWorld() != Bukkit.getWorld(plugin.getConfigs().getGameWorld())) return true;

        GameTeam playerTeam = null;
        Location loc = block.getLocation();

        // 開始中のゲームを回す
        for (Game game : GameManager.getGames().values()) {
            GameTeam blockTeam = null;
            // 拠点マップを回してブロックの所属拠点チームを取得
            for (Map.Entry<GameTeam, Cuboid> entry : game.getStage().getBases().entrySet()) {
                if (entry.getValue().isIn(loc)) {
                    blockTeam = entry.getKey();
                    break;
                }
            }
            if (blockTeam == null) {
                continue;
            }

            // プレイヤーのチーム取得
            playerTeam = game.getPlayerTeam(player);
            if (playerTeam == null) continue;

            // プレイヤーとブロックのチームが違えばイベントをキャンセルする
            if (playerTeam != blockTeam) {
                if (sendFalseMessage) Actions.message(player, msgPrefix + "&cここは相手の拠点です！");
                return false; // 開けない
            } else {
                return true; // 自分の拠点のブロックは使用可能
            }
        }

        // そのブロックがどのゲームのチーム拠点にも所属していない
        // ドアなら自由に開閉可能にする
        if (door) return true;

        for (Stage stage : StageManager.getStages().values()) {
            Cuboid stageArea = stage.getStage();
            // ステージ領域内かどうか
            if (stageArea != null && stageArea.isIn(loc)) {
                // ステージ保護があるかどうか
                if (stage.isStageProtected()) {
                    // そのゲームに参加しているプレイヤーかどうか取得
                    if (stage.isUsing() && stage.getGame() != null && stage.getGame().isJoined(player)) {
                        // チェスト登録されているものか取得
                        if (stage.isChest(loc)) {
                            return true;
                        }
                        // 未登録チェストはダミー扱いで開閉禁止
                        else {
                            if (sendFalseMessage) Actions.message(player, msgPrefix + "&cこれはダミーブロックです！");
                            return false;
                        }
                    } else {
                        if (sendFalseMessage) Actions.message(player, msgPrefix + "&cあなたはこのゲームに参加していません！");
                        return false;
                    }
                } else {
                    // 保護無効なら開閉可能
                    return true;
                }
            }
        }

        // どのゲームステージにも所属していない
        return true; // 開ける
    }

    private void clickFlagSign(Player player, Block block) {
        if (!(block.getState() instanceof Sign)) return;

        Sign sign = (Sign) block.getState();
        String line2 = sign.getLine(1); // 2行目
        String line3 = sign.getLine(2); // 3行目

        SignAction action = null;
        for (SignAction sa : SignAction.values()) {
            if (sa.name().toLowerCase().equalsIgnoreCase(line2.trim())) action = sa;
        }
        if (action == null) {
            Actions.message(player, "&cThis sign is broken! Please contact server staff!");
            return;
        }

        // 処理を分ける
        switch (action) {
        // 回復
            case HEAL:
                if (line3 != "" && !line3.isEmpty()) {
                    GameTeam signTeam = null;
                    GameTeam playerTeam = null;
                    for (GameTeam gt : GameTeam.values()) {
                        if (gt.name().toLowerCase().equalsIgnoreCase(line3)) signTeam = gt;
                    }
                    if (signTeam == null) {
                        Actions.message(player, "&cThis sign is broken! Please contact server staff!");
                        return;
                    }
                    for (Game game : GameManager.getGames().values()) {
                        if (!game.isStarting()) continue;
                        if (game.getPlayerTeam(player) != null) {
                            playerTeam = game.getPlayerTeam(player);
                            break;
                        }
                    }
                    if (playerTeam == null) {
                        Actions.message(player, "&cこの看板はフラッグゲーム中にのみ使うことができます");
                        return;
                    }

                    if (playerTeam != signTeam) {
                        Actions.message(player, "&cこれはあなたのチームの看板ではありません！");
                        return;
                    }
                }

                // 20以上にならないように体力とお腹ゲージを+2(ハート、おにく1つ分)回復させる
                int nowHP = player.getHealth();
                nowHP = nowHP + 2;
                if (nowHP > 20) nowHP = 20;

                int nowFL = player.getFoodLevel();
                nowFL = nowFL + 2;
                if (nowFL > 20) nowFL = 20;

                // プレイヤーにセット
                player.setHealth(nowHP);
                player.setFoodLevel(nowFL);
                player.setFireTicks(0); // 燃えてれば消してあげる

                Actions.message(player, msgPrefix + "&aHealed!");

                break;

            // 自殺
            case KILL:
                for (Game game : GameManager.getGames().values()) {
                    if (!game.isStarting()) continue;
                    if (game.getPlayerTeam(player) != null) {
                        GameTeam team = game.getPlayerTeam(player);
                        game.message(msgPrefix + "&6[" + game.getName() + "]&6 '" + team.getColor() + player.getName() + "&6'が自殺しました。");
                        break;
                    }
                }
                player.setHealth(0);
                player.setFoodLevel(0);
                break;

            default:
                Actions.message(player, msgPrefix + "&cSorry I forgot this sign-action. Please contact server staff!");
                log.warning(logPrefix + player.getName() + ": Sorry I forgot this sign-action. Please contact server staff!");
        }

    }
}