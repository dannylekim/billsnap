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

import proj.kedabra.billsnap.business.dto.AssociateBillDTO;
import proj.kedabra.billsnap.business.entities.Account;
import proj.kedabra.billsnap.business.entities.AccountItem;
import proj.kedabra.billsnap.business.mapper.AccountMapper;
import proj.kedabra.billsnap.business.mapper.BillMapper;
import proj.kedabra.billsnap.business.mapper.ItemMapper;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.business.repository.BillRepository;
import proj.kedabra.billsnap.business.service.BillService;
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
    private BillRepository billRepository;

    @Mock
    private BillService billService;

    private static final String ACCOUNT_DOES_NOT_EXIST = "Account does not exist";

    private static final String BILL_DOES_NOT_EXIST = "Bill does not exist";

    private static final String LIST_ACCOUNT_DOES_NOT_EXIST = "One or more accounts in the list of accounts does not exist: ";

    private static final String LIST_CANNOT_CONTAIN_BILL_CREATOR = "List of emails cannot contain bill creator email";

    private static final String ITEM_PERCENTAGES_MUST_ADD_TO_100 = "The percentage split for this item must add up to 100: {%s, Percentage: %s}";

    private static final String MUST_HAVE_ONLY_ONE_TYPE_OF_TIPPING = "Only one type of tipping is supported. Please make sure only either tip amount or tip percent is set.";

    @BeforeEach
    void setup() {

        MockitoAnnotations.initMocks(this);
        billFacade = new BillFacadeImpl(accountRepository, billRepository, billService, billMapper, accountMapper, itemMapper);

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
    @DisplayName("Should return exception if bill id does not reference existing bill")
    void shouldThrowExceptionIfBillDoesNotExist() {
        //Given
        AssociateBillDTO associateBillDTO = AssociateBillDTOFixture.getDefault();
        associateBillDTO.setId(9001L);
        when(billRepository.findById(any())).thenReturn(null);

        //When/Then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> billFacade.associateAccountsToBill(associateBillDTO))
                .withMessage(BILL_DOES_NOT_EXIST);
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

        when(billRepository.getBillById(any())).thenReturn(bill);

        //When/Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> billFacade.associateAccountsToBill(dto))
                .withMessage(String.format(ITEM_PERCENTAGES_MUST_ADD_TO_100, item.getName(), BigDecimal.valueOf(125)));
    }

    @Test
    @DisplayName("Should return BillSplitDTO with each account's total items cost sum and mapped to input Bill")
    void shouldReturnBillSplitDTOWithAccountItemsCostSum() {

    }


}