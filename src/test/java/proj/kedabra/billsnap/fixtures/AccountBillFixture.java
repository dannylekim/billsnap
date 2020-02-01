package proj.kedabra.billsnap.fixtures;

import java.math.BigDecimal;

import proj.kedabra.billsnap.business.model.entities.AccountBill;
import proj.kedabra.billsnap.business.model.entities.AccountBillId;
import proj.kedabra.billsnap.business.utils.enums.InvitationStatusEnum;

public final class AccountBillFixture {

    private AccountBillFixture() {}

    public static AccountBill getDefault() {
        final var accountBill = new AccountBill();
        accountBill.setBill(BillEntityFixture.getDefault());
        accountBill.setAccount(AccountEntityFixture.getDefaultAccount());
        accountBill.setAmountPaid(BigDecimal.ZERO);
        final var id = new AccountBillId();
        id.setAccountId(123L);
        id.setBillId(123L);
        accountBill.setId(id);
        accountBill.setPercentage(BigDecimal.TEN);
        accountBill.setStatus(InvitationStatusEnum.PENDING);

        return accountBill;
    }
}
