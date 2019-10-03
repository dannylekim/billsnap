package proj.kedabra.billsnap.fixtures;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.model.entities.Bill;
import proj.kedabra.billsnap.business.model.entities.Item;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.SplitByEnum;

public final class BillEntityFixture {
    private BillEntityFixture() {}

    public static Bill getDefault() {
        final var account = new Account();
        account.setEmail("accountentity@test.com");
        account.setFirstName("Naruto");
        account.setLastName("Uchiha");
        account.setPassword("Hidden@Vill4ge");

        final var item = new Item();
        item.setName("yogurt");
        item.setCost(BigDecimal.valueOf(4));

        final var bill = new Bill();
        bill.setStatus(BillStatusEnum.OPEN);
        bill.setResponsible(account);
        bill.setCreator(account);
        bill.setActive(true);
        bill.setSplitBy(SplitByEnum.ITEM);
        bill.setName("default bill");
        bill.setTipAmount(BigDecimal.TEN);
        bill.setTipPercent(BigDecimal.ZERO);
        bill.setItems(Stream.of(item).collect(Collectors.toSet()));

        return bill;
    }

    public static Bill getMappedBillDTOFixture () {
        final var bill = new Bill();
        bill.setName("Monthly Rent");
        bill.setCategory("Rent");
        bill.setCompany("Landlord");
        bill.setTipAmount(BigDecimal.ZERO);

        final Set<Item> items = new HashSet<>();
        final var item = new Item();
        item.setName("Rent");
        item.setCost(BigDecimal.valueOf(300));
        items.add(item);
        bill.setItems(items);

        return bill;
    }
}
