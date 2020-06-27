package proj.kedabra.billsnap.fixtures;

import java.math.BigDecimal;

import proj.kedabra.billsnap.business.dto.ItemDTO;

public final class ItemDTOFixture {

    private ItemDTOFixture() {}

    public static ItemDTO getItemCustom(Long id) {
        ItemDTO item = new ItemDTO();
        item.setId(id);
        item.setName("ramen");
        item.setCost(BigDecimal.valueOf(10));
        return item;
    }
}
