package proj.kedabra.billsnap.fixtures;

import java.time.LocalDate;

import proj.kedabra.billsnap.business.model.entities.Location;
import proj.kedabra.billsnap.presentation.resources.BaseAccountResource;

public class BaseAccountResourceFixture {

    private BaseAccountResourceFixture() {}

    public static BaseAccountResource getDefault() {
        final var baseAccount = new BaseAccountResource();

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
