package proj.kedabra.billsnap.fixtures;

import proj.kedabra.billsnap.presentation.resources.LoginResource;

public class LoginResourceFixture {

    private LoginResourceFixture() {}

    public static LoginResource getDefault(){
        LoginResource resource = new LoginResource();
        resource.setEmail("some@email.com");
        resource.setPassword("respass");

        return resource;
    }

    public static LoginResource getLoginResourceRegistered(){
        LoginResource resource = new LoginResource();
        resource.setEmail("userdetails@service.com");
        resource.setPassword("somepass");

        return resource;
    }
}
