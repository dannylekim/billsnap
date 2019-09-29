package proj.kedabra.billsnap.fixtures;


import java.math.BigDecimal;

import proj.kedabra.billsnap.business.entities.AccountBill;
import proj.kedabra.billsnap.business.entities.AccountBillId;

public class AccountBillEntityFixture {

    private AccountBillEntityFixture() {}

    public static AccountBill getDefault() {
        final AccountBill accountBill = new AccountBill();
        final AccountBillId accountBillId = new AccountBillId();

        accountBillId.setAccountId(AccountEntityFixture.getDefaultAccount().getId());
        accountBillId.setBillId(BillEntityFixture.getDefault().getId());
        accountBill.setId(accountBillId);
        accountBill.setBill(BillEntityFixture.getDefault());
        accountBill.setAccount(AccountEntityFixture.getDefaultAccount());
        accountBill.setPercentage(BigDecimal.ZERO);

        return accountBill;
    }
}
