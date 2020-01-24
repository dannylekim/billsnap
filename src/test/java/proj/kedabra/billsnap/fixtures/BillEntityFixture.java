package proj.kedabra.billsnap.fixtures;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.model.entities.AccountBill;
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
        bill.setId(5000L);

        return bill;
    }

    public static Bill getMappedBillDTOFixture() {
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

        final var account1 = AccountEntityFixture.getDefaultAccount();
        final var accountItem1 = AccountItemEntityFixture.getDefault();
        accountItem1.setAccount(account1);
        accountItem1.setItem(item);
        accountItem1.setPercentage(accountPercentageSplit);
        final AccountBill accountBill1 = getAccountBill(bill, account1);

        final var account2 = AccountEntityFixture.getDefaultAccount();
        account2.setEmail("hellomotto@cell.com");
        account2.setId(1357L);
        final var accountItem2 = AccountItemEntityFixture.getDefault();
        accountItem2.setAccount(account2);
        accountItem2.setItem(item);
        accountItem2.setPercentage(accountPercentageSplit);
        final AccountBill accountBill2 = getAccountBill(bill, account2);

        item.setAccounts(Set.of(accountItem1, accountItem2));
        bill.setCreator(account1);
        bill.setResponsible(account1);
        bill.setAccounts(Set.of(accountBill1, accountBill2));
        bill.setStatus(BillStatusEnum.OPEN);

        return bill;
    }

    public static Bill getMappedBillSplitDTOFixtureGivenSplitPercentage(BigDecimal splitPercentage) {
        final var bill = BillEntityFixture.getDefault();
        final var item = bill.getItems().iterator().next();

        final var accountItem1 = AccountItemEntityFixture.getDefault();
        final var account1 = AccountEntityFixture.getDefaultAccount();

        accountItem1.setAccount(account1);
        accountItem1.setItem(item);
        accountItem1.setPercentage(splitPercentage);
        final AccountBill accountBill1 = getAccountBill(bill, account1);

        final var accountItem2 = AccountItemEntityFixture.getDefault();
        final var account2 = AccountEntityFixture.getDefaultAccount();
        account2.setEmail("hellomotto@cell.com");
        account2.setId(1357L);
        accountItem2.setAccount(account2);
        accountItem2.setItem(item);
        accountItem2.setPercentage(splitPercentage);
        final AccountBill accountBill2 = getAccountBill(bill, account2);

        item.setAccounts(Set.of(accountItem1, accountItem2));
        bill.setCreator(account1);
        bill.setResponsible(account1);
        bill.setAccounts(Set.of(accountBill1, accountBill2));
        bill.setStatus(BillStatusEnum.OPEN);

        return bill;
    }
    private static AccountBill getAccountBill(Bill bill, Account account) {
        final var accountBill = AccountBillEntityFixture.getDefault();
        accountBill.setBill(bill);
        accountBill.setAccount(account);
        accountBill.setPercentage(BigDecimal.ZERO);
        return accountBill;
    }
}
