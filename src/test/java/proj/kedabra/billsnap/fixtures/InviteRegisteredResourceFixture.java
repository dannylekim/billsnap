package proj.kedabra.billsnap.fixtures;

import java.util.ArrayList;
import java.util.List;

import proj.kedabra.billsnap.presentation.resources.InviteRegisteredResource;

public final class InviteRegisteredResourceFixture {

    private InviteRegisteredResourceFixture(){}

    public static InviteRegisteredResource getDefault() {
        final var resource = new InviteRegisteredResource();
        resource.setAccounts(new ArrayList<>());
        return resource;
    }
}
