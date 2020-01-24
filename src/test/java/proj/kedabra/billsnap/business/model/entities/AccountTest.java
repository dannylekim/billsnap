package proj.kedabra.billsnap.business.model.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import proj.kedabra.billsnap.fixtures.AccountBillFixture;
import proj.kedabra.billsnap.fixtures.AccountEntityFixture;

class AccountTest {

    @Test
    @DisplayName("Get AccountBill by Bill")
    void getAccountBill() {
        //Given
        final var defaultAccount = AccountEntityFixture.getDefaultAccount();
        final var accountBill = AccountBillFixture.getDefault();
        final var bill = accountBill.getBill();
        defaultAccount.setBills(Set.of(accountBill));

        //when
        final var retrievedAccountBill = defaultAccount.getAccountBill(bill);

        //then
        assertThat(retrievedAccountBill.orElseThrow()).isSameAs(accountBill);
    }

}