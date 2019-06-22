package proj.kedabra.billsnap.fixtures;

import proj.kedabra.billsnap.presentation.resources.AccountCreationResource;

public class AccountCreationResourceFixture {

    private AccountCreationResourceFixture(){}

    public static AccountCreationResource getDefault(){
        var resource = new AccountCreationResource();
        resource.setEmail("test@email.com");
        resource.setFirstName("Naruto");
        resource.setLastName("Uchiha");
        resource.setPassword("notsecure");

        return resource;
    }
}
