package proj.kedabra.billsnap.fixtures;

import proj.kedabra.billsnap.business.dto.AccountDTO;

public final class AccountDTOFixture {

    private AccountDTOFixture() {}

    public static AccountDTO getCreationDTO() {
        final var dto = new AccountDTO();
        dto.setEmail("test@email.com");
        dto.setId(1000L);
        dto.setFirstName("Naruto");
        dto.setLastName("Uchiha");
        dto.setPassword("Hidden@Vill4ge");

        return dto;
    }

    public static AccountDTO getMappedDefaultAccount() {
        final var account = new AccountDTO();
        account.setEmail("accountentity@test.com");
        account.setFirstName("Naruto");
        account.setLastName("Uchiha");
        account.setPassword("Hidden@Vill4ge");
        account.setMiddleName("middlename");
        account.setGender("MALE");
        account.setPhoneNumber("123456789");
        account.setId(1234L);
        return account;
    }
}
