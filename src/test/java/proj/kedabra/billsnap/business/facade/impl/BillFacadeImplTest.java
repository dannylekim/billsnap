package proj.kedabra.billsnap.business.facade.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;

import proj.kedabra.billsnap.business.dto.AccountDTO;
import proj.kedabra.billsnap.business.dto.BillCompleteDTO;
import proj.kedabra.billsnap.business.dto.BillDTO;
import proj.kedabra.billsnap.business.dto.BillSplitDTO;
import proj.kedabra.billsnap.business.dto.ItemAssociationSplitDTO;
import proj.kedabra.billsnap.business.dto.ItemPercentageSplitDTO;
import proj.kedabra.billsnap.business.dto.PendingRegisteredBillSplitDTO;
import proj.kedabra.billsnap.business.exception.AccessForbiddenException;
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
import proj.kedabra.billsnap.fixtures.PendingRegisteredBillSplitDTOFixture;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;

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
    private AccountService accountService;

    @Mock
    private BillService billService;

    private static final BigDecimal PERCENTAGE_DIVISOR = BigDecimal.valueOf(100);

    private static final String ITEM_PERCENTAGES_MUST_ADD_TO_100 = "The percentage split for this item must add up to 100: {%s, Percentage: %s}";

    @BeforeEach
    void setup() {

        MockitoAnnotations.initMocks(this);
        billFacade = new BillFacadeImpl(billService, accountService, billMapper, accountMapper, itemMapper);

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
        when(accountService.getAccount(nonExistentEmail)).thenThrow(new ResourceNotFoundException(ErrorMessageEnum.ACCOUNT_DOES_NOT_EXIST.getMessage()));

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
        billSplitDTO.setItemsPerAccount(null);

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
        final var item = bill.getItems().iterator().next();
        final var accountPercentageSplit = BigDecimal.valueOf(50);
        final var billSplitDTO = BillSplitDTOFixture.getDefault();
        billSplitDTO.setItemsPerAccount(null);

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
        verifyBillSplitDTOToBill(returnBillSplitDTO, bill, null);

        assertThat(returnBillSplitDTO.getTotalTip()).isEqualTo(bill.getTipAmount());
        assertThat(returnBillSplitDTO.getItemsPerAccount().get(0).getCost())
                .isEqualTo(item.getCost().multiply(accountPercentageSplit.divide(PERCENTAGE_DIVISOR)));
    }

    @Test
    @DisplayName("Should throw error if billId references non-existent bill in Invite Registered call")
    void shouldThrowErrorIfNonExistentBillIdInInviteRegistered() {
        //Given
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        final var principal = "test@email.com";
        final var accountNotInBill = "nobills@inthisemail.com";
        final var nonExistentBillId = 90019001L;
        inviteRegisteredResource.setAccounts(List.of(accountNotInBill));

        when(billService.getBill(any())).thenThrow(new ResourceNotFoundException(ErrorMessageEnum.BILL_DOES_NOT_EXIST.getMessage()));

        //When/Then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> billFacade.inviteRegisteredToBill(nonExistentBillId, principal, inviteRegisteredResource.getAccounts()))
                .withMessage(ErrorMessageEnum.BILL_DOES_NOT_EXIST.getMessage());
    }

    @Test
    @DisplayName("Should return error if User requesting POST bills/{billId}/accounts is not the Bill responsible")
    void shouldReturnErrorIfUserMakingRequestIsNotBillResponsible() {
        //Given
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        final var billId = 1000L;
        final var notBillResponsible = "nobills@inthisemail.com";
        final var bill = BillEntityFixture.getDefault();
        final var billResponsible = "bill@responsible.com";
        final var principal = AccountEntityFixture.getDefaultAccount();
        principal.setEmail(billResponsible);
        bill.setResponsible(principal);
        bill.setId(billId);

        when(billService.getBill(any())).thenReturn(bill);

        //When/Then
        assertThatExceptionOfType(AccessForbiddenException.class)
                .isThrownBy(() -> billFacade.inviteRegisteredToBill(billId, notBillResponsible, inviteRegisteredResource.getAccounts()))
                .withMessage(ErrorMessageEnum.USER_IS_NOT_BILL_RESPONSIBLE.getMessage());
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
                .isThrownBy(() -> billFacade.inviteRegisteredToBill(existentBillId, billResponsible, inviteRegisteredResource.getAccounts()))
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
                .isThrownBy(() -> billFacade.inviteRegisteredToBill(existentBillId, billResponsible, inviteRegisteredResource.getAccounts()))
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
                    final var accounts = new HashSet<AccountBill>(billInput.getAccounts());
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
        final var pendingRegisteredBillSplitDTOFixture = PendingRegisteredBillSplitDTOFixture.getMappedBillSplitDTOFixture();
        pendingRegisteredBillSplitDTOFixture.setId(existentBillId);
        when(billMapper.toPendingRegisteredBillSplitDTO(any())).thenReturn(pendingRegisteredBillSplitDTOFixture);

        //When
        final var pendingRegisteredBillSplitDTO = billFacade.inviteRegisteredToBill(existentBillId, billResponsible, inviteRegisteredResource.getAccounts());

        //Then
        verifyBillSplitDTOToBill(null, bill, pendingRegisteredBillSplitDTO);
        final List<String> dtoPendingAccounts = pendingRegisteredBillSplitDTO.getPendingAccounts();
        assertThat(dtoPendingAccounts.size()).isEqualTo(1);
        assertThat(dtoPendingAccounts.containsAll(invitedAccountsList)).isTrue();
    }

    @Test
    @DisplayName("Should return BillSplitDTO in getDetailedBill where user is Bill Creator")
    void shouldReturnBillSplitDTOInGetDetailedBillWithBillCreator() {
        //Given user that is bill's creator
        final var billSplitDTOFixture = BillSplitDTOFixture.getDefault();
        final var bill = BillEntityFixture.getMappedBillSplitDTOFixture();
        final var billId = bill.getId();
        final var userEmail = "accountentity@test.com";
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
        final var billSplitDTO = billFacade.getDetailedBill(billId, userEmail);

        //Then
        verifyBillSplitDTOToBill(billSplitDTO, bill, null);
    }

    @Test
    @DisplayName("Should return BillSplitDTO in getDetailedBill where user is in Bill's accounts")
    void shouldReturnBillSplitDTOInGetDetailedBillUserInBillAccounts() {
        //Given user is in bill's accounts
        final var billSplitDTOFixture = BillSplitDTOFixture.getDefault();
        final var bill = BillEntityFixture.getMappedBillSplitDTOFixture();
        final var billId = bill.getId();
        final var userEmail = "hellomotto@cell.com";
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
        final var billSplitDTO = billFacade.getDetailedBill(billId, userEmail);

        //Then
        verifyBillSplitDTOToBill(billSplitDTO, bill, null);
    }

    @Test
    @DisplayName("Should throw Exception in getDetailedBill if user not part of bill")
    void shouldReturnExceptionIfUserNotPartOfBill() {
        //Given
        final var bill = BillEntityFixture.getMappedBillSplitDTOFixture();
        final var billId = 1000L;
        bill.setId(1000L);
        final var userEmail = "nonexistent@email.com";

        when(billService.getBill(any())).thenReturn(bill);

        //When/Then
        assertThatExceptionOfType(AccessForbiddenException.class)
                .isThrownBy(() -> billFacade.getDetailedBill(billId, userEmail))
                .withMessage(ErrorMessageEnum.ACCOUNT_IS_NOT_ASSOCIATED_TO_BILL.getMessage());
    }

    private void verifyBillSplitDTOToBill(BillSplitDTO billSplitDTO, Bill bill, PendingRegisteredBillSplitDTO pendingRegisteredBillSplitDTO) {
        var dto = Optional.ofNullable(pendingRegisteredBillSplitDTO).isPresent() ? pendingRegisteredBillSplitDTO : billSplitDTO;

        final Account billCreatorAccount = bill.getAccounts().stream().map(AccountBill::getAccount)
                .filter(acc -> acc.equals(bill.getCreator()))
                .iterator().next();
        assertThat(dto.getCreator().getId()).isEqualTo(billCreatorAccount.getId());
        assertThat(dto.getResponsible().getId()).isEqualTo(billCreatorAccount.getId());
        assertThat(bill.getStatus()).isEqualTo(BillStatusEnum.OPEN);
        assertThat(dto.getId()).isEqualTo(bill.getId());
        assertThat(dto.getName()).isEqualTo(bill.getName());
        assertThat(dto.getStatus()).isEqualTo(bill.getStatus());
        assertThat(dto.getCategory()).isEqualTo(bill.getCategory());
        assertThat(dto.getCompany()).isEqualTo(bill.getCompany());
        assertThat(dto.getUpdated()).isCloseTo(bill.getUpdated(), within(500, ChronoUnit.MILLIS));
        assertThat(dto.getCreated()).isCloseTo(bill.getCreated(), within(500, ChronoUnit.MILLIS));

        final List<ItemAssociationSplitDTO> itemsPerAccount = dto.getItemsPerAccount();
        if (!(dto instanceof PendingRegisteredBillSplitDTO)) {
            final Set<AccountBill> accounts = bill.getAccounts();
            assertThat(itemsPerAccount.size()).isEqualTo(accounts.size());
        }
        //for the time being we verify a bill with only 1 item. Should be generic when needed.
        if (!bill.getItems().isEmpty()) {
            final Item item = bill.getItems().iterator().next();
            final ItemPercentageSplitDTO returnItemPercentageSplitDTO = itemsPerAccount.get(0).getItems().get(0);
            assertThat(returnItemPercentageSplitDTO.getName()).isEqualTo(item.getName());
            assertThat(returnItemPercentageSplitDTO.getCost()).isEqualTo(item.getCost());
            assertThat(dto.getBalance()).isEqualTo(item.getCost().add(bill.getTipAmount()));
        }

    }

}