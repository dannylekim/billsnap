package proj.kedabra.billsnap.fixtures;

import java.math.BigDecimal;

import proj.kedabra.billsnap.presentation.resources.ItemResource;

public final class ItemResourceFixture {
    private ItemResourceFixture() {}

    public static ItemResource getDefault() {
        final ItemResource resource = new ItemResource();
        resource.setName("sashimi");
        resource.setCost(BigDecimal.valueOf(25));

        return resource;
    }
}
