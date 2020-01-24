package proj.kedabra.billsnap.fixtures;

import java.math.BigDecimal;

import proj.kedabra.billsnap.business.model.entities.Item;

public class ItemEntityFixture {

    private ItemEntityFixture() {}

    public static Item getDefault() {
        final var item = new Item();
        item.setId(4000L);
        item.setCost(BigDecimal.TEN);
        item.setName("ramen");
        item.setBill(BillEntityFixture.getDefault());

        return item;
    }
}
