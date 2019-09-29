package proj.kedabra.billsnap.fixtures;

import java.util.List;

import proj.kedabra.billsnap.business.dto.ItemAssociationDTO;

public final class ItemAssociationDTOFixture {

    private ItemAssociationDTOFixture() {}

    public static ItemAssociationDTO getDefault() {
        final var itemAssociationDTO = new ItemAssociationDTO();
        itemAssociationDTO.setAccountEmail("test@email.com'");
        final var percentages = List.of(ItemPercentageDTOFixture.getDefault());
        itemAssociationDTO.setItems(percentages);
        return itemAssociationDTO;
    }
}
