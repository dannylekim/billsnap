package proj.kedabra.billsnap.fixtures;

import java.math.BigDecimal;

import proj.kedabra.billsnap.business.entities.AccountItem;
import proj.kedabra.billsnap.business.entities.AccountItemId;

public class AccountItemEntityFixture {
    private AccountItemEntityFixture() {}

    public static AccountItem getDefault() {
        final var accountItem = new AccountItem();
        final var accountItemId = new AccountItemId();
        accountItemId.setAccountId(AccountEntityFixture.getDefaultAccount().getId());
        accountItemId.setItemId(ItemEntityFixture.getDefault().getId());
        accountItem.setId(accountItemId);
        accountItem.setAccount(AccountEntityFixture.getDefaultAccount());
        accountItem.setItem(ItemEntityFixture.getDefault());
        accountItem.setPercentage(BigDecimal.valueOf(25));

        return accountItem;
    }
}
