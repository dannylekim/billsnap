package proj.kedabra.billsnap.fixtures;

import java.math.BigDecimal;

import proj.kedabra.billsnap.business.dto.ItemPercentageDTO;

public final class ItemPercentageDTOFixture {

    private ItemPercentageDTOFixture() {}

    public static ItemPercentageDTO getDefault() {
        final ItemPercentageDTO itemPercentageDTO = new ItemPercentageDTO();
        itemPercentageDTO.setId(25L);
        itemPercentageDTO.setPercentage(BigDecimal.TEN);
        return itemPercentageDTO;
    }

    public static ItemPercentageDTO getDefaultWithId(Long id) {
        final ItemPercentageDTO itemPercentageDTO = new ItemPercentageDTO();
        itemPercentageDTO.setId(id);
        itemPercentageDTO.setPercentage(BigDecimal.TEN);
        return itemPercentageDTO;
    }
}