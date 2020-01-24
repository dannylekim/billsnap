package proj.kedabra.billsnap.business.model.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import proj.kedabra.billsnap.fixtures.AccountBillFixture;
import proj.kedabra.billsnap.fixtures.BillEntityFixture;

class BillTest {

    @Test
    @DisplayName("Get account bill by account")
    void shouldReturnAccountBill() {
        //Given
        final var defaultBill = BillEntityFixture.getDefault();
        final var accountBill = AccountBillFixture.getDefault();
        final var account = accountBill.getAccount();
        defaultBill.setAccounts(Set.of(accountBill));

        //when
        final var retrievedAccountBill = defaultBill.getAccountBill(account);

        //then
        assertThat(retrievedAccountBill.orElseThrow()).isSameAs(accountBill);
    }

}