package proj.kedabra.billsnap.business.facade.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.dto.AssociateBillDTO;
import proj.kedabra.billsnap.business.dto.BillCompleteDTO;
import proj.kedabra.billsnap.business.dto.BillSplitDTO;
import proj.kedabra.billsnap.business.dto.ItemAssociationDTO;
import proj.kedabra.billsnap.business.dto.ItemAssociationSplitDTO;
import proj.kedabra.billsnap.business.dto.ItemDTO;
import proj.kedabra.billsnap.business.dto.ItemPercentageSplitDTO;
import proj.kedabra.billsnap.business.dto.PendingRegisteredBillSplitDTO;
import proj.kedabra.billsnap.business.exception.AccessForbiddenException;
import proj.kedabra.billsnap.business.exception.FunctionalWorkflowException;
import proj.kedabra.billsnap.business.facade.BillFacade;
import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.model.entities.AccountBill;
import proj.kedabra.billsnap.business.model.entities.Bill;
import proj.kedabra.billsnap.business.model.entities.Item;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.business.repository.BillRepository;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.InvitationStatusEnum;
import proj.kedabra.billsnap.fixtures.AssociateBillDTOFixture;
import proj.kedabra.billsnap.fixtures.BillDTOFixture;
import proj.kedabra.billsnap.fixtures.InviteRegisteredResourceFixture;
import proj.kedabra.billsnap.fixtures.ItemPercentageDTOFixture;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;
import proj.kedabra.billsnap.utils.SpringProfiles;
import proj.kedabra.billsnap.utils.tuples.AccountStatusPair;


@Tag("integration")
@ActiveProfiles(SpringProfiles.TEST)
@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class BillFacadeImplIT {

    @Autowired
    private BillFacade billFacade;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BillRepository billRepository;

    private static final String ITEM_PERCENTAGES_MUST_ADD_TO_100 = "The percentage split for this item must add up to 100: {%s, Percentage: %s}";

    @Test
    @DisplayName("Should return an exception if the account does not exist")
    void shouldReturnExceptionIfAccountDoesNotExist() {
        // Given
        final var billDTO = BillDTOFixture.getDefault();
        final String testEmail = "abc@123.ca";

        // When/Then
        final ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class,
                () -> billFacade.addPersonalBill(testEmail, billDTO));
        assertEquals("Account does not exist", resourceNotFoundException.getMessage());
    }

    @Test
    @DisplayName("Should return exception if list of emails contains one or more emails that do not exist")
    void ShouldReturnExceptionIfEmailInListOfEmailsDoesNotExist() {
        //Given a bill creator with existing email, but billDTO containing non-existent email in array of emails
        final var billDTO = BillDTOFixture.getDefault();
        final String existentEmail = "userdetails@service.com";
        final String anotherExistentEmail = "test@email.com";
        final String nonExistentEmail = "nonexistent@email.com";
        billDTO.setAccountsList(List.of(nonExistentEmail, anotherExistentEmail));

        //When/Then
        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() -> billFacade.addPersonalBill(existentEmail, billDTO))
                .withMessage("One or more accounts in the list of accounts does not exist: [%s]", nonExistentEmail);
    }

    @Test
    @DisplayName("Should return exception if list of emails contains bill creator email")
    void ShouldReturnExceptionIfBillCreatorIsInListOfEmails() {
        //Given
        final var billDTO = BillDTOFixture.getDefault();
        final String billCreator = "userdetails@service.com";
        final String anotherExistentEmail = "test@email.com";
        billDTO.setAccountsList(List.of(billCreator, anotherExistentEmail));

        //When/Then
        assertThatIllegalArgumentException().isThrownBy(() -> billFacade.addPersonalBill(billCreator, billDTO))
                .withMessage("List of emails cannot contain bill creator email");
    }

    @Test
    @DisplayName("Should save bill in database")
    void shouldSaveBillToUserInDatabase() {
        // Given
        final var billDTO = BillDTOFixture.getDefault();
        final String testEmail = "test@email.com";

        // When
        final BillCompleteDTO returnBillDTO = billFacade.addPersonalBill(testEmail, billDTO);

        // Then
        final var bill = billRepository.findById(returnBillDTO.getId()).orElseThrow();

        verifyBillDTOToBill(returnBillDTO, bill);
    }

    @Test
    @DisplayName("Should save bill with non-empty accountsList in database")
    void shouldSaveBillWithAccountsListInDatabase() {
        // Given
        final var billDTO = BillDTOFixture.getDefault();
        final String testEmail = "test@email.com";
        final String existentEmail = "userdetails@service.com";
        billDTO.setAccountsList(List.of(existentEmail));

        // When
        final BillCompleteDTO returnBillDTO = billFacade.addPersonalBill(testEmail, billDTO);

        // Then
        final var bill = billRepository.findById(returnBillDTO.getId()).orElseThrow();

        verifyBillDTOToBill(returnBillDTO, bill);
        assertThat(returnBillDTO.getAccountsList()).isNotEmpty();
        final List<AccountStatusPair> inputtedAccountsList = returnBillDTO.getAccountsList().stream()
                .filter(pair -> pair.getAccount().getEmail().equals(existentEmail)).collect(Collectors.toList());
        assertThat(inputtedAccountsList.get(0).getStatus()).isEqualTo(InvitationStatusEnum.PENDING);
    }

    @Test
    @DisplayName("Should save bill to user with null AccountBill percentage in database")
    void shouldSaveBillNullPercentageToUserInDatabase() {

        // Given
        final var billDTO = BillDTOFixture.getDefault();
        final String testEmail = "test@email.com";

        // When
        final BillCompleteDTO returnBillDTO = billFacade.addPersonalBill(testEmail, billDTO);

        // Then
        final var account = accountRepository.getAccountByEmail(testEmail);

        final var bill = billRepository.findById(returnBillDTO.getId()).orElseThrow();
        final Set<AccountBill> accounts = bill.getAccounts();

        assertEquals(1, accounts.size());
        final AccountBill accountBill = accounts.iterator().next();
        assertNull(accountBill.getPercentage());
        assertEquals(account, accountBill.getAccount());


    }

    @Test
    @DisplayName("Should return an exception if the account does not exist in getAllBills")
    void shouldReturnExceptionIfAccountDoesNotExistInGetAllBills() {
        // Given
        final String testEmail = "abc@123.ca";

        // When/Then
        final ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class,
                () -> billFacade.getAllBillsByEmail(testEmail));
        assertEquals("Account does not exist", resourceNotFoundException.getMessage());
    }

    @Test
    @DisplayName("Should return two mapped BillSplitDTO in getAllBills")
    void shouldReturn2BillSplitDTOInGetAllBills() {
        //Given
        final var testEmail = "test@email.com";
        final var billIdsToCompare = new HashSet<Long>();
        billIdsToCompare.add(1000L);
        billIdsToCompare.add(1001L);

        //When
        final List<BillSplitDTO> allBillsByEmail = billFacade.getAllBillsByEmail(testEmail);

        //Then
        assertEquals(2, allBillsByEmail.size());
        final Iterable<Bill> billsByTwoIds = billRepository.findAllById(billIdsToCompare);
        final List<Bill> billsListByTwoIdsList = StreamSupport.stream(billsByTwoIds.spliterator(), false)
                .collect(Collectors.toList());

        if (allBillsByEmail.get(0).getId().equals(1000L)) {
            verifyBillSplitDTOToBill(allBillsByEmail.get(0), billsListByTwoIdsList.get(0), null);
            verifyBillSplitDTOToBill(allBillsByEmail.get(1), billsListByTwoIdsList.get(1), null);
        } else if (allBillsByEmail.get(0).getId().equals(1001L)) {
            verifyBillSplitDTOToBill(allBillsByEmail.get(1), billsListByTwoIdsList.get(0), null);
            verifyBillSplitDTOToBill(allBillsByEmail.get(0), billsListByTwoIdsList.get(1), null);
        }
    }

    @Test
    @DisplayName("Should return emptyList in getAllBills")
    void shouldReturnEmptyList() {
        //Given existent user with 0 bills
        final var testEmail = "user@user.com";

        //When
        final List<BillSplitDTO> allBillsByEmail = billFacade.getAllBillsByEmail(testEmail);

        //Then
        assertEquals(0, allBillsByEmail.size());
    }

    @Test
    @DisplayName("Should throw exception if bill items percentage split does not add up to hundred")
    void shouldThrowExceptionIfItemPercentagesDoNotAddToHundred() {
        //Given
        final var dto = AssociateBillDTOFixture.getDefault();
        final var bill = billRepository.findById(dto.getId()).orElseThrow();
        final var item = bill.getItems().iterator().next();
        dto.getItems().get(0).getItems().get(0).setPercentage(new BigDecimal(10));

        //When/Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> billFacade.associateAccountsToBill(dto))
                .withMessage(String.format(ITEM_PERCENTAGES_MUST_ADD_TO_100, item.getName(), BigDecimal.valueOf(10)));
    }

    @ParameterizedTest
    @EnumSource(value = BillStatusEnum.class, names = {"IN_PROGRESS", "RESOLVED"})
    @DisplayName("Should throw exception if bill is not status open in Associate Bill")
    void shouldThrowExceptionIfBillIsNotStatusOpenAssociateBill(BillStatusEnum status) {
        //Given
        final var dto = AssociateBillDTOFixture.getDefault();
        final var bill = billRepository.findById(dto.getId()).orElseThrow();
        bill.setStatus(status);

        //When/Then
        assertThatExceptionOfType(FunctionalWorkflowException.class)
                .isThrownBy(() -> billFacade.associateAccountsToBill(dto))
                .withMessage(ErrorMessageEnum.BILL_IS_NOT_OPEN.getMessage());
    }

    @Test
    @DisplayName("Should throw exception if associated items contain a declined email")
    void shouldThrowExceptionForDeclinedEmailAssociation() {
        //given
        final var associateBillDTO = new AssociateBillDTO();
        associateBillDTO.setId(2000L);
        final var itemAssociationDTO = new ItemAssociationDTO();
        final var email = "user@withADeclinedBill.com";
        itemAssociationDTO.setEmail(email);
        itemAssociationDTO.setItems(List.of(ItemPercentageDTOFixture.getDefaultWithId(1012L)));
        associateBillDTO.setItems(List.of(itemAssociationDTO));

        //When/Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> billFacade.associateAccountsToBill(associateBillDTO))
                .withMessage(ErrorMessageEnum.LIST_ACCOUNT_DECLINED.getMessage(List.of(email).toString()));

    }

    @Test
    @DisplayName("Should return BillSplitDTO with each account's total items cost sum and mapped to input Bill")
    void shouldReturnBillSplitDTOWithAccountItemsCostSum() {
        //Given
        final var dto = AssociateBillDTOFixture.getDefault();
        final var bill = billRepository.findById(dto.getId()).orElseThrow();
        final var item = bill.getItems().iterator().next();

        //When
        final BillSplitDTO returnBillSplitDTO = billFacade.associateAccountsToBill(dto);

        //Then
        verifyBillSplitDTOToBill(returnBillSplitDTO, bill, null);

        assertThat(returnBillSplitDTO.getTotalTip()).isEqualTo(bill.getTipAmount());
        assertThat(returnBillSplitDTO.getItemsPerAccount().get(0).getCost()).isEqualTo(item.getCost());
    }

    @Test
    @DisplayName("Should return error if User requesting POST bills/{billId}/accounts is not the Bill responsible")
    void shouldReturnErrorIfUserMakingRequestIsNotBillResponsible() {
        //Given
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        final var billId = 1000L;
        final var notBillResponsible = "nobills@inthisemail.com";

        //When/Then
        assertThatExceptionOfType(AccessForbiddenException.class)
                .isThrownBy(() -> billFacade.inviteRegisteredToBill(billId, notBillResponsible, inviteRegisteredResource.getAccounts()))
                .withMessage(ErrorMessageEnum.USER_IS_NOT_BILL_RESPONSIBLE.getMessage());
    }

    @Test
    @DisplayName("Should throw error if bill does not exist in Invite Registered call")
    void shouldThrowErrorIfBillDoesNotExistInInviteRegistered() {
        //Given
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        final var principal = "test@email.com";
        final var accountNotInBill = "nobills@inthisemail.com";
        final var nonExistentBillId = 90019001L;
        inviteRegisteredResource.setAccounts(List.of(accountNotInBill));

        //When/Then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> billFacade.inviteRegisteredToBill(nonExistentBillId, principal, inviteRegisteredResource.getAccounts()))
                .withMessage(ErrorMessageEnum.BILL_ID_DOES_NOT_EXIST.getMessage(String.valueOf(nonExistentBillId)));
    }

    @Test
    @DisplayName("Should throw error if one account does not exist in Invite Registered call")
    void shouldThrowErrorIfOneAccountDoesNotExistInInviteRegistered() {
        //Given
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        final var billResponsible = "test@email.com";
        final var accountNotInBill = "nobills@inthisemail.com";
        final var nonExistentEmail = "clearly@nonexistent.gov";
        final var existentBillId = 1000L;
        inviteRegisteredResource.setAccounts(List.of(accountNotInBill, nonExistentEmail));

        //When/Then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> billFacade.inviteRegisteredToBill(existentBillId, billResponsible, inviteRegisteredResource.getAccounts()))
                .withMessage(ErrorMessageEnum.LIST_ACCOUNT_DOES_NOT_EXIST.getMessage(List.of(nonExistentEmail).toString()));
    }

    @Test
    @DisplayName("Should throw error if many accounts do not exist in Invite Registered call")
    void shouldThrowErrorIfManyAccountDoNotExistInInviteRegistered() {
        //Given
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        final var billResponsible = "test@email.com";
        final var accountNotInBill = "nobills@inthisemail.com";
        final var nonExistentEmail = "clearly@nonexistent.gov";
        final var secondNonExistentEmail = "veryfake@fake.ca";
        final var existentBillId = 1000L;
        inviteRegisteredResource.setAccounts(List.of(accountNotInBill, nonExistentEmail, secondNonExistentEmail));

        //When/Then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> billFacade.inviteRegisteredToBill(existentBillId, billResponsible, inviteRegisteredResource.getAccounts()))
                .withMessage(ErrorMessageEnum.LIST_ACCOUNT_DOES_NOT_EXIST.getMessage(List.of(nonExistentEmail, secondNonExistentEmail).toString()));
    }

    @Test
    @DisplayName("Should throw error if one account is already part of bill in Invite Registered call")
    void shouldThrowErrorIfOneAccountIsAlreadyPartOfBillInInviteRegistered() {
        //Given
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        final var billResponsible = "user@withABill.com";
        final var accountInBill = "user@hasbills.com";
        final var accountNotInBill = "nobills@inthisemail.com";
        final var existentBillId = 1005L;
        inviteRegisteredResource.setAccounts(List.of(accountInBill, accountNotInBill));

        //When/Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> billFacade.inviteRegisteredToBill(existentBillId, billResponsible, inviteRegisteredResource.getAccounts()))
                .withMessage(ErrorMessageEnum.LIST_ACCOUNT_ALREADY_IN_BILL.getMessage(List.of(accountInBill).toString()));
    }

    @Test
    @DisplayName("Should throw error if many accounts are already part of bill in Invite Registered call")
    void shouldThrowErrorIfManyAccountsAreAlreadyPartOfBillInInviteRegistered() {
        //Given
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        final var billResponsible = "user@withABill.com";
        final var accountInBill = "user@hasbills.com";
        final var accountNotInBill = "nobills@inthisemail.com";
        final var existentBillId = 1005L;
        inviteRegisteredResource.setAccounts(List.of(billResponsible, accountInBill, accountNotInBill));

        //When/Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> billFacade.inviteRegisteredToBill(existentBillId, billResponsible, inviteRegisteredResource.getAccounts()))
                .withMessageContaining(billResponsible).withMessageContaining(accountInBill);
    }

    @Test
    @DisplayName("Should return mapped PendingRegisteredBillSplitDTO when Invite Registered Call with one new User")
    void shouldReturnMappedPendingRegisteredBillSplitDTOInInviteRegistered() {
        //Given
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        final var billResponsible = "test@email.com";
        final var accountNotInBill = "nobills@inthisemail.com";
        final var existentBillId = 1000L;
        final List<String> accountsList = List.of(accountNotInBill);
        inviteRegisteredResource.setAccounts(accountsList);

        //When
        final var pendingRegisteredBillSplitDTO = billFacade.inviteRegisteredToBill(existentBillId, billResponsible, inviteRegisteredResource.getAccounts());

        //Then
        final var bill = billRepository.getBillById(existentBillId);
        verifyBillSplitDTOToBill(null, bill, pendingRegisteredBillSplitDTO);
        final List<String> dtoPendingAccounts = pendingRegisteredBillSplitDTO.getPendingAccounts();
        assertThat(dtoPendingAccounts.size()).isEqualTo(1);
        assertThat(dtoPendingAccounts.containsAll(accountsList)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = BillStatusEnum.class, names = {"IN_PROGRESS", "RESOLVED"})
    @DisplayName("Should throw exception if bill is not status open in Invite Registered To Bill")
    void shouldThrowExceptionIfBillIsNotStatusOpenInviteRegistered(BillStatusEnum status) {
        //Given
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        final var billResponsible = "test@email.com";
        final var existentBillId = 1000L;
        final var bill = billRepository.findById(existentBillId).orElseThrow();
        bill.setStatus(status);

        //When/Then
        assertThatExceptionOfType(FunctionalWorkflowException.class)
                .isThrownBy(() -> billFacade.inviteRegisteredToBill(existentBillId, billResponsible, inviteRegisteredResource.getAccounts()))
                .withMessage(ErrorMessageEnum.BILL_IS_NOT_OPEN.getMessage());
    }

    @Test
    @DisplayName("Should return BillSplitDTO in getDetailedBill where user is Bill Creator")
    void shouldReturnBillSplitDTOInGetDetailedBillWithBillCreator() {
        //Given user that is bill's creator
        final var billId = 1000L;
        final var userEmail = "test@email.com";

        //When
        final var billSplitDTO = billFacade.getDetailedBill(billId, userEmail);

        //Then
        final var bill = billRepository.getBillById(billId);
        verifyBillSplitDTOToBill(billSplitDTO, bill, null);
    }

    @Test
    @DisplayName("Should return BillSplitDTO in getDetailedBill where user is in Bill's accounts")
    void shouldReturnBillSplitDTOInGetDetailedBillUserInBillAccounts() {
        //Given user is in bill's accounts
        final var billId = 1100L;
        final var userEmail = "userdetails@service.com";

        //When
        final var billSplitDTO = billFacade.getDetailedBill(billId, userEmail);

        //Then
        final var bill = billRepository.getBillById(billId);
        verifyBillSplitDTOToBill(billSplitDTO, bill, null);
    }

    @Test
    @DisplayName("Should throw Exception in getDetailedBill if user not part of bill")
    void shouldReturnExceptionIfUserNotPartOfBill() {
        //Given
        final var billId = 1000L;
        final var userEmail = "nonexistent@user.com";

        //When/Then
        assertThatExceptionOfType(AccessForbiddenException.class)
                .isThrownBy(() -> billFacade.getDetailedBill(billId, userEmail))
                .withMessage(ErrorMessageEnum.ACCOUNT_IS_NOT_ASSOCIATED_TO_BILL.getMessage());
    }

    @Test
    @DisplayName("Should return BillSplitDTO with status IN_PROGRESS when Start Bill")
    void ShouldReturnBillSplitDTOWhenStartBill() {
        //Given
        final var billId = 1100L;
        final var userEmail = "user@hasbills.com";

        //When
        final BillSplitDTO billSplitDTO = billFacade.startBill(billId, userEmail);

        //Then
        assertThat(billSplitDTO.getStatus()).isEqualTo(BillStatusEnum.IN_PROGRESS);
    }

    @Test
    @DisplayName("Should throw exception if Bill is Resolved and not Open in startBill call")
    void shouldThrowExceptionIfBillIsResolvedNotOpenInStartBill() {
        //Given
        final var billId = 1001L;
        final var userEmail = "test@email.com";

        //When/Then
        assertThatExceptionOfType(FunctionalWorkflowException.class).isThrownBy(() -> billFacade.startBill(billId, userEmail))
                .withMessage(ErrorMessageEnum.BILL_IS_NOT_OPEN.getMessage());
    }

    @Test
    @DisplayName("Should throw exception if Bill is In Progress and not Open in startBill call")
    void shouldThrowExceptionIfBillIsInProgressNotOpenInStartBill() {
        //Given
        final var billId = 1101L;
        final var userEmail = "user@hasbills.com";

        //When/Then
        assertThatExceptionOfType(FunctionalWorkflowException.class).isThrownBy(() -> billFacade.startBill(billId, userEmail))
                .withMessage(ErrorMessageEnum.BILL_IS_NOT_OPEN.getMessage());
    }

    @Test
    @DisplayName("Should throw exception if the given User email is not the Bill Responsible in startBill call")
    void shouldThrowExceptionIfGivenEmailIsNotBillResponsibleInStartBill() {
        //Given
        final var billId = 1100L;
        final String notBillResponsible = "notbillresponsible@email.com";

        //When/Then
        assertThatExceptionOfType(AccessForbiddenException.class)
                .isThrownBy(() -> billFacade.startBill(billId, notBillResponsible))
                .withMessage(ErrorMessageEnum.USER_IS_NOT_BILL_RESPONSIBLE.getMessage());
    }

    private void verifyBillSplitDTOToBill(BillSplitDTO billSplitDTO, Bill bill, PendingRegisteredBillSplitDTO pendingRegisteredBillSplitDTO) {
        var dto = Optional.ofNullable(pendingRegisteredBillSplitDTO).isPresent() ? pendingRegisteredBillSplitDTO : billSplitDTO;

        final Account billCreatorAccount = bill.getAccounts().stream().map(AccountBill::getAccount)
                .filter(acc -> acc.equals(bill.getCreator()))
                .iterator().next();
        assertThat(dto.getCreator().getId()).isEqualTo(billCreatorAccount.getId());
        assertThat(dto.getResponsible().getId()).isEqualTo(billCreatorAccount.getId());
        assertThat(dto.getStatus()).isEqualTo(bill.getStatus());
        assertThat(dto.getId()).isEqualTo(bill.getId());
        assertThat(dto.getName()).isEqualTo(bill.getName());
        assertThat(dto.getStatus()).isEqualTo(bill.getStatus());
        assertThat(dto.getCategory()).isEqualTo(bill.getCategory());
        assertThat(dto.getCompany()).isEqualTo(bill.getCompany());
        assertThat(dto.getUpdated()).isCloseTo(bill.getUpdated(), within(200, ChronoUnit.MILLIS));
        assertThat(dto.getCreated()).isCloseTo(bill.getCreated(), within(200, ChronoUnit.MILLIS));

        final List<ItemAssociationSplitDTO> itemsPerAccount = dto.getItemsPerAccount();
        final Set<AccountBill> accounts = bill.getAccounts();
        assertThat(itemsPerAccount.size()).isEqualTo(accounts.size());

        if (!bill.getItems().isEmpty()) {
            //for the time being we verify a bill with only 1 item. Should be generic when needed.
            final Item item = bill.getItems().iterator().next();
            final ItemPercentageSplitDTO returnItemPercentageSplitDTO = itemsPerAccount.get(0).getItems().get(0);

            assertThat(returnItemPercentageSplitDTO.getName()).isEqualTo(item.getName());
            assertThat(returnItemPercentageSplitDTO.getCost()).isEqualTo(item.getCost());
            assertThat(dto.getBalance()).isEqualTo(item.getCost().add(bill.getTipAmount()));
        } else {
            assertThat(BigDecimal.ZERO.compareTo(dto.getBalance())).isEqualTo(0);
        }

    }

    private void verifyBillDTOToBill(BillCompleteDTO returnBillDTO, Bill bill) {
        assertEquals(bill.getId(), returnBillDTO.getId());
        assertEquals(bill.getStatus(), returnBillDTO.getStatus());
        assertEquals(bill.getCreated(), returnBillDTO.getCreated());
        assertEquals(bill.getUpdated(), returnBillDTO.getUpdated());
        assertEquals(BillStatusEnum.OPEN, bill.getStatus());
        assertEquals(bill.getCategory(), returnBillDTO.getCategory());
        assertEquals(bill.getCompany(), returnBillDTO.getCompany());
        final List<ItemDTO> items = returnBillDTO.getItems();
        final Set<Item> billItems = bill.getItems();
        assertEquals(billItems.size(), items.size());
        assertEquals(returnBillDTO.getAccountsList().size(), bill.getAccounts().size());

        if (!items.isEmpty()) {
            //for the time being we verify only 1 item. Should be generic when needed.
            final ItemDTO returnItemDTO = items.get(0);
            final Item item = billItems.iterator().next();
            assertEquals(item.getName(), returnItemDTO.getName());
            assertEquals(item.getCost(), returnItemDTO.getCost());
            assertEquals(bill.getName(), returnBillDTO.getName());
            assertEquals(item.getCost(), returnBillDTO.getBalance());
        } else {
            assertEquals(0, BigDecimal.ZERO.compareTo(returnBillDTO.getBalance()));
        }

        final Account account = bill.getAccounts().stream().map(AccountBill::getAccount)
                .filter(acc -> acc.equals(bill.getCreator()))
                .iterator().next();
        assertEquals(account.getId(), returnBillDTO.getCreator().getId());
        assertEquals(account.getId(), returnBillDTO.getResponsible().getId());
    }


}