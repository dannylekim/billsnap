package proj.kedabra.billsnap.fixtures;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import proj.kedabra.billsnap.business.entities.Account;
import proj.kedabra.billsnap.business.entities.AccountBill;
import proj.kedabra.billsnap.business.entities.AccountItem;
import proj.kedabra.billsnap.business.entities.Bill;
import proj.kedabra.billsnap.business.entities.Item;
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

    public static Bill getMappedBillSplitDTOFixture() {
        final var bill = BillEntityFixture.getDefault();
        final var item = bill.getItems().iterator().next();
        final var accountPercentageSplit = BigDecimal.valueOf(50);

        final var accountItem1 = new AccountItem();
        final var account1 = AccountEntityFixture.getDefaultAccount();
        account1.setEmail("abc123@email.com");
        accountItem1.setAccount(account1);
        accountItem1.setItem(item);
        accountItem1.setPercentage(accountPercentageSplit);
        final var accountBill1 = new AccountBill();
        accountBill1.setBill(bill);
        accountBill1.setAccount(account1);
        accountBill1.setPercentage(BigDecimal.ZERO);

        final var accountItem2 = new AccountItem();
        final var account2 = AccountEntityFixture.getDefaultAccount();
        account2.setEmail("hellomotto@cell.com");
        accountItem2.setAccount(account2);
        accountItem2.setItem(item);
        accountItem2.setPercentage(accountPercentageSplit);
        final var accountBill2 = new AccountBill();
        accountBill2.setBill(bill);
        accountBill2.setAccount(account2);
        accountBill2.setPercentage(BigDecimal.ZERO);

        item.setAccounts(Set.of(accountItem1, accountItem2));
        bill.setCreator(account1);
        bill.setResponsible(account1);
        bill.setAccounts(Set.of(accountBill1, accountBill2));

        return bill;
    }

    public static Bill getMappedBillSplitDTOFixtureGivenSplitPercentage(BigDecimal splitPercentage) {
        final var bill = BillEntityFixture.getDefault();
        final var item = bill.getItems().iterator().next();

        final var accountItem1 = new AccountItem();
        final var account1 = AccountEntityFixture.getDefaultAccount();
        account1.setEmail("abc123@email.com");
        accountItem1.setAccount(account1);
        accountItem1.setItem(item);
        accountItem1.setPercentage(splitPercentage);
        final var accountBill1 = new AccountBill();
        accountBill1.setBill(bill);
        accountBill1.setAccount(account1);
        accountBill1.setPercentage(BigDecimal.ZERO);

        final var accountItem2 = new AccountItem();
        final var account2 = AccountEntityFixture.getDefaultAccount();
        account2.setEmail("hellomotto@cell.com");
        accountItem2.setAccount(account2);
        accountItem2.setItem(item);
        accountItem2.setPercentage(splitPercentage);
        final var accountBill2 = new AccountBill();
        accountBill2.setBill(bill);
        accountBill2.setAccount(account2);
        accountBill2.setPercentage(BigDecimal.ZERO);

        item.setAccounts(Set.of(accountItem1, accountItem2));
        bill.setCreator(account1);
        bill.setResponsible(account1);
        bill.setAccounts(Set.of(accountBill1, accountBill2));

        return bill;
    }
}
