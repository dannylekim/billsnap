package proj.kedabra.billsnap.fixtures;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import proj.kedabra.billsnap.business.dto.ItemAssociationDTO;

public final class ItemAssociationDTOFixture {

    private ItemAssociationDTOFixture() {}

    public static ItemAssociationDTO getDefault() {
        final var itemAssociationDTO = new ItemAssociationDTO();
        itemAssociationDTO.setEmail("associateitem@test.com");
        final var percentages = Stream.of(ItemPercentageDTOFixture.getDefault()).collect(Collectors.toList());
        itemAssociationDTO.setItems(percentages);
        return itemAssociationDTO;
    }

}
