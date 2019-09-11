package proj.kedabra.billsnap.fixtures;

import proj.kedabra.billsnap.presentation.resources.AccountCreationResource;

public final class AccountCreationResourceFixture {

    private AccountCreationResourceFixture() {}

    public static AccountCreationResource getDefault() {
        final var resource = new AccountCreationResource();
        resource.setEmail("test@email.com");
        resource.setFirstName("Naruto");
        resource.setLastName("Uchiha");
        resource.setPassword("Hidden@Vill4ge");

        return resource;
    }
}
