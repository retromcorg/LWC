package com.griefcraft.integration.currency;

import com.griefcraft.integration.ICurrency;
import com.griefcraft.util.config.Configuration;
import com.iConomy.iConomy;
import com.iConomy.system.Account;
import com.iConomy.system.Holdings;
import com.iConomy.util.Constants;
import org.bukkit.entity.Player;

public class iConomy5Currency implements ICurrency {

    /**
     * The economy configuration
     */
    private Configuration configuration = Configuration.load("iconomy.yml");

    /**
     * The server account to use
     */
    private String serverAccount;

    public iConomy5Currency() {
        serverAccount = configuration.getString("iConomy.serverBankAccount", "");

        // create the account in iConomy if needed
        if(!serverAccount.isEmpty()) {
            iConomy.getAccount(serverAccount);
        }
    }

    public boolean isActive() {
        return true;
    }

    public boolean usingCentralBank() {
        return !serverAccount.isEmpty();
    }

    public String format(double money) {
        return iConomy.format(money);
    }

    public String getMoneyName() {
        return Constants.Major.get(1);
    }

    public double getBalance(Player player) {
        if(player == null) {
            return 0;
        }

        Account account = iConomy.getAccount(player.getName());

        if (account == null) {
            return 0;
        }

        return account.getHoldings().balance();
    }

    public boolean canAfford(Player player, double money) {
        if(player == null) {
            return false;
        }

        Account account = iConomy.getAccount(player.getName());

        return account != null && account.getHoldings().hasEnough(money);
    }

    public boolean canCentralBankAfford(double money) {
        if (!usingCentralBank()) {
            return true;
        }

        Account account = iConomy.getAccount(serverAccount);

        return account != null && account.getHoldings().hasEnough(money);
    }

    public double addMoney(Player player, double money) {
        if(player == null) {
            return 0;
        }

        // remove the money from the central bank if applicable
        if(usingCentralBank()) {
            if (!canCentralBankAfford(money)) {
                return 0;
            }

            Account central = iConomy.getAccount(serverAccount);

            if(central == null) {
                return 0;
            }

            central.getHoldings().subtract(money);
        }

        Account account = iConomy.getAccount(player.getName());

        if (account == null) {
            return 0;
        }

        Holdings holdings = account.getHoldings();
        holdings.add(money);

        return holdings.balance();
    }

    public double removeMoney(Player player, double money) {
        if(player == null) {
            return 0;
        }

        // we're removing money, so it should be positive
        if (money < 0) {
            money = -money;
        }

        // add the money to the central bank if applicable
        if(usingCentralBank()) {
            Account central = iConomy.getAccount(serverAccount);

            if(central == null) {
                return 0;
            }

            central.getHoldings().add(money);
        }

        Account account = iConomy.getAccount(player.getName());

        if (account == null) {
            return 0;
        }

        Holdings holdings = account.getHoldings();

        // this SHOULD be a transaction, ensure they have enough
        if (!holdings.hasEnough(money)) {
            return holdings.balance();
        }

        holdings.subtract(money);

        return holdings.balance();
    }

}