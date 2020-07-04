package proj.kedabra.billsnap.fixtures;

import java.math.BigDecimal;
import java.util.List;

import proj.kedabra.billsnap.business.dto.BillSplitDTO;

public final class BillSplitDTOFixture {

    private BillSplitDTOFixture() {}

    public static BillSplitDTO getDefault() {
        final var billSplitDTO = new BillSplitDTO();
        final var bill = BillEntityFixture.getDefault();
        final var item = bill.getItems().iterator().next();
        final var accountPercentageSplit = BigDecimal.valueOf(50);

        final var accountItem1 = AccountItemEntityFixture.getDefault();
        final var account1 = AccountEntityFixture.getDefaultAccount();
        accountItem1.setAccount(account1);
        accountItem1.setItem(item);
        accountItem1.setPercentage(accountPercentageSplit);

        final var accountItem2 = AccountItemEntityFixture.getDefault();
        final var account2 = AccountEntityFixture.getDefaultAccount();
        account2.setEmail("hellomotto@cell.com");
        account2.setId(1357L);
        accountItem2.setAccount(account2);
        accountItem2.setItem(item);
        accountItem2.setPercentage(accountPercentageSplit);

        billSplitDTO.setBalance(item.getCost().add(bill.getTipAmount()));
        billSplitDTO.setTotalTip(bill.getTipAmount());
        billSplitDTO.setCategory(bill.getCategory());
        billSplitDTO.setCompany(bill.getCompany());
        billSplitDTO.setCreated(bill.getCreated());
        billSplitDTO.setId(bill.getId());
        billSplitDTO.setName(bill.getName());
        billSplitDTO.setSplitBy(bill.getSplitBy());
        billSplitDTO.setStatus(bill.getStatus());
        billSplitDTO.setUpdated(bill.getUpdated());

        final var accountDTO1 = AccountDTOFixture.getMappedDefaultAccount();
        accountDTO1.setEmail(account1.getEmail());
        accountDTO1.setId(account1.getId());
        billSplitDTO.setCreator(accountDTO1);
        billSplitDTO.setResponsible(accountDTO1);

        final var accountDTO2 = AccountDTOFixture.getMappedDefaultAccount();
        accountDTO2.setEmail(account2.getEmail());
        accountDTO2.setId(account2.getId());

        final var itemAssociationSplitDTO1 = ItemAssociationSplitDTOFixture.getDefault();
        itemAssociationSplitDTO1.setAccount(accountDTO1);
        itemAssociationSplitDTO1.setSubTotal(BigDecimal.valueOf(2.0));
        final var itemPercentageSplitDTO1 = ItemPercentageSplitDTOFixture.getDefault();
        itemPercentageSplitDTO1.setPercentage(accountItem1.getPercentage());
        itemPercentageSplitDTO1.setCost(item.getCost());
        itemPercentageSplitDTO1.setName(item.getName());
        itemPercentageSplitDTO1.setItemId(item.getId());
        itemAssociationSplitDTO1.setItems(List.of(itemPercentageSplitDTO1));

        final var itemAssociationSplitDTO2 = ItemAssociationSplitDTOFixture.getDefault();
        itemAssociationSplitDTO2.setAccount(accountDTO2);
        itemAssociationSplitDTO2.setSubTotal(BigDecimal.valueOf(2.0));
        final var itemPercentageSplitDTO2 = ItemPercentageSplitDTOFixture.getDefault();
        itemPercentageSplitDTO2.setPercentage(accountItem2.getPercentage());
        itemPercentageSplitDTO2.setCost(item.getCost());
        itemPercentageSplitDTO2.setName(item.getName());
        itemPercentageSplitDTO2.setItemId(item.getId());
        itemAssociationSplitDTO2.setItems(List.of(itemPercentageSplitDTO2));

        billSplitDTO.setItemsPerAccount(List.of(itemAssociationSplitDTO1, itemAssociationSplitDTO2));

        return billSplitDTO;
    }
}
