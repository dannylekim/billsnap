package proj.kedabra.billsnap.fixtures;

import java.math.BigDecimal;

import proj.kedabra.billsnap.business.dto.ItemPercentageSplitDTO;

public class ItemPercentageSplitDTOFixture {

    private ItemPercentageSplitDTOFixture() {}

    public static ItemPercentageSplitDTO getDefault() {
        final ItemPercentageSplitDTO itemPercentageSplitDTO = new ItemPercentageSplitDTO();
        itemPercentageSplitDTO.setId(25L);
        itemPercentageSplitDTO.setPercentage(BigDecimal.valueOf(20));
        itemPercentageSplitDTO.setName("pudding");
        itemPercentageSplitDTO.setCost(BigDecimal.valueOf(5));
        return itemPercentageSplitDTO;
    }
}
