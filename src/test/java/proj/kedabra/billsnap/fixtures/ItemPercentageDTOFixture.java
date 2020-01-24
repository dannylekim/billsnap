package proj.kedabra.billsnap.fixtures;

import java.math.BigDecimal;

import proj.kedabra.billsnap.business.dto.ItemPercentageDTO;

public final class ItemPercentageDTOFixture {

    private ItemPercentageDTOFixture() {}

    public static ItemPercentageDTO getDefault() {
        final ItemPercentageDTO itemPercentageDTO = new ItemPercentageDTO();
        itemPercentageDTO.setItemId(1008L);
        itemPercentageDTO.setPercentage(new BigDecimal(100));
        return itemPercentageDTO;
    }

    public static ItemPercentageDTO getDefaultWithId(Long id) {
        final ItemPercentageDTO itemPercentageDTO = new ItemPercentageDTO();
        itemPercentageDTO.setItemId(id);
        itemPercentageDTO.setPercentage(new BigDecimal(100));
        return itemPercentageDTO;
    }
}
