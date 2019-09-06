package proj.kedabra.billsnap.fixtures;

import java.math.BigDecimal;
import java.util.List;

import proj.kedabra.billsnap.business.dto.ItemAssociationSplitDTO;

public class ItemAssociationSplitDTOFixture {
    private ItemAssociationSplitDTOFixture () {}

    public static ItemAssociationSplitDTO getDefault() {
        final var itemAssociationSplitDTO = new ItemAssociationSplitDTO();
        itemAssociationSplitDTO.setAccount(AccountDTOFixture.getCreationDTO());
        itemAssociationSplitDTO.setCost(BigDecimal.valueOf(5));
        final var items = List.of(ItemPercentageSplitDTOFixture.getDefault());
        itemAssociationSplitDTO.setItems(items);

        return itemAssociationSplitDTO;
    }
}
