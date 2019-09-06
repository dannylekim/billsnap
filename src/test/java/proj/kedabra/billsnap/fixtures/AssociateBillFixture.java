package proj.kedabra.billsnap.fixtures;

import java.math.BigDecimal;
import java.util.List;

import proj.kedabra.billsnap.presentation.resources.AssociateBillResource;
import proj.kedabra.billsnap.presentation.resources.ItemAssociationResource;
import proj.kedabra.billsnap.presentation.resources.ItemPercentageResource;

public class AssociateBillFixture {

    private AssociateBillFixture() {}

    public static AssociateBillResource getDefault() {
        final var resource = new AssociateBillResource();

        final var itemAssociationResourceOne = new ItemAssociationResource();
        itemAssociationResourceOne.setEmail("test@email.com");
        final var itemPercentageResourceOne = new ItemPercentageResource();
        itemPercentageResourceOne.setItemId(1L);
        itemPercentageResourceOne.setPercentage(BigDecimal.valueOf(50));
        itemAssociationResourceOne.setItems(List.of(itemPercentageResourceOne));

        final var itemAssociationResourceTwo = new ItemAssociationResource();
        itemAssociationResourceTwo.setEmail("userdetails@service.com");
        final var itemPercentageResourceTwo = new ItemPercentageResource();
        itemPercentageResourceTwo.setItemId(1L);
        itemPercentageResourceTwo.setPercentage(BigDecimal.valueOf(50));
        itemAssociationResourceTwo.setItems(List.of(itemPercentageResourceTwo));

        resource.setId(1002L);
        resource.setItemsPerAccount(List.of(itemAssociationResourceOne, itemAssociationResourceTwo));

        return resource;
    }
}
