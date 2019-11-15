package proj.kedabra.billsnap.business.facade.impl;

import static org.assertj.core.api.Assertions.assertThat;
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

import proj.kedabra.billsnap.business.dto.BillCompleteDTO;
import proj.kedabra.billsnap.business.dto.BillDTO;
import proj.kedabra.billsnap.business.mapper.AccountMapper;
import proj.kedabra.billsnap.business.mapper.BillMapper;
import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.model.entities.AccountBill;
import proj.kedabra.billsnap.business.model.entities.Bill;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.business.service.BillService;
import proj.kedabra.billsnap.business.utils.enums.InvitationStatusEnum;
import proj.kedabra.billsnap.fixtures.AccountDTOFixture;
import proj.kedabra.billsnap.fixtures.AccountEntityFixture;
import proj.kedabra.billsnap.fixtures.BillCompleteDTOFixture;
import proj.kedabra.billsnap.fixtures.BillDTOFixture;
import proj.kedabra.billsnap.fixtures.BillEntityFixture;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;

class BillFacadeImplTest {

    private BillFacadeImpl billFacade;

    @Mock
    private BillMapper billMapper;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private BillService billService;

    @BeforeEach
    void setup() {

        MockitoAnnotations.initMocks(this);
        billFacade = new BillFacadeImpl(accountRepository, billService, billMapper, accountMapper);

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
        assertEquals(ErrorMessageEnum.ACCOUNT_DOES_NOT_EXIST.getMessage(), resourceNotFoundException.getMessage());

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
                .withMessage("One or more accounts in the list of accounts does not exist: [%s, %s]", nonExistentEmail, nonExistentEmail2);
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
                .withMessage(ErrorMessageEnum.LIST_CANNOT_CONTAIN_BILL_CREATOR.getMessage());
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
                .withMessage(ErrorMessageEnum.ACCOUNT_DOES_NOT_EXIST.getMessage());
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

        assertEquals(ErrorMessageEnum.MULTIPLE_TIP_METHOD.getMessage(), illegalArgumentException.getMessage());
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

        assertEquals(ErrorMessageEnum.MULTIPLE_TIP_METHOD.getMessage(), illegalArgumentException.getMessage());

    }

    @Test
    @DisplayName("Should return list of AccountStatusPair's if inputted accountsList is not null")
    void shouldContainAccountStatusPairIfAccountsListNotNull() {
        //Given
        final Account billCreatorAccount = AccountEntityFixture.getDefaultAccount();
        final String billCreator = "billcreator@email.com";
        billCreatorAccount.setEmail(billCreator);
        final Account existingAccount = AccountEntityFixture.getDefaultAccount();
        final Bill mappedBillFixture = BillEntityFixture.getMappedBillDTOFixture();
        final BillDTO billDTO = BillDTOFixture.getDefault();
        billDTO.setAccountsList(List.of(existingAccount.getEmail()));
        final AccountBill accountBill = new AccountBill();
        accountBill.setAccount(existingAccount);
        accountBill.setBill(mappedBillFixture);
        accountBill.setStatus(InvitationStatusEnum.ACCEPTED);
        mappedBillFixture.setAccounts(Set.of(accountBill));
        mappedBillFixture.setCreator(billCreatorAccount);

        when(accountRepository.getAccountByEmail(billCreator)).thenReturn(AccountEntityFixture.getDefaultAccount());
        when(accountRepository.getAccountsByEmailIn(any())).thenReturn(Stream.of(existingAccount));
        when(billService.createBillToAccount(any(), any(), any())).thenReturn(mappedBillFixture);
        when(billMapper.toDTO(any(Bill.class))).thenReturn(BillCompleteDTOFixture.getDefault());
        when(accountMapper.toDTO(any(Account.class))).thenReturn(AccountDTOFixture.getCreationDTO());

        //When
        final BillCompleteDTO billCompleteDTO = billFacade.addPersonalBill(billCreator, billDTO);

        //Then
        assertThat(billCompleteDTO.getAccountsList()).isNotEmpty();
        assertThat(billCompleteDTO.getAccountsList().get(0).getAccount().getEmail()).isEqualTo(AccountDTOFixture.getCreationDTO().getEmail());
        assertThat(billCompleteDTO.getAccountsList().get(0).getStatus()).isEqualTo(InvitationStatusEnum.ACCEPTED);
    }
}