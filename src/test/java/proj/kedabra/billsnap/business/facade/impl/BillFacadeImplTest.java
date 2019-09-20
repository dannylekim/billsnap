package proj.kedabra.billsnap.business.facade.impl;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;

import proj.kedabra.billsnap.business.dto.AccountDTO;
import proj.kedabra.billsnap.business.dto.BillSplitDTO;
import proj.kedabra.billsnap.business.dto.ItemAssociationSplitDTO;
import proj.kedabra.billsnap.business.dto.ItemPercentageSplitDTO;
import proj.kedabra.billsnap.business.entities.Account;
import proj.kedabra.billsnap.business.entities.AccountBill;
import proj.kedabra.billsnap.business.entities.AccountItem;
import proj.kedabra.billsnap.business.entities.Bill;
import proj.kedabra.billsnap.business.entities.Item;
import proj.kedabra.billsnap.business.mapper.AccountMapper;
import proj.kedabra.billsnap.business.mapper.BillMapper;
import proj.kedabra.billsnap.business.mapper.ItemMapper;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.business.service.BillService;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.fixtures.AccountEntityFixture;
import proj.kedabra.billsnap.fixtures.AssociateBillDTOFixture;
import proj.kedabra.billsnap.fixtures.BillDTOFixture;
import proj.kedabra.billsnap.fixtures.BillEntityFixture;

class BillFacadeImplTest {

    private BillFacadeImpl billFacade;

    @Mock
    private BillMapper billMapper;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private BillService billService;

    private static final String ACCOUNT_DOES_NOT_EXIST = "Account does not exist";

    private static final String LIST_ACCOUNT_DOES_NOT_EXIST = "One or more accounts in the list of accounts does not exist: ";

    private static final String LIST_CANNOT_CONTAIN_BILL_CREATOR = "List of emails cannot contain bill creator email";

    private static final String ITEM_PERCENTAGES_MUST_ADD_TO_100 = "The percentage split for this item must add up to 100: {%s, Percentage: %s}";

    private static final String MUST_HAVE_ONLY_ONE_TYPE_OF_TIPPING = "Only one type of tipping is supported. Please make sure only either tip amount or tip percent is set.";

    @BeforeEach
    void setup() {

        MockitoAnnotations.initMocks(this);
        billFacade = new BillFacadeImpl(accountRepository, billService, billMapper, accountMapper, itemMapper);

    }

    @Test
    @DisplayName("Should return an exception if given an email that does not exist")
    void shouldReturnExceptionIfEmailDoesNotExist() {
        // Given
        final var billDTO = BillDTOFixture.getDefault();
        final String testEmail = "abc@123.ca";
        when(accountRepository.getAccountByEmail(testEmail)).thenReturn(null);

        // When/Then
        final ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class,
                () -> billFacade.addPersonalBill(testEmail, billDTO));
        assertEquals(ACCOUNT_DOES_NOT_EXIST, resourceNotFoundException.getMessage());

    }

    @Test
    @DisplayName("Should return exception if list of emails contains one or more emails that do not exist")
    void ShouldReturnExceptionIfEmailInListOfEmailsDoesNotExist() {
        //Given a bill creator with existing email, but billDTO containing non-existent email in array of emails
        final var billDTO = BillDTOFixture.getDefault();
        final String nonExistentEmail = "abc@123.ca";
        final String nonExistentEmail2 = "lalala@email.com";
        final String existingEmail = "accountentity@test.com";
        final String existingEmail2 = "existing2@email.com";
        final Account existingAccount = AccountEntityFixture.getDefaultAccount();
        existingAccount.setEmail(existingEmail2);
        billDTO.setAccountsList(List.of(existingEmail2, nonExistentEmail, nonExistentEmail2));
        when(accountRepository.getAccountsByEmailIn(any())).thenReturn(Stream.of(existingAccount));
        when(accountRepository.getAccountByEmail(existingEmail)).thenReturn(AccountEntityFixture.getDefaultAccount());

        //When/Then

        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() -> billFacade.addPersonalBill(existingEmail, billDTO))
                .withMessage(LIST_ACCOUNT_DOES_NOT_EXIST + "[%s, %s]", nonExistentEmail, nonExistentEmail2);
    }

    @Test
    @DisplayName("Should return exception if list of emails contains bill creator email")
    void ShouldReturnExceptionIfBillCreatorIsInListOfEmails() {
        //Given
        final var billDTO = BillDTOFixture.getDefault();
        final String billCreator = "accountentity@test.com";
        final String existentEmail = "existent@email.com";
        billDTO.setAccountsList(List.of(billCreator, existentEmail));
        when(accountRepository.getAccountByEmail(billCreator)).thenReturn(AccountEntityFixture.getDefaultAccount());

        //When/Then
        assertThatIllegalArgumentException().isThrownBy(() -> billFacade.addPersonalBill(billCreator, billDTO))
                .withMessage(LIST_CANNOT_CONTAIN_BILL_CREATOR);
    }

    @Test
    @DisplayName("Should return exception if email does not exist in GetAllBills")
    void shouldThrowExceptionIfEmailDoesNotExistInGetAllBills() {
        //Given
        final String nonExistentEmail = "nonexistent@email.ca";
        when(accountRepository.getAccountByEmail(any())).thenReturn(null);

        //When/Then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> billFacade.getAllBillsByEmail(nonExistentEmail))
                .withMessage(ACCOUNT_DOES_NOT_EXIST);
    }

    @Test
    @DisplayName("Should return exception if both tipping methods are null")
    void shouldThrowExceptionIfBothTipNull() {
        //Given
        final var billDTO = BillDTOFixture.getDefault();
        billDTO.setTipAmount(null);
        billDTO.setTipPercent(null);
        final String testEmail = "abc@123.ca";

        //When/Then
        final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> billFacade.addPersonalBill(testEmail, billDTO));

        assertEquals(MUST_HAVE_ONLY_ONE_TYPE_OF_TIPPING, illegalArgumentException.getMessage());
    }

    @Test
    @DisplayName("Should return exception if both tipping methods are not null")
    void shouldThrowExceptionIfBothTipNotNull() {
        //Given
        final var billDTO = BillDTOFixture.getDefault();
        billDTO.setTipAmount(BigDecimal.ZERO);
        billDTO.setTipPercent(BigDecimal.ZERO);
        final String testEmail = "abc@123.ca";

        //When/Then
        final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> billFacade.addPersonalBill(testEmail, billDTO));

        assertEquals(MUST_HAVE_ONLY_ONE_TYPE_OF_TIPPING, illegalArgumentException.getMessage());

    }

    @Test
    @DisplayName("Should throw exception if bill items percentage split does not add up to hundred")
    void shouldThrowExceptionIfItemPercentagesDoNotAddToHundred() {
        //Given bill with 1 item {name: yogurt, cost: 4}
        final var dto = AssociateBillDTOFixture.getDefault();
        final var bill = BillEntityFixture.getDefault();
        final var item = bill.getItems().iterator().next();
        final var accountItem1 = new AccountItem();
        final var accountItem2 = new AccountItem();
        final var account1 = AccountEntityFixture.getDefaultAccount();
        account1.setEmail("abc123@email.com");
        final var account2 = AccountEntityFixture.getDefaultAccount();
        account2.setEmail("hellomotto@cell.com");

        accountItem1.setAccount(account1);
        accountItem1.setItem(item);
        accountItem1.setPercentage(BigDecimal.valueOf(50));
        accountItem2.setAccount(account2);
        accountItem2.setItem(item);
        accountItem2.setPercentage(BigDecimal.valueOf(75));

        item.setAccounts(Set.of(accountItem1, accountItem2));

        when(billService.associateItemToAccountBill(any())).thenReturn(bill);

        //When/Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> billFacade.associateAccountsToBill(dto))
                .withMessage(String.format(ITEM_PERCENTAGES_MUST_ADD_TO_100, item.getName(), BigDecimal.valueOf(125)));
    }

    //    @Test
//    @DisplayName("Should return BillSplitDTO with each account's total items cost sum and mapped to input Bill")
    void shouldReturnBillSplitDTOWithAccountItemsCostSum() {
        //Given bill with 1 item {name: yogurt, cost: 4}
        final var dto = AssociateBillDTOFixture.getDefault();
        final var bill = BillEntityFixture.getDefault();
        final var item = bill.getItems().iterator().next();

        final var accountItem1 = new AccountItem();
        final var account1 = AccountEntityFixture.getDefaultAccount();
        account1.setEmail("abc123@email.com");
        accountItem1.setAccount(account1);
        accountItem1.setItem(item);
        accountItem1.setPercentage(BigDecimal.valueOf(50));
        final AccountBill accountBill1 = new AccountBill();
        accountBill1.setBill(bill);
        accountBill1.setAccount(account1);
        accountBill1.setPercentage(BigDecimal.ZERO);

        final var accountItem2 = new AccountItem();
        final var account2 = AccountEntityFixture.getDefaultAccount();
        account2.setEmail("hellomotto@cell.com");
        accountItem2.setAccount(account2);
        accountItem2.setItem(item);
        accountItem2.setPercentage(BigDecimal.valueOf(50));
        final AccountBill accountBill2 = new AccountBill();
        accountBill2.setBill(bill);
        accountBill2.setAccount(account2);
        accountBill2.setPercentage(BigDecimal.ZERO);

        item.setAccounts(Set.of(accountItem1, accountItem2));

        bill.setCreator(account1);
        bill.setResponsible(account1);
        bill.setAccounts(Set.of(accountBill1, accountBill2));

        when(billService.associateItemToAccountBill(any())).thenReturn(bill);

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

        final AccountDTO accountDTO1 = new AccountDTO();
        accountDTO1.setEmail(account1.getEmail());
        accountDTO1.setId(account1.getId());
        billSplitDTO.setCreator(accountDTO1);
        billSplitDTO.setResponsible(accountDTO1);

        final AccountDTO accountDTO2 = new AccountDTO();
        accountDTO2.setEmail(account2.getEmail());
        accountDTO2.setId(account2.getId());

        final ItemAssociationSplitDTO itemAssociationSplitDTO1 = new ItemAssociationSplitDTO();
        itemAssociationSplitDTO1.setAccount(accountDTO1);
        itemAssociationSplitDTO1.setCost(BigDecimal.valueOf(2));
        final ItemPercentageSplitDTO itemPercentageSplitDTO1 = new ItemPercentageSplitDTO();
        itemPercentageSplitDTO1.setPercentage(accountItem1.getPercentage());
        itemPercentageSplitDTO1.setCost(item.getCost());
        itemPercentageSplitDTO1.setName(item.getName());
        itemPercentageSplitDTO1.setId(item.getId());
        itemAssociationSplitDTO1.setItems(List.of(itemPercentageSplitDTO1));

        final ItemAssociationSplitDTO itemAssociationSplitDTO2 = new ItemAssociationSplitDTO();
        itemAssociationSplitDTO2.setAccount(accountDTO1);
        itemAssociationSplitDTO2.setCost(BigDecimal.valueOf(2));
        final ItemPercentageSplitDTO itemPercentageSplitDTO2 = new ItemPercentageSplitDTO();
        itemPercentageSplitDTO2.setPercentage(accountItem1.getPercentage());
        itemPercentageSplitDTO2.setCost(item.getCost());
        itemPercentageSplitDTO2.setName(item.getName());
        itemPercentageSplitDTO2.setId(item.getId());
        itemAssociationSplitDTO2.setItems(List.of(itemPercentageSplitDTO2));

        billSplitDTO.setItemsPerAccount(List.of(itemAssociationSplitDTO1, itemAssociationSplitDTO2));

        when(billMapper.toBillSplitDTO(any())).thenReturn(billSplitDTO);

        //When
        final BillSplitDTO returnBillSplitDTO = billFacade.associateAccountsToBill(dto);

        //Then
        verifyBillSplitDTOToBill(returnBillSplitDTO, bill);

    }

    private void verifyBillSplitDTOToBill(BillSplitDTO billSplitDTO, Bill bill) {
        final Account billCreatorAccount = bill.getAccounts().stream().map(AccountBill::getAccount)
                .filter(acc -> acc.equals(bill.getCreator()))
                .iterator().next();
        assertEquals(billCreatorAccount.getId(), billSplitDTO.getCreator().getId());
        assertEquals(billCreatorAccount.getId(), billSplitDTO.getResponsible().getId());
        assertEquals(BillStatusEnum.OPEN, bill.getStatus());
        assertEquals(bill.getId(), billSplitDTO.getId());
        assertEquals(bill.getCategory(), billSplitDTO.getCategory());
        assertEquals(bill.getCompany(), billSplitDTO.getCompany());

        final List<ItemAssociationSplitDTO> itemsPerAccount = billSplitDTO.getItemsPerAccount();
        final Set<AccountBill> accounts = bill.getAccounts();
        assertEquals(accounts.size(), itemsPerAccount.size());

        final Set<Item> billItems = bill.getItems();
        if (!itemsPerAccount.get(0).getItems().isEmpty()) {
            //for the time being we verify a bill with only 1 item. Should be generic when needed.
            final Item item = billItems.iterator().next();
            final ItemPercentageSplitDTO returnItemPercentageSplitDTO = itemsPerAccount.get(0).getItems().get(0);
            assertEquals(item.getName(), returnItemPercentageSplitDTO.getName());
            assertEquals(item.getCost(), returnItemPercentageSplitDTO.getCost());
            assertEquals(item.getCost(), billSplitDTO.getBalance());
            assertEquals(bill.getName(), billSplitDTO.getName());
            assertEquals(bill.getId(), billSplitDTO.getId());
            assertEquals(bill.getStatus(), billSplitDTO.getStatus());
            assertEquals(bill.getCreated(), billSplitDTO.getCreated());
            assertEquals(bill.getUpdated(), billSplitDTO.getUpdated());
        } else {
            assertEquals(0, BigDecimal.ZERO.compareTo(billSplitDTO.getBalance()));
        }
    }

}