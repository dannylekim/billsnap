package proj.kedabra.billsnap.business.facade.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import proj.kedabra.billsnap.business.dto.AccountDTO;
import proj.kedabra.billsnap.business.dto.BillCompleteDTO;
import proj.kedabra.billsnap.business.dto.BillDTO;
import proj.kedabra.billsnap.business.dto.BillSplitDTO;
import proj.kedabra.billsnap.business.dto.ItemAssociationSplitDTO;
import proj.kedabra.billsnap.business.dto.ItemPercentageSplitDTO;
import proj.kedabra.billsnap.business.exception.AccessForbiddenException;
import proj.kedabra.billsnap.business.exception.ResourceNotFoundException;
import proj.kedabra.billsnap.business.mapper.AccountMapper;
import proj.kedabra.billsnap.business.mapper.BillMapper;
import proj.kedabra.billsnap.business.mapper.ItemMapper;
import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.model.entities.AccountBill;
import proj.kedabra.billsnap.business.model.entities.Bill;
import proj.kedabra.billsnap.business.model.entities.Item;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.business.service.AccountService;
import proj.kedabra.billsnap.business.service.BillService;
import proj.kedabra.billsnap.business.service.CalculatePaymentService;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.InvitationStatusEnum;
import proj.kedabra.billsnap.fixtures.AccountBillEntityFixture;
import proj.kedabra.billsnap.fixtures.AccountDTOFixture;
import proj.kedabra.billsnap.fixtures.AccountEntityFixture;
import proj.kedabra.billsnap.fixtures.AssociateBillDTOFixture;
import proj.kedabra.billsnap.fixtures.BillCompleteDTOFixture;
import proj.kedabra.billsnap.fixtures.BillDTOFixture;
import proj.kedabra.billsnap.fixtures.BillEntityFixture;
import proj.kedabra.billsnap.fixtures.BillSplitDTOFixture;
import proj.kedabra.billsnap.fixtures.InviteRegisteredResourceFixture;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;

@ExtendWith(MockitoExtension.class)
class BillFacadeImplTest {

    @InjectMocks
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
    private AccountService accountService;

    @Mock
    private BillService billService;

    @Mock
    private CalculatePaymentService calculatePaymentService;

    private static final BigDecimal PERCENTAGE_DIVISOR = BigDecimal.valueOf(100);

    private static final String ITEM_PERCENTAGES_MUST_ADD_TO_100 = "The percentage split for this item must add up to 100: {%s, Percentage: %s}";

    @BeforeEach
    void setCalculatePaymentServiceMock() {
        //Because we are not testing any of the values within another module in this unit test, we are simply setting these default values to pass tests
        //We use lenient so that we do not get the unnecessary stubbing exception
        //Note that we are returning a non-zero value to avoid divide by 0 arithmetic exceptions
        lenient().when(calculatePaymentService.calculateBalance(any())).thenReturn(BigDecimal.ONE);
        lenient().when(calculatePaymentService.calculateTaxes(any(), any())).thenReturn(BigDecimal.ONE);
        lenient().when(calculatePaymentService.calculateTip(any(), any(), any())).thenReturn(BigDecimal.ONE);
        lenient().when(calculatePaymentService.calculateSubTotal(any())).thenReturn(BigDecimal.ONE);
    }

    @Test
    @DisplayName("Should return an exception if given an email that does not exist")
    void shouldReturnExceptionIfEmailDoesNotExist() {
        // Given
        final var billDTO = BillDTOFixture.getDefault();
        final String testEmail = "abc@123.ca";
        when(accountService.getAccount(testEmail)).thenThrow(new ResourceNotFoundException(ErrorMessageEnum.ACCOUNT_DOES_NOT_EXIST.getMessage()));

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
        when(accountService.getAccounts(any()))
                .thenThrow(new ResourceNotFoundException(ErrorMessageEnum.LIST_ACCOUNT_DOES_NOT_EXIST.getMessage(List.of(nonExistentEmail, nonExistentEmail2).toString())));

        //When/Then

        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() -> billFacade.addPersonalBill(existingEmail, billDTO))
                .withMessage(ErrorMessageEnum.LIST_ACCOUNT_DOES_NOT_EXIST.getMessage(List.of(nonExistentEmail, nonExistentEmail2).toString()));
    }

    @Test
    @DisplayName("Should return exception if list of emails contains bill creator email")
    void ShouldReturnExceptionIfBillCreatorIsInListOfEmails() {
        //Given
        final var billDTO = BillDTOFixture.getDefault();
        final String billCreator = "accountentity@test.com";
        final String existentEmail = "existent@email.com";
        billDTO.setAccountsList(List.of(billCreator, existentEmail));

        //When/Then
        assertThatIllegalArgumentException().isThrownBy(() -> billFacade.addPersonalBill(billCreator, billDTO))
                .withMessage(ErrorMessageEnum.LIST_CANNOT_CONTAIN_BILL_CREATOR.getMessage());
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

        when(billService.createBillToAccount(any(), any(), any())).thenReturn(mappedBillFixture);
        when(billMapper.toBillCompleteDTO(any(Bill.class))).thenReturn(BillCompleteDTOFixture.getDefault());
        when(accountMapper.toDTO(any(Account.class))).thenReturn(AccountDTOFixture.getCreationDTO());

        //When
        final BillCompleteDTO billCompleteDTO = billFacade.addPersonalBill(billCreator, billDTO);

        //Then
        assertThat(billCompleteDTO.getAccountsList()).isNotEmpty();
        assertThat(billCompleteDTO.getAccountsList().get(0).getAccount().getEmail()).isEqualTo(AccountDTOFixture.getCreationDTO().getEmail());
        assertThat(billCompleteDTO.getAccountsList().get(0).getStatus()).isEqualTo(InvitationStatusEnum.ACCEPTED);
    }

    @Test
    @DisplayName("Should throw exception if bill items percentage split does not add up to hundred")
    void shouldThrowExceptionIfItemPercentagesDoNotAddToHundred() {
        //Given bill with 1 item {name: yogurt, cost: 4}
        final var dto = AssociateBillDTOFixture.getDefault();
        final var bill = BillEntityFixture.getMappedBillSplitDTOFixtureGivenSplitPercentage(BigDecimal.valueOf(150));
        final var item = bill.getItems().iterator().next();
        final var accountPercentageSplit = BigDecimal.valueOf(50);
        final var billSplitDTO = BillSplitDTOFixture.getDefault();
        billSplitDTO.setInformationPerAccount(null);

        when(billService.associateItemsToAccountBill(any())).thenReturn(bill);
        when(billMapper.toBillSplitDTO(any())).thenReturn(billSplitDTO);
        when(itemMapper.toItemPercentageSplitDTO(any(Item.class))).thenAnswer(
                i -> {
                    Item itemInput = (Item) i.getArguments()[0];
                    final ItemPercentageSplitDTO itemDTO = new ItemPercentageSplitDTO();
                    itemDTO.setItemId(itemInput.getId());
                    itemDTO.setName(itemInput.getName());
                    itemDTO.setCost(itemInput.getCost());
                    itemDTO.setPercentage(accountPercentageSplit);
                    return itemDTO;
                }
        );


        //When/Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> billFacade.associateAccountsToBill(dto))
                .withMessage(String.format(ITEM_PERCENTAGES_MUST_ADD_TO_100, item.getName(), BigDecimal.valueOf(300)));
    }

    @SuppressWarnings("BigDecimalMethodWithoutRoundingCalled")
    @Test
    @DisplayName("Should return BillSplitDTO with each account's total items cost sum and mapped to input Bill")
    void shouldReturnBillSplitDTOWithAccountItemsCostSum() {
        //Given bill with 1 item {name: yogurt, cost: 4}
        final var dto = AssociateBillDTOFixture.getDefault();
        final var bill = BillEntityFixture.getMappedBillSplitDTOFixture();
        bill.getTaxes().clear();
        final var item = bill.getItems().iterator().next();
        final var accountPercentageSplit = BigDecimal.valueOf(50);
        final var billSplitDTO = BillSplitDTOFixture.getDefault();
        billSplitDTO.setInformationPerAccount(null);

        when(billService.associateItemsToAccountBill(any())).thenReturn(bill);
        when(billMapper.toBillSplitDTO(any())).thenReturn(billSplitDTO);
        when(itemMapper.toItemPercentageSplitDTO(any(Item.class))).thenAnswer(
                i -> {
                    Item itemInput = (Item) i.getArguments()[0];
                    final ItemPercentageSplitDTO itemDTO = new ItemPercentageSplitDTO();
                    itemDTO.setItemId(itemInput.getId());
                    itemDTO.setName(itemInput.getName());
                    itemDTO.setCost(itemInput.getCost());
                    itemDTO.setPercentage(accountPercentageSplit);
                    return itemDTO;
                }
        );
        when(accountMapper.toDTO(any(Account.class))).thenAnswer(
                a -> {
                    Account acc = (Account) a.getArguments()[0];
                    final AccountDTO accountDTO = new AccountDTO();
                    accountDTO.setId(acc.getId());
                    accountDTO.setEmail(acc.getEmail());
                    return accountDTO;
                }
        );

        //When
        final BillSplitDTO returnBillSplitDTO = billFacade.associateAccountsToBill(dto);

        //Then
        verifyBillSplitDTOToBill(returnBillSplitDTO, bill);

        assertThat(returnBillSplitDTO.getInformationPerAccount().get(0).getSubTotal())
                .isEqualTo(item.getCost().multiply(accountPercentageSplit.divide(PERCENTAGE_DIVISOR).setScale(CalculatePaymentService.DOLLAR_SCALE, RoundingMode.HALF_UP)));
    }

    @Test
    @DisplayName("Should throw error if billId references non-existent bill in Invite Registered call")
    void shouldThrowErrorIfNonExistentBillIdInInviteRegistered() {
        //Given
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        final var accountNotInBill = "nobills@inthisemail.com";
        final Long nonExistentBillId = 90019001L;
        final var accounts = List.of(accountNotInBill);
        inviteRegisteredResource.setAccounts(accounts);

        when(billService.getBill(any())).thenThrow(new ResourceNotFoundException(ErrorMessageEnum.BILL_ID_DOES_NOT_EXIST.getMessage(nonExistentBillId.toString())));

        //When/Then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> billFacade.inviteRegisteredToBill(nonExistentBillId, accounts))
                .withMessage(ErrorMessageEnum.BILL_ID_DOES_NOT_EXIST.getMessage(nonExistentBillId.toString()));
    }

    @Test
    @DisplayName("Should throw error if one account does not exist in Invite Registered call")
    void shouldThrowErrorIfOneAccountDoesNotExistInInviteRegistered() {
        //Given
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        final var billResponsible = "test@email.com";
        final var emailNotInBill = "nobills@inthisemail.com";
        final var nonExistentEmail = "clearly@nonexistent.gov";
        final var existentBillId = 1000L;
        final var accountsList = List.of(emailNotInBill, nonExistentEmail);
        inviteRegisteredResource.setAccounts(accountsList);

        final var bill = BillEntityFixture.getDefault();
        final var principal = AccountEntityFixture.getDefaultAccount();
        principal.setEmail(billResponsible);
        bill.setResponsible(principal);
        bill.setId(existentBillId);

        final var nonExistentEmails = List.of(nonExistentEmail);

        when(billService.getBill(any())).thenReturn(bill);
        when(accountService.getAccounts(any())).thenThrow(new ResourceNotFoundException(ErrorMessageEnum.LIST_ACCOUNT_DOES_NOT_EXIST.getMessage(nonExistentEmails.toString())));

        //When/Then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> billFacade.inviteRegisteredToBill(existentBillId, accountsList))
                .withMessage(ErrorMessageEnum.LIST_ACCOUNT_DOES_NOT_EXIST.getMessage(nonExistentEmails.toString()));
    }

    @Test
    @DisplayName("Should throw error if one account is already part of Bill in Invite Registered call")
    void shouldThrowErrorIfOneAccountIsPartOfBillInInviteRegistered() {
        //Given
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        final var billResponsible = "test@email.com";
        final var emailNotInBill = "nobills@inthisemail.com";
        final var emailInBill = "email@inbill.com";
        final var existentBillId = 1000L;
        final var accountsList = List.of(emailNotInBill, emailInBill);
        inviteRegisteredResource.setAccounts(accountsList);

        final var accountNotInBill = AccountEntityFixture.getDefaultAccount();
        accountNotInBill.setEmail(emailNotInBill);
        final var accountInBill = AccountEntityFixture.getDefaultAccount();
        accountInBill.setEmail(emailInBill);

        final var bill = BillEntityFixture.getDefault();
        final var principal = AccountEntityFixture.getDefaultAccount();
        principal.setEmail(billResponsible);
        bill.setResponsible(principal);
        bill.setId(existentBillId);

        final var accountBill = AccountBillEntityFixture.getDefault();
        accountBill.setAccount(accountInBill);
        accountBill.setBill(bill);

        bill.setAccounts(Set.of(accountBill));

        final var existentAccountList = List.of(accountInBill, accountNotInBill);

        when(accountService.getAccounts(any())).thenReturn(existentAccountList);
        when(billService.getBill(any())).thenReturn(bill);

        //When/Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> billFacade.inviteRegisteredToBill(existentBillId, accountsList))
                .withMessage(ErrorMessageEnum.LIST_ACCOUNT_ALREADY_IN_BILL.getMessage(List.of(emailInBill).toString()));
    }

    @Test
    @DisplayName("Should return mapped PendingRegisteredBillSplitDTO when Invite Registered Call with one new User")
    void shouldReturnMappedPendingRegisteredBillSplitDTOInInviteRegistered() {
        //Given
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        final var billResponsible = "test@email.com";
        final var emailNotInBill = "nobills@inthisemail.com";
        final var existentBillId = 1000L;
        final List<String> invitedAccountsList = List.of(emailNotInBill);
        inviteRegisteredResource.setAccounts(invitedAccountsList);

        final var accountNotInBill = AccountEntityFixture.getDefaultAccount();
        accountNotInBill.setEmail(emailNotInBill);

        final var bill = BillEntityFixture.getMappedBillSplitDTOFixture();
        bill.getTaxes().clear();
        final var principal = AccountEntityFixture.getDefaultAccount();
        principal.setEmail(billResponsible);
        bill.setResponsible(principal);
        bill.setId(existentBillId);
        bill.getAccounts().forEach(ab -> ab.setStatus(InvitationStatusEnum.ACCEPTED));

        final var defaultAccount = AccountEntityFixture.getDefaultAccount();
        defaultAccount.setEmail(emailNotInBill);

        when(billService.inviteRegisteredToBill(any(Bill.class), any())).thenAnswer(
                arg -> {
                    final Bill billInput = (Bill) arg.getArguments()[0];
                    final var accountBill = new AccountBill();
                    accountBill.setAccount(defaultAccount);
                    accountBill.setBill(bill);
                    accountBill.setPercentage(null);
                    accountBill.setStatus(InvitationStatusEnum.PENDING);
                    final var accounts = new HashSet<>(billInput.getAccounts());
                    accounts.add(accountBill);
                    billInput.setAccounts(accounts);
                    return billInput;
                }
        );

        when(accountService.getAccounts(any())).thenReturn(new ArrayList<>());
        when(billService.getBill(any())).thenReturn(bill);

        final var accountPercentageSplit = BigDecimal.valueOf(50);
        final var billSplitDTO = BillSplitDTOFixture.getDefault();
        billSplitDTO.setId(existentBillId);

        when(billMapper.toBillSplitDTO(any())).thenReturn(billSplitDTO);
        when(itemMapper.toItemPercentageSplitDTO(any(Item.class))).thenAnswer(
                i -> {
                    final Item itemInput = (Item) i.getArguments()[0];
                    final ItemPercentageSplitDTO itemDTO = new ItemPercentageSplitDTO();
                    itemDTO.setItemId(itemInput.getId());
                    itemDTO.setName(itemInput.getName());
                    itemDTO.setCost(itemInput.getCost());
                    itemDTO.setPercentage(accountPercentageSplit);
                    return itemDTO;
                }
        );
        when(accountMapper.toDTO(any(Account.class))).thenAnswer(
                a -> {
                    final Account acc = (Account) a.getArguments()[0];
                    final AccountDTO accountDTO = new AccountDTO();
                    accountDTO.setId(acc.getId());
                    accountDTO.setEmail(acc.getEmail());
                    return accountDTO;
                }
        );
        final var pendingRegisteredBillSplitDTOFixture = BillSplitDTOFixture.getMappedPendingBillSplitDTOFixture();
        pendingRegisteredBillSplitDTOFixture.setId(existentBillId);
        when(billMapper.toBillSplitDTO(any())).thenReturn(pendingRegisteredBillSplitDTOFixture);

        //When
        final var pendingRegisteredBillSplitDTO = billFacade.inviteRegisteredToBill(existentBillId, inviteRegisteredResource.getAccounts());

        //Then
        verifyBillSplitDTOToBill(pendingRegisteredBillSplitDTO, bill);
    }

    @Test
    @DisplayName("Should return BillSplitDTO in getDetailedBill where user is Bill Creator")
    void shouldReturnBillSplitDTOInGetDetailedBillWithBillCreator() {
        //Given user that is bill's creator
        final var billSplitDTOFixture = BillSplitDTOFixture.getDefault();
        final var bill = BillEntityFixture.getMappedBillSplitDTOFixture();
        bill.getTaxes().clear();
        final var billId = bill.getId();
        final var accountPercentageSplit = BigDecimal.valueOf(50);

        when(billService.getBill(any())).thenReturn(bill);
        when(billMapper.toBillSplitDTO(any())).thenReturn(billSplitDTOFixture);
        when(itemMapper.toItemPercentageSplitDTO(any(Item.class))).thenAnswer(
                i -> {
                    final Item itemInput = (Item) i.getArguments()[0];
                    final ItemPercentageSplitDTO itemDTO = new ItemPercentageSplitDTO();
                    itemDTO.setItemId(itemInput.getId());
                    itemDTO.setName(itemInput.getName());
                    itemDTO.setCost(itemInput.getCost());
                    itemDTO.setPercentage(accountPercentageSplit);
                    return itemDTO;
                }
        );

        //When
        final var billSplitDTO = billFacade.getDetailedBill(billId);

        //Then
        verifyBillSplitDTOToBill(billSplitDTO, bill);
    }

    @Test
    @DisplayName("Should return BillSplitDTO in getDetailedBill where user is in Bill's accounts")
    void shouldReturnBillSplitDTOInGetDetailedBillUserInBillAccounts() {
        //Given user is in bill's accounts
        final var billSplitDTOFixture = BillSplitDTOFixture.getDefault();
        final var bill = BillEntityFixture.getMappedBillSplitDTOFixture();
        bill.getTaxes().clear();
        final var billId = bill.getId();
        final var accountPercentageSplit = BigDecimal.valueOf(50);

        when(billService.getBill(any())).thenReturn(bill);
        when(billMapper.toBillSplitDTO(any())).thenReturn(billSplitDTOFixture);
        when(itemMapper.toItemPercentageSplitDTO(any(Item.class))).thenAnswer(
                i -> {
                    final Item itemInput = (Item) i.getArguments()[0];
                    final ItemPercentageSplitDTO itemDTO = new ItemPercentageSplitDTO();
                    itemDTO.setItemId(itemInput.getId());
                    itemDTO.setName(itemInput.getName());
                    itemDTO.setCost(itemInput.getCost());
                    itemDTO.setPercentage(accountPercentageSplit);
                    return itemDTO;
                }
        );

        //When
        final var billSplitDTO = billFacade.getDetailedBill(billId);

        //Then
        verifyBillSplitDTOToBill(billSplitDTO, bill);
    }

    private void verifyBillSplitDTOToBill(BillSplitDTO billSplitDTO, Bill bill) {
        final Account billCreatorAccount = bill.getAccounts().stream().map(AccountBill::getAccount)
                .filter(acc -> acc.equals(bill.getCreator()))
                .iterator().next();
        assertThat(billSplitDTO.getCreator().getId()).isEqualTo(billCreatorAccount.getId());
        assertThat(billSplitDTO.getResponsible().getId()).isEqualTo(billCreatorAccount.getId());
        assertThat(bill.getStatus()).isEqualTo(BillStatusEnum.OPEN);
        assertThat(billSplitDTO.getId()).isEqualTo(bill.getId());
        assertThat(billSplitDTO.getName()).isEqualTo(bill.getName());
        assertThat(billSplitDTO.getStatus()).isEqualTo(bill.getStatus());
        assertThat(billSplitDTO.getCategory()).isEqualTo(bill.getCategory());
        assertThat(billSplitDTO.getCompany()).isEqualTo(bill.getCompany());
        assertThat(bill.getTaxes().size()).isEqualTo(billSplitDTO.getTaxes().size());

        final List<ItemAssociationSplitDTO> itemsPerAccount = billSplitDTO.getInformationPerAccount();
        final Set<AccountBill> accounts = bill.getAccounts();
        assertThat(itemsPerAccount.size()).isEqualTo(accounts.size());

    }

}