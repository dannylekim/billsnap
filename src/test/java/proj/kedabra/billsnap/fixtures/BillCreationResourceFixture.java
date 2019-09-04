package proj.kedabra.billsnap.fixtures;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import proj.kedabra.billsnap.presentation.resources.BillCreationResource;

public class BillCreationResourceFixture {

    private BillCreationResourceFixture() { }

    public static BillCreationResource getDefault() {
        final var resource = new BillCreationResource();
        resource.setName("sushi restaurant");
        resource.setCategory("");
        resource.setCompany("");
        resource.setItems(List.of(ItemResourceFixture.getDefault()));
        resource.setTipAmount(BigDecimal.valueOf(5));
        resource.setAccountsStringList(new ArrayList<>());

        return resource;
    }
}
