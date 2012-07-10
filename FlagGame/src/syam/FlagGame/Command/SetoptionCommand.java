package syam.FlagGame.Command;

import org.bukkit.command.CommandSender;

import syam.FlagGame.Game.Game;
import syam.FlagGame.Game.GameManager;
import syam.FlagGame.Util.Actions;

public class SetoptionCommand extends BaseCommand {
	public SetoptionCommand(){
		bePlayer = false;
		name = "setoption";
		argLength = 0;
		usage = "<option> <value> <- set game option";
	}

	@Override
	public boolean execute() {
		// 引数が無ければオプションデータを表示する
		if (args.size() == 0 || args.size() == 1){
			Actions.message(sender, null, "&cオプションと値を指定してください！");
			sendAvailableOptions(sender);
			return true;
		}
		// 引数が2つ以上なら設定する
		else{
			Game game = GameManager.getSelectedGame(player);
			if (game == null){
				Actions.message(sender, null, "&c先に編集するゲームを選択してください！");
				return true;
			}

			if (game.isReady() || game.isStarting()){
				Actions.message(sender, null, "&cこのゲームは受付中か開始中のため設定変更できません！");
				return true;
			}

			String option = args.get(0).trim();
			Configuable cnf = null;
			for (Configuable c : Configuable.values()){
				if (c.name().toLowerCase().equalsIgnoreCase(option))
					cnf = c;
			}
			if (cnf == null){
				Actions.message(sender, null, "&cそのオプションは存在しません！");
				sendAvailableOptions(sender);
				return true;
			}

			// 指定されたオプションによって処理を分ける
			switch(cnf){
				// ゲーム時間変更
				case GAMETIME:
					int num = 60 * 10; // デフォルト10分
					try{
						num = Integer.parseInt(args.get(1));
					}catch(NumberFormatException ex){
						Actions.message(sender, null, "&cオプションの値が整数ではありません！");
						return true;
					}

					if (num <= 0){
						Actions.message(sender, null, "&c値が不正です！正数を入力してください！");
						return true;
					}

					game.setGameTime(num);

					Actions.message(sender, null, "&aゲーム'"+game.getName()+"'のゲーム時間は "+num+"秒("+Actions.getTimeString(num)+") に設定されました！");
					break;
				// チーム毎の人数上限変更
				case TEAMLIMIT:
					int cnt = 8; // デフォルト8人
					try{
						cnt = Integer.parseInt(args.get(1));
					}catch(NumberFormatException ex){
						Actions.message(sender, null, "&cオプションの値が整数ではありません！");
						return true;
					}

					if (cnt <= 0){
						Actions.message(sender, null, "&c値が不正です！正数を入力してください！");
						return true;
					}

					game.setTeamLimit(cnt);

					Actions.message(sender, null, "&aゲーム'"+game.getName()+"'のチーム毎人数上限値は "+cnt+"人 に設定されました！");
					break;
				// 賞金
				case AWARD:
					int award = 1000; // デフォルト1000コイン
					try{
						award = Integer.parseInt(args.get(1));
					}catch(NumberFormatException ex){
						Actions.message(sender, null, "&cオプションの値が整数ではありません！");
						return true;
					}
					if (award < 0){
						Actions.message(sender, null, "&c値が不正です！負数は指定できません！");
						return true;
					}

					game.setAward(award);

					Actions.message(sender, null, "&aゲーム'"+game.getName()+"'の賞金は "+award+"Coin に設定されました！");
					break;
				default:
					Actions.message(sender, null, "&cSorry, this option is non available. Please report this issue.");
					break;
			}

			return true;
		}
	}

	@Override
	public boolean permission() {
		return sender.hasPermission("flag.admin");
	}

	/**
	 * 有効なオプションとその説明をsenderに送る
	 * @param sender
	 */
	private void sendAvailableOptions(CommandSender sender){
		Actions.message(sender, null, "&a有効なオプション: ");
		for (Configuable conf : Configuable.values()){
			Actions.message(sender, null, "&f "+conf.name().toLowerCase()+"&7 - "+conf.getDescription());
		}
	}

	/**
	 * 有効なオプションの列挙型
	 * @author syam
	 */
	private enum Configuable{
		GAMETIME ("ゲームの制限時間(秒)"),
		TEAMLIMIT ("チーム毎の人数制限"),
		AWARD ("勝利チームへの賞金"),
		;

		String description;
		Configuable(String description){
			this.description = description;
		}

		public String getDescription(){
			return description;
		}
	}
}
