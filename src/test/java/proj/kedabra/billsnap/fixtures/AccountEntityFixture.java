package proj.kedabra.billsnap.fixtures;

import proj.kedabra.billsnap.business.entities.Account;

public class AccountEntityFixture {
    private AccountEntityFixture(){}

    public static Account getDefaultAccount(){
        var account = new Account();
        account.setEmail("accountentity@test.com");
        account.setFirstName("Naruto");
        account.setLastName("Uchiha");
        account.setPassword("Hidden@Vill4ge");
        return account;
    }
}
