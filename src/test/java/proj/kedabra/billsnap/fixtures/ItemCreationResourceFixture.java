package proj.kedabra.billsnap.fixtures;

import java.math.BigDecimal;

import proj.kedabra.billsnap.presentation.resources.ItemCreationResource;

public final class ItemCreationResourceFixture {
    private ItemCreationResourceFixture() {}

    public static ItemCreationResource getDefault() {
        final ItemCreationResource resource = new ItemCreationResource();
        resource.setName("sashimi");
        resource.setCost(BigDecimal.valueOf(25));

        return resource;
    }
}
