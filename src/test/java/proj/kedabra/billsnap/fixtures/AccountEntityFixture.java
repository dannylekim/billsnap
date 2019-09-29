package proj.kedabra.billsnap.fixtures;

import proj.kedabra.billsnap.business.entities.Account;
import proj.kedabra.billsnap.business.utils.enums.AccountStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.GenderEnum;

public final class AccountEntityFixture {
    private AccountEntityFixture(){}

    public static Account getDefaultAccount(){
        final var account = new Account();
        account.setEmail("accountentity@test.com");
        account.setFirstName("Naruto");
        account.setLastName("Uchiha");
        account.setPassword("Hidden@Vill4ge");
        account.setMiddleName("middlename");
        account.setGender(GenderEnum.MALE);
        account.setPhoneNumber("123456789");
        account.setStatus(AccountStatusEnum.REGISTERED);
        account.setId(1234L);
        return account;
    }
}
