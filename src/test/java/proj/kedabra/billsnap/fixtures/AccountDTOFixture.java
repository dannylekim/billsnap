package proj.kedabra.billsnap.fixtures;

import proj.kedabra.billsnap.business.dto.AccountDTO;

public class AccountDTOFixture {

    private AccountDTOFixture() {}

    public static AccountDTO getCreationDTO() {
        var dto = new AccountDTO();
        dto.setEmail("test@email.com");
        dto.setFirstName("Naruto");
        dto.setLastName("Uchiha");
        dto.setPassword("Hidden@Vill4ge");

        return dto;
    }
}
