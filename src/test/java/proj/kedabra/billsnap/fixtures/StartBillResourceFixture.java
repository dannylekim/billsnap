package proj.kedabra.billsnap.fixtures;

import proj.kedabra.billsnap.presentation.resources.StartBillResource;

public class StartBillResourceFixture {

    private StartBillResourceFixture() {}

    public static StartBillResource getStartBillResourceCustom(Long id) {
        final var startBillResource = new StartBillResource();
        startBillResource.setId(id);

        return startBillResource;
    }

}
