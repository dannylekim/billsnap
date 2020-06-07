package proj.kedabra.billsnap.fixtures;

import proj.kedabra.billsnap.presentation.resources.AccountResource;

public class AccountResourceFixture {

    private AccountResourceFixture() {}

    public static AccountResource getDefault() {
        final AccountResource account = new AccountResource();

        account.setId(1000L);
        account.setEmail("test@email.com");
        account.setFirstName("Naruto");
        account.setLastName("Uchiha");

        return account;
    }
}
