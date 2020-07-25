package proj.kedabra.billsnap.fixtures;

import java.time.LocalDate;

import proj.kedabra.billsnap.business.dto.BaseAccountDTO;
import proj.kedabra.billsnap.business.model.entities.Location;

public final class BaseAccountDTOFixture {

    private BaseAccountDTOFixture() {}

    public static BaseAccountDTO getDefault() {
        final var baseAccount = new BaseAccountDTO();

        baseAccount.setFirstName("editFirstName");
        baseAccount.setLastName("editLastName");
        baseAccount.setMiddleName("editMiddleName");
        baseAccount.setGender("FEMALE");
        baseAccount.setPhoneNumber("4201112222");
        baseAccount.setBirthDate(LocalDate.of(2000, 1, 1));

        final var location = new Location();
        location.setAddress("editAddress");
        location.setCity("editCity");
        location.setState("editState");
        baseAccount.setLocation(location);

        return baseAccount;
    }

}
