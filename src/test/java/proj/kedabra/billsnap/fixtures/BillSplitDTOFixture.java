package proj.kedabra.billsnap.fixtures;

import java.math.BigDecimal;
import java.util.List;

import proj.kedabra.billsnap.business.dto.AccountDTO;
import proj.kedabra.billsnap.business.dto.BillSplitDTO;
import proj.kedabra.billsnap.business.dto.ItemAssociationSplitDTO;
import proj.kedabra.billsnap.business.dto.ItemPercentageSplitDTO;
import proj.kedabra.billsnap.business.entities.AccountItem;
import proj.kedabra.billsnap.business.utils.enums.AccountStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.GenderEnum;

public class BillSplitDTOFixture {
    private BillSplitDTOFixture() {}

    public static BillSplitDTO getDefault() {
        final var bill = BillEntityFixture.getDefault();
        final var item = bill.getItems().iterator().next();
        final var accountPercentageSplit = BigDecimal.valueOf(50);

        final var accountItem1 = new AccountItem();
        final var account1 = AccountEntityFixture.getDefaultAccount();
        account1.setEmail("abc123@email.com");
        account1.setId(1234L);
        account1.setMiddleName("middlename");
        account1.setGender(GenderEnum.MALE);
        account1.setPhoneNumber("123456789");
        account1.setStatus(AccountStatusEnum.REGISTERED);
        accountItem1.setAccount(account1);
        accountItem1.setItem(item);
        accountItem1.setPercentage(accountPercentageSplit);

        final var accountItem2 = new AccountItem();
        final var account2 = AccountEntityFixture.getDefaultAccount();
        account2.setEmail("hellomotto@cell.com");
        account2.setId(1357L);
        account2.setMiddleName("middlename");
        account2.setGender(GenderEnum.MALE);
        account2.setPhoneNumber("123456789");
        account2.setStatus(AccountStatusEnum.REGISTERED);
        accountItem2.setAccount(account2);
        accountItem2.setItem(item);
        accountItem2.setPercentage(accountPercentageSplit);

        final var billSplitDTO = new BillSplitDTO();
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

        final var accountDTO1 = new AccountDTO();
        accountDTO1.setEmail(account1.getEmail());
        accountDTO1.setId(account1.getId());
        billSplitDTO.setCreator(accountDTO1);
        billSplitDTO.setResponsible(accountDTO1);

        final var accountDTO2 = new AccountDTO();
        accountDTO2.setEmail(account2.getEmail());
        accountDTO2.setId(account2.getId());

        final var itemAssociationSplitDTO1 = new ItemAssociationSplitDTO();
        itemAssociationSplitDTO1.setAccount(accountDTO1);
        itemAssociationSplitDTO1.setCost(BigDecimal.valueOf(2));
        final var itemPercentageSplitDTO1 = new ItemPercentageSplitDTO();
        itemPercentageSplitDTO1.setPercentage(accountItem1.getPercentage());
        itemPercentageSplitDTO1.setCost(item.getCost());
        itemPercentageSplitDTO1.setName(item.getName());
        itemPercentageSplitDTO1.setId(item.getId());
        itemAssociationSplitDTO1.setItems(List.of(itemPercentageSplitDTO1));

        final var itemAssociationSplitDTO2 = new ItemAssociationSplitDTO();
        itemAssociationSplitDTO2.setAccount(accountDTO2);
        itemAssociationSplitDTO2.setCost(BigDecimal.valueOf(2));
        final var itemPercentageSplitDTO2 = new ItemPercentageSplitDTO();
        itemPercentageSplitDTO2.setPercentage(accountItem2.getPercentage());
        itemPercentageSplitDTO2.setCost(item.getCost());
        itemPercentageSplitDTO2.setName(item.getName());
        itemPercentageSplitDTO2.setId(item.getId());
        itemAssociationSplitDTO2.setItems(List.of(itemPercentageSplitDTO2));

        billSplitDTO.setItemsPerAccount(List.of(itemAssociationSplitDTO1, itemAssociationSplitDTO2));

        return billSplitDTO;
    }
}
