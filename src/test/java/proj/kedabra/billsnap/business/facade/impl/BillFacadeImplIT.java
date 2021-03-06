package proj.kedabra.billsnap.business.facade.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.comparator.Comparators;

import proj.kedabra.billsnap.business.dto.AccountDTO;
import proj.kedabra.billsnap.business.dto.AssociateBillDTO;
import proj.kedabra.billsnap.business.dto.BillCompleteDTO;
import proj.kedabra.billsnap.business.dto.BillSplitDTO;
import proj.kedabra.billsnap.business.dto.ItemAssociationDTO;
import proj.kedabra.billsnap.business.dto.ItemAssociationSplitDTO;
import proj.kedabra.billsnap.business.dto.ItemDTO;
import proj.kedabra.billsnap.business.dto.ItemPercentageSplitDTO;
import proj.kedabra.billsnap.business.dto.TaxDTO;
import proj.kedabra.billsnap.business.exception.AccessForbiddenException;
import proj.kedabra.billsnap.business.exception.FunctionalWorkflowException;
import proj.kedabra.billsnap.business.exception.ResourceNotFoundException;
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
import proj.kedabra.billsnap.fixtures.BillEntityFixture;
import proj.kedabra.billsnap.fixtures.EditBillDTOFixture;
import proj.kedabra.billsnap.fixtures.GetBillPaginationDTOFixture;
import proj.kedabra.billsnap.fixtures.InviteRegisteredResourceFixture;
import proj.kedabra.billsnap.fixtures.ItemPercentageDTOFixture;
import proj.kedabra.billsnap.presentation.resources.OrderByEnum;
import proj.kedabra.billsnap.presentation.resources.SortByEnum;
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
        assertThat(returnBillDTO.getBalance()).isEqualByComparingTo(new BigDecimal("330.00"));
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
    @DisplayName("Should save bill with new taxes in database")
    void shouldSaveBillWithNewTaxesInDatabase() {

        // Given
        final var billDTO = BillDTOFixture.getDefault();
        final String testEmail = "test@email.com";

        // When
        final BillCompleteDTO returnBillDTO = billFacade.addPersonalBill(testEmail, billDTO);

        // Then
        final var taxDTO = billDTO.getTaxes().get(0);
        final var bill = billRepository.findById(returnBillDTO.getId()).orElseThrow();
        final var persistedTaxes = bill.getTaxes();

        assertThat(returnBillDTO.getTaxes()).hasSameSizeAs(billDTO.getTaxes());
        final var returnedTaxDTO = returnBillDTO.getTaxes().get(0);
        assertThat(returnedTaxDTO.getId()).isNotNull();
        assertThat(returnedTaxDTO.getPercentage()).isEqualByComparingTo(taxDTO.getPercentage());
        assertThat(returnedTaxDTO.getName()).isEqualTo(taxDTO.getName());

        assertThat(persistedTaxes).hasSameSizeAs(billDTO.getTaxes());
        final var persistedTax = persistedTaxes.iterator().next();
        assertThat(persistedTax.getId()).isNotNull();
        assertThat(persistedTax.getBill()).isEqualTo(bill);
        assertThat(persistedTax.getPercentage()).isEqualByComparingTo(taxDTO.getPercentage());
        assertThat(persistedTax.getName()).isEqualTo(taxDTO.getName());
    }

    @Test
    @DisplayName("Should return bill according to pagination when sorted by creation")
    void shouldReturnBillAccordingToPaginationWhenSortedByCreation() {
        //Given
        final var billPagination1 = GetBillPaginationDTOFixture.getDefault();

        final List<SortByEnum> sortByList = new ArrayList<>();
        sortByList.add(SortByEnum.CREATED);
        final var billPagination2 = GetBillPaginationDTOFixture.getCustom("restaurant", OrderByEnum.ASC, sortByList, 1, 2);
        final var billPagination3 = GetBillPaginationDTOFixture.getCustom("restaurant", OrderByEnum.DESC, sortByList, 0, 2);

        //When
        final List<BillSplitDTO> result1 = billFacade.getAllBillsByEmailPageable(billPagination1);
        final List<BillSplitDTO> result2 = billFacade.getAllBillsByEmailPageable(billPagination2);
        final List<BillSplitDTO> result3 = billFacade.getAllBillsByEmailPageable(billPagination3);

        //Then
        assertThat(result1).hasSize(2);
        assertThat(result1.get(0).getName()).isEqualTo("bill pagination 2");
        assertThat(result1.get(1).getName()).isEqualTo("bill pagination 3");

        assertThat(result2).hasSize(1);
        assertThat(result2.get(0).getName()).isEqualTo("bill pagination 4");

        assertThat(result3).hasSize(2);
        assertThat(result3.get(0).getName()).isEqualTo("bill pagination 4");
        assertThat(result3.get(1).getName()).isEqualTo("bill pagination 3");
    }

    @Test
    @DisplayName("Should return bill according to pagination when sorted by status")
    void shouldReturnBillAccordingToPaginationWhenSortedByStatus() {
        //Given
        final List<SortByEnum> sortByList = new ArrayList<>();
        sortByList.add(SortByEnum.STATUS);
        final var billPagination = GetBillPaginationDTOFixture.getCustom("restaurant" , OrderByEnum.DESC, sortByList, 0, 2);
        final var billPagination1 = GetBillPaginationDTOFixture.getCustom("restaurant" , OrderByEnum.ASC, sortByList, 0, 5);

        //When
        final List<BillSplitDTO> result = billFacade.getAllBillsByEmailPageable(billPagination);
        final List<BillSplitDTO> result1 = billFacade.getAllBillsByEmailPageable(billPagination1);

        //Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("bill pagination 4");

        assertThat(result1).hasSize(3);
        assertThat(result1.get(0).getName()).isEqualTo("bill pagination 3");
    }

    @Test
    @DisplayName("Should return bill according to pagination when sorted by category")
    void shouldReturnBillAccordingToPaginationWhenSortedByCategory() {
        //Given
        final List<SortByEnum> sortByList = new ArrayList<>();
        sortByList.add(SortByEnum.CATEGORY);
        final var billPagination = GetBillPaginationDTOFixture.getCustom(null, OrderByEnum.ASC, sortByList, 0, 2);

        //When
        final List<BillSplitDTO> result = billFacade.getAllBillsByEmailPageable(billPagination);

        //Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("bill pagination 5");
    }

    @Test
    @DisplayName("Should return bill according to pagination when sorted by name")
    void shouldReturnBillAccordingToPaginationWhenSortedByName() {
        //Given
        final List<SortByEnum> sortByList = new ArrayList<>();
        sortByList.add(SortByEnum.NAME);
        final var billPagination = GetBillPaginationDTOFixture.getCustom(null, OrderByEnum.ASC, sortByList, 0, 2);

        //When
        final List<BillSplitDTO> result = billFacade.getAllBillsByEmailPageable(billPagination);

        //Then
        assertThat(result).hasSize(2);
        final var billNames = result.stream().map(BillSplitDTO::getName).collect(Collectors.toList());
        final var sortedBillNames = new ArrayList<>(billNames);
        Collections.sort(sortedBillNames);
        assertThat(sortedBillNames).isEqualTo(billNames);
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
                .withMessage(ErrorMessageEnum.WRONG_BILL_STATUS.getMessage(BillStatusEnum.OPEN.toString()));
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

        final var existentBill = billRepository.getBillById(2000L);

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
        verifyBillSplitDTOToBill(returnBillSplitDTO, bill);

        assertThat(returnBillSplitDTO.getTotalTip()).isEqualTo(bill.getTipAmount());
        assertThat(returnBillSplitDTO.getInformationPerAccount().get(0).getSubTotal()).isEqualTo(item.getCost());
    }

    @Test
    @DisplayName("Should throw error if bill does not exist in Invite Registered call")
    void shouldThrowErrorIfBillDoesNotExistInInviteRegistered() {
        //Given
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        final var accountNotInBill = "nobills@inthisemail.com";
        final var nonExistentBillId = 90019001L;
        final var accounts = List.of(accountNotInBill);
        inviteRegisteredResource.setAccounts(accounts);

        //When/Then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> billFacade.inviteRegisteredToBill(nonExistentBillId, accounts))
                .withMessage(ErrorMessageEnum.BILL_ID_DOES_NOT_EXIST.getMessage(String.valueOf(nonExistentBillId)));
    }

    @Test
    @DisplayName("Should throw error if one account does not exist in Invite Registered call")
    void shouldThrowErrorIfOneAccountDoesNotExistInInviteRegistered() {
        //Given
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        final var accountNotInBill = "nobills@inthisemail.com";
        final var nonExistentEmail = "clearly@nonexistent.gov";
        final var existentBillId = 1000L;
        final var accounts = List.of(accountNotInBill, nonExistentEmail);
        inviteRegisteredResource.setAccounts(accounts);

        //When/Then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> billFacade.inviteRegisteredToBill(existentBillId, accounts))
                .withMessage(ErrorMessageEnum.LIST_ACCOUNT_DOES_NOT_EXIST.getMessage(List.of(nonExistentEmail).toString()));
    }

    @Test
    @DisplayName("Should throw error if many accounts do not exist in Invite Registered call")
    void shouldThrowErrorIfManyAccountDoNotExistInInviteRegistered() {
        //Given
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        final var accountNotInBill = "nobills@inthisemail.com";
        final var nonExistentEmail = "clearly@nonexistent.gov";
        final var secondNonExistentEmail = "veryfake@fake.ca";
        final var existentBillId = 1000L;
        final var accounts = List.of(accountNotInBill, nonExistentEmail, secondNonExistentEmail);
        inviteRegisteredResource.setAccounts(accounts);

        //When/Then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> billFacade.inviteRegisteredToBill(existentBillId, accounts))
                .withMessage(ErrorMessageEnum.LIST_ACCOUNT_DOES_NOT_EXIST.getMessage(List.of(nonExistentEmail, secondNonExistentEmail).toString()));
    }

    @Test
    @DisplayName("Should throw error if one account is already part of bill in Invite Registered call")
    void shouldThrowErrorIfOneAccountIsAlreadyPartOfBillInInviteRegistered() {
        //Given
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        final var accountInBill = "user@hasbills.com";
        final var accountNotInBill = "nobills@inthisemail.com";
        final var existentBillId = 1005L;
        final var accounts = List.of(accountInBill, accountNotInBill);
        inviteRegisteredResource.setAccounts(accounts);

        //When/Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> billFacade.inviteRegisteredToBill(existentBillId, accounts))
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
        final var accounts = List.of(billResponsible, accountInBill, accountNotInBill);
        inviteRegisteredResource.setAccounts(accounts);

        //When/Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> billFacade.inviteRegisteredToBill(existentBillId, accounts))
                .withMessageContaining(billResponsible).withMessageContaining(accountInBill);
    }

    @Test
    @DisplayName("Should return mapped BillSplitDTO when Invite Registered Call with one new User")
    void shouldReturnMappedBillSplitDTOInInviteRegisteredWithPendingAccounts() {
        //Given
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        final var accountNotInBill = "nobills@inthisemail.com";
        final var existentBillId = 1000L;
        final List<String> accountsList = List.of(accountNotInBill);
        inviteRegisteredResource.setAccounts(accountsList);

        //When
        final var billSplitDTO = billFacade.inviteRegisteredToBill(existentBillId, inviteRegisteredResource.getAccounts());

        //Then
        final var bill = billRepository.getBillById(existentBillId);
        verifyBillSplitDTOToBill(billSplitDTO, bill);
        final var informationPerAccount = billSplitDTO.getInformationPerAccount();
        assertThat(informationPerAccount.stream().map(ItemAssociationSplitDTO::getAccount).map(AccountDTO::getEmail).collect(Collectors.toList())).containsAll(accountsList);
        assertThat(informationPerAccount.stream().filter(info -> info.getAccount().getEmail().equals(accountNotInBill)).map(ItemAssociationSplitDTO::getInvitationStatus).collect(Collectors.toList())).containsExactly(InvitationStatusEnum.PENDING);
    }

    @ParameterizedTest
    @EnumSource(value = BillStatusEnum.class, names = {"IN_PROGRESS", "RESOLVED"})
    @DisplayName("Should throw exception if bill is not status open in Invite Registered To Bill")
    void shouldThrowExceptionIfBillIsNotStatusOpenInviteRegistered(BillStatusEnum status) {
        //Given
        final var inviteRegisteredResource = InviteRegisteredResourceFixture.getDefault();
        final var existentBillId = 1000L;
        final var bill = billRepository.findById(existentBillId).orElseThrow();
        bill.setStatus(status);
        final var accounts = inviteRegisteredResource.getAccounts();

        //When/Then
        assertThatExceptionOfType(FunctionalWorkflowException.class)
                .isThrownBy(() -> billFacade.inviteRegisteredToBill(existentBillId, accounts))
                .withMessage(ErrorMessageEnum.WRONG_BILL_STATUS.getMessage(BillStatusEnum.OPEN.toString()));
    }

    @Test
    @DisplayName("Should return BillSplitDTO in getDetailedBill where user is Bill Creator")
    void shouldReturnBillSplitDTOInGetDetailedBillWithBillCreator() {
        //Given user that is bill's creator
        final var billId = 2000L;

        //When
        final var billSplitDTO = billFacade.getDetailedBill(billId);

        //Then
        final var bill = billRepository.getBillById(billId);
        verifyBillSplitDTOToBill(billSplitDTO, bill);
        assertThat(billSplitDTO.getItems()).isNotEmpty();
    }

    @Test
    @DisplayName("Should return BillSplitDTO in getDetailedBill where user is in Bill's accounts")
    void shouldReturnBillSplitDTOInGetDetailedBillUserInBillAccounts() {
        //Given user is in bill's accounts
        final var billId = 1100L;

        //When
        final var billSplitDTO = billFacade.getDetailedBill(billId);

        //Then
        final var bill = billRepository.getBillById(billId);
        verifyBillSplitDTOToBill(billSplitDTO, bill);
    }

    @Test
    @DisplayName("Should return BillSplitDTO with status IN_PROGRESS when Start Bill")
    void ShouldReturnBillSplitDTOWhenStartBill() {
        //Given
        final var billId = 1100L;

        //When
        final BillSplitDTO billSplitDTO = billFacade.startBill(billId);

        //Then
        assertThat(billSplitDTO.getStatus()).isEqualTo(BillStatusEnum.IN_PROGRESS);
    }

    @Test
    @DisplayName("Should throw exception if Bill is Resolved and not Open in startBill call")
    void shouldThrowExceptionIfBillIsResolvedNotOpenInStartBill() {
        //Given
        final var billId = 1001L;

        //When/Then
        assertThatExceptionOfType(FunctionalWorkflowException.class).isThrownBy(() -> billFacade.startBill(billId))
                .withMessage(ErrorMessageEnum.WRONG_BILL_STATUS.getMessage(BillStatusEnum.OPEN.toString()));
    }

    @Test
    @DisplayName("Should throw exception if Bill is In Progress and not Open in startBill call")
    void shouldThrowExceptionIfBillIsInProgressNotOpenInStartBill() {
        //Given
        final var billId = 1101L;

        //When/Then
        assertThatExceptionOfType(FunctionalWorkflowException.class).isThrownBy(() -> billFacade.startBill(billId))
                .withMessage(ErrorMessageEnum.WRONG_BILL_STATUS.getMessage(BillStatusEnum.OPEN.toString()));
    }

    @Test
    @DisplayName("Should return BillSplitDTO")
    void shouldReturnBillSplitDTO() {
        //Given
        final Bill bill = BillEntityFixture.getMappedBillSplitDTOFixture();
        bill.getTaxes().clear();

        //When
        final BillSplitDTO billSplitDTO = billFacade.getBillSplitDTO(bill);

        //Then
        verifyBillSplitDTOToBill(billSplitDTO, bill);
    }

    @Test
    @DisplayName("Should return BillSplitDTO when edit bill")
    void shouldReturnBillSplitDTOWhenEditBill() {
        //Given
        final var billId = 1102L;
        final var userEmail = "editBill@email.com";
        final var editBill = EditBillDTOFixture.getDefault();
        editBill.setResponsible("editBill@email.com");
        editBill.getItems().get(0).setId(1013L);

        //When
        final BillSplitDTO billSplit = billFacade.editBill(billId, userEmail, editBill);

        //Then
        assertThat(billSplit.getName()).isEqualTo(editBill.getName());
        assertThat(billSplit.getResponsible().getEmail()).isEqualTo(editBill.getResponsible());
        assertThat(billSplit.getCompany()).isEqualTo(editBill.getCompany());
        assertThat(billSplit.getCategory()).isEqualTo(editBill.getCategory());

        final var items = billSplit.getInformationPerAccount().get(0).getItems();
        final var firstItemDTO = editBill.getItems().get(0);
        final var secondItemDTO = editBill.getItems().get(1);
        final var firstItemPercentageSplitDTO = items.get(0);
        final var secondItemPercentageSplitDTO = items.get(1);
        if (firstItemPercentageSplitDTO.getName().equals(firstItemDTO.getName())) {
            assertThat(firstItemPercentageSplitDTO.getName()).isEqualTo(firstItemDTO.getName());
            assertThat(firstItemPercentageSplitDTO.getCost()).isEqualByComparingTo(firstItemDTO.getCost());
            assertThat(firstItemPercentageSplitDTO.getItemId()).isEqualTo(firstItemDTO.getId());
            assertThat(secondItemPercentageSplitDTO.getName()).isEqualTo(secondItemDTO.getName());
            assertThat(secondItemPercentageSplitDTO.getCost()).isEqualByComparingTo(secondItemDTO.getCost());
            assertThat(secondItemPercentageSplitDTO.getItemId()).isNotNull();
        } else {
            assertThat(secondItemPercentageSplitDTO.getName()).isEqualTo(firstItemDTO.getName());
            assertThat(secondItemPercentageSplitDTO.getCost()).isEqualByComparingTo(firstItemDTO.getCost());
            assertThat(secondItemPercentageSplitDTO.getItemId()).isEqualTo(firstItemDTO.getId());
            assertThat(firstItemPercentageSplitDTO.getName()).isEqualTo(secondItemDTO.getName());
            assertThat(firstItemPercentageSplitDTO.getCost()).isEqualByComparingTo(secondItemDTO.getCost());
            assertThat(firstItemPercentageSplitDTO.getItemId()).isNotNull();
        }
    }

    @Test
    @DisplayName("Should return BillSplitDTO when edit bill twice")
    void shouldReturnBillSplitDTOWhenEditBillTwice() {
        //Given
        final var billId = 1102L;
        final var userEmail = "editBill@email.com";
        final var editBill = EditBillDTOFixture.getDefault();
        editBill.setResponsible("editBill@email.com");
        editBill.getItems().get(0).setId(1013L);
        billFacade.editBill(billId, userEmail, editBill);

        //When
        final BillSplitDTO billSplit = billFacade.editBill(billId, userEmail, editBill);

        //Then
        assertThat(billSplit.getName()).isEqualTo(editBill.getName());
        assertThat(billSplit.getResponsible().getEmail()).isEqualTo(editBill.getResponsible());
        assertThat(billSplit.getCompany()).isEqualTo(editBill.getCompany());
        assertThat(billSplit.getCategory()).isEqualTo(editBill.getCategory());

        final var items = billSplit.getInformationPerAccount().get(0).getItems();
        final var firstItemDTO = editBill.getItems().get(0);
        final var secondItemDTO = editBill.getItems().get(1);
        final var firstItemPercentageSplitDTO = items.get(0);
        final var secondItemPercentageSplitDTO = items.get(1);
        if (firstItemPercentageSplitDTO.getName().equals(firstItemDTO.getName())) {
            assertThat(firstItemPercentageSplitDTO.getName()).isEqualTo(firstItemDTO.getName());
            assertThat(firstItemPercentageSplitDTO.getCost()).isEqualByComparingTo(firstItemDTO.getCost());
            assertThat(firstItemPercentageSplitDTO.getItemId()).isEqualTo(firstItemDTO.getId());
            assertThat(secondItemPercentageSplitDTO.getName()).isEqualTo(secondItemDTO.getName());
            assertThat(secondItemPercentageSplitDTO.getCost()).isEqualByComparingTo(secondItemDTO.getCost());
            assertThat(secondItemPercentageSplitDTO.getItemId()).isNotNull();
        } else {
            assertThat(secondItemPercentageSplitDTO.getName()).isEqualTo(firstItemDTO.getName());
            assertThat(secondItemPercentageSplitDTO.getCost()).isEqualByComparingTo(firstItemDTO.getCost());
            assertThat(secondItemPercentageSplitDTO.getItemId()).isEqualTo(firstItemDTO.getId());
            assertThat(firstItemPercentageSplitDTO.getName()).isEqualTo(secondItemDTO.getName());
            assertThat(firstItemPercentageSplitDTO.getCost()).isEqualByComparingTo(secondItemDTO.getCost());
            assertThat(firstItemPercentageSplitDTO.getItemId()).isNotNull();
        }
    }

    @Test
    @DisplayName("Should throw exception when account does not exist when editing bill")
    void shouldThrowExceptionWhenAccountDoesNotExistWhenEditingBill() {
        //Given
        final var billId = 1102L;
        final var nonExistentEmail = "nonExistingEmail@user.com";
        final var editBill = EditBillDTOFixture.getDefault();

        //When/Then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> billFacade.editBill(billId, nonExistentEmail, editBill))
                .withMessage(ErrorMessageEnum.ACCOUNT_DOES_NOT_EXIST.getMessage());

    }

    @Test
    @DisplayName("Should throw exception when bill already started when editing bill")
    void shouldThrowExceptionWhenBillAlreadyStartedWhenEditingBill() {
        //Given
        final var billId = 1102L;
        final var userEmail = "editBill@email.com";
        final var editBill = EditBillDTOFixture.getDefault();
        editBill.setResponsible("editBill@email.com");
        billFacade.startBill(billId);

        //When/Then
        assertThatExceptionOfType(FunctionalWorkflowException.class)
                .isThrownBy(() -> billFacade.editBill(billId, userEmail, editBill))
                .withMessage(ErrorMessageEnum.WRONG_BILL_STATUS.getMessage(BillStatusEnum.OPEN.name()));
    }

    @Test
    @DisplayName("Should throw exception if tip format is incorrect when editing bill")
    void shouldThrowExceptionWhenBothValuesOfTipEditingBill() {
        //Given
        final var billId = 1102L;
        final var userEmail = "editBill@email.com";
        final var editBill = EditBillDTOFixture.getDefault();
        editBill.setResponsible("editBill@email.com");
        editBill.setTipPercent(BigDecimal.TEN);
        editBill.setTipAmount(BigDecimal.valueOf(69));

        //When/Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> billFacade.editBill(billId, userEmail, editBill))
                .withMessage(ErrorMessageEnum.MULTIPLE_TIP_METHOD.getMessage());
    }

    @Test
    @DisplayName("Should throw exception if tip format is incorrect when editing bill")
    void shouldThrowExceptionForNullTipsWhenEditingBill() {
        //Given
        final var billId = 1102L;
        final var userEmail = "editBill@email.com";
        final var editBill = EditBillDTOFixture.getDefault();
        editBill.setResponsible("editBill@email.com");
        editBill.setTipPercent(null);
        editBill.setTipAmount(null);

        //When/Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> billFacade.editBill(billId, userEmail, editBill))
                .withMessage(ErrorMessageEnum.MULTIPLE_TIP_METHOD.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when edit bill does not have referenced item when editing bill")
    void shouldThrowExceptionWhenEditBillDoesNotHaveReferencedItemWhenEditingBill() {
        //Given
        final var billId = 1102L;
        final var nonExistentItem = 6969L;
        final var userEmail = "editBill@email.com";
        final var editBill = EditBillDTOFixture.getDefault();
        editBill.setResponsible("editBill@email.com");
        editBill.getItems().get(0).setId(nonExistentItem);

        //When/Then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> billFacade.editBill(billId, userEmail, editBill))
                .withMessage(ErrorMessageEnum.ITEM_ID_DOES_NOT_EXIST.getMessage(Long.toString(nonExistentItem)));
    }

    @Test
    @DisplayName("Should return exception when tax id does not exist for edit bill")
    void shouldReturnExceptionForTaxIdNotFoundEditBill() {
        //Given
        final var billId = 1102L;
        final var userEmail = "editBill@email.com";
        final var editBill = EditBillDTOFixture.getDefault();
        editBill.setResponsible("editBill@email.com");
        editBill.getItems().get(0).setId(1013L);
        final var nonExistentTaxDTO = new TaxDTO();
        nonExistentTaxDTO.setId(999L);
        editBill.getTaxes().add(nonExistentTaxDTO);

        //When / Then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> billFacade.editBill(billId, userEmail, editBill))
                .withMessage(ErrorMessageEnum.TAX_ID_DOES_NOT_EXIST.getMessage(List.of(nonExistentTaxDTO.getId()).toString()));


    }

    @Test
    @DisplayName("Should return updated tax for edit bill")
    void shouldReturnUpdatedTaxForEditBIll() {
        //Given
        final var billId = 1102L;
        final var userEmail = "editBill@email.com";
        final var editBill = EditBillDTOFixture.getDefault();
        editBill.setResponsible("editBill@email.com");
        editBill.getItems().get(0).setId(1013L);
        final var taxDto = editBill.getTaxes().iterator().next();
        taxDto.setName("Sharingan");
        taxDto.setPercentage(BigDecimal.ONE);


        //When
        final var billSplitDTO = billFacade.editBill(billId, userEmail, editBill);

        //Then
        assertThat(billSplitDTO.getTaxes()).hasSize(1);
        final var resultTax = billSplitDTO.getTaxes().iterator().next();
        assertThat(resultTax.getName()).isEqualTo(taxDto.getName());
        assertThat(resultTax.getPercentage()).isEqualByComparingTo(taxDto.getPercentage());
        assertThat(resultTax.getId()).isEqualByComparingTo(taxDto.getId());

        final var persistedBill = billRepository.getBillById(billId);
        assertThat(persistedBill.getTaxes()).hasSize(1);
        final var persistedBillTax = persistedBill.getTaxes().iterator().next();
        assertThat(persistedBillTax.getName()).isEqualTo(taxDto.getName());
        assertThat(persistedBillTax.getPercentage()).isEqualByComparingTo(taxDto.getPercentage());
        assertThat(persistedBillTax.getId()).isEqualTo(taxDto.getId());

    }

    @Test
    @DisplayName("Should return added tax for edit bill")
    void shouldReturnAddedTaxForEditBIll() {
        //Given
        final var billId = 1102L;
        final var userEmail = "editBill@email.com";
        final var editBill = EditBillDTOFixture.getDefault();
        editBill.setResponsible("editBill@email.com");
        editBill.getItems().get(0).setId(1013L);
        final var taxDto = new TaxDTO();
        taxDto.setName("Sharingan");
        taxDto.setPercentage(BigDecimal.ONE);
        editBill.getTaxes().add(taxDto);

        //When
        final var billSplitDTO = billFacade.editBill(billId, userEmail, editBill);

        //Then
        assertThat(billSplitDTO.getTaxes()).hasSize(2);
        final var resultTax = billSplitDTO.getTaxes().stream().filter(t -> !t.getId().equals(123L)).findFirst().orElseThrow();
        assertThat(resultTax.getName()).isEqualTo(taxDto.getName());
        assertThat(resultTax.getPercentage()).isEqualByComparingTo(taxDto.getPercentage());
        assertThat(resultTax.getId()).isNotNull();

        final var persistedBill = billRepository.getBillById(billId);
        assertThat(persistedBill.getTaxes()).hasSize(2);
        final var persistedBillTax = billSplitDTO.getTaxes().stream().filter(t -> !t.getId().equals(123L)).findFirst().orElseThrow();
        assertThat(persistedBillTax.getName()).isEqualTo(taxDto.getName());
        assertThat(persistedBillTax.getPercentage()).isEqualByComparingTo(taxDto.getPercentage());
        assertThat(persistedBillTax.getId()).isNotNull();


    }

    @Test
    @DisplayName("Should return informations per account accurate to the specified account")
    void shouldReturnAccurateInformationsPerAccount() {
        //Given
        final var bill = BillEntityFixture.getMappedBillSplitDTOFixture();
        bill.getAccounts().forEach(ab -> ab.setStatus(InvitationStatusEnum.ACCEPTED));
        final var accountBills = new ArrayList<>(bill.getAccounts());
        accountBills.get(0).setAmountPaid(BigDecimal.valueOf(3.00));

        //When
        final var billSplitDTO = billFacade.getBillSplitDTO(bill);

        //Then
        billSplitDTO.getInformationPerAccount().forEach(info -> {
            assertThat(info.getSubTotal()).isEqualByComparingTo(new BigDecimal("2.00"));
            assertThat(info.getTaxes()).isEqualByComparingTo(new BigDecimal("0.20"));
            assertThat(info.getTip()).isEqualByComparingTo(new BigDecimal("5.00"));
            assertThat(info.getInvitationStatus()).isEqualTo(InvitationStatusEnum.ACCEPTED);
            assertThat(info.getPaidStatus()).isNull();

            if (!info.getAmountRemaining().equals(new BigDecimal("7.20"))) {
                assertThat(info.getAmountPaid()).isEqualByComparingTo(new BigDecimal("3.0"));
                assertThat(info.getAmountRemaining()).isEqualByComparingTo(new BigDecimal("4.20"));
            } else {
                assertThat(info.getAmountPaid()).isEqualByComparingTo(BigDecimal.ZERO);
                assertThat(info.getAmountRemaining()).isEqualByComparingTo(new BigDecimal("7.20"));
            }
        });
    }

    @Test
    @DisplayName("Should return informations per account accurate to the specified account NOT ACCEPTED")
    void shouldReturnAccurateInformationsPerAccountNotAccepted() {
        //Given
        final var bill = BillEntityFixture.getMappedBillSplitDTOFixture();
        bill.getAccounts().forEach(ab -> ab.setStatus(InvitationStatusEnum.PENDING));

        //When
        final var billSplitDTO = billFacade.getBillSplitDTO(bill);

        //Then
        billSplitDTO.getInformationPerAccount().forEach(info -> {
            assertThat(info.getSubTotal()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(info.getTaxes()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(info.getTip()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(info.getInvitationStatus()).isEqualTo(InvitationStatusEnum.PENDING);
            assertThat(info.getPaidStatus()).isNull();
        });
    }

    @ParameterizedTest
    @EnumSource(value = InvitationStatusEnum.class, names = "ACCEPTED", mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("Should return informations not accepted")
    void shouldReturnAccurateDeclinedInformationsPerAccount(InvitationStatusEnum invitationStatus) {
        //Given
        final var bill = BillEntityFixture.getMappedBillSplitDTOFixture();
        bill.getAccounts().forEach(ab -> {
            ab.setStatus(invitationStatus);
            // normally when declined or pending, payment status is null
            ab.setPaymentStatus(null);
        });


        //When
        final var billSplitDTO = billFacade.getBillSplitDTO(bill);

        //Then
        billSplitDTO.getInformationPerAccount().forEach(info -> {
            assertThat(info.getSubTotal()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(info.getTaxes()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(info.getTip()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(info.getInvitationStatus()).isEqualTo(invitationStatus);
            assertThat(info.getItems()).isEmpty();
            assertThat(info.getPaidStatus()).isNull();
        });
    }


    private void verifyBillSplitDTOToBill(BillSplitDTO billSplitDTO, Bill bill) {
        final Account billCreatorAccount = bill.getAccounts().stream().map(AccountBill::getAccount)
                .filter(acc -> acc.equals(bill.getCreator()))
                .iterator().next();
        assertThat(billSplitDTO.getCreator().getId()).isEqualTo(billCreatorAccount.getId());
        assertThat(billSplitDTO.getResponsible().getId()).isEqualTo(billCreatorAccount.getId());
        assertThat(billSplitDTO.getStatus()).isEqualTo(bill.getStatus());
        assertThat(billSplitDTO.getId()).isEqualTo(bill.getId());
        assertThat(billSplitDTO.getName()).isEqualTo(bill.getName());
        assertThat(billSplitDTO.getStatus()).isEqualTo(bill.getStatus());
        assertThat(billSplitDTO.getCategory()).isEqualTo(bill.getCategory());
        assertThat(billSplitDTO.getCompany()).isEqualTo(bill.getCompany());
        if (billSplitDTO.getUpdated() != null) {
            assertThat(billSplitDTO.getUpdated()).isCloseTo(bill.getUpdated(), within(200, ChronoUnit.MILLIS));
        }
        if (billSplitDTO.getCreated() != null) {
            assertThat(billSplitDTO.getCreated()).isCloseTo(bill.getCreated(), within(200, ChronoUnit.MILLIS));
        }
        final List<ItemAssociationSplitDTO> itemsPerAccount = billSplitDTO.getInformationPerAccount();
        final Set<AccountBill> accounts = bill.getAccounts();
        assertThat(itemsPerAccount.size()).isEqualTo(accounts.size());

        if (!bill.getItems().isEmpty()) {
            assertThat(bill.getItems()).hasSameSizeAs(billSplitDTO.getItems());

            //for the time being we verify a bill with only 1 item. Should be generic when needed.
            if (bill.getItems().size() == 1) {
                final Item item = bill.getItems().iterator().next();
                final var itemAssociationSplitDTO = itemsPerAccount.stream().filter(it -> it.getSubTotal().compareTo(BigDecimal.ZERO) > 0).findFirst().orElseThrow();
                final ItemPercentageSplitDTO returnItemPercentageSplitDTO = itemAssociationSplitDTO.getItems().get(0);

                assertThat(returnItemPercentageSplitDTO.getName()).isEqualTo(item.getName());
                assertThat(returnItemPercentageSplitDTO.getCost()).isEqualTo(item.getCost());
                if (bill.getTipAmount() != null) { //bill has a tip amount
                    assertThat(billSplitDTO.getBalance()).isEqualTo(item.getCost().add(bill.getTipAmount()).setScale(2, RoundingMode.HALF_UP));
                } else { //bill has a tip percent
                    assertThat(billSplitDTO.getBalance()).isEqualByComparingTo(item.getCost().multiply(
                            bill.getTipPercent().divide(new BigDecimal("100"), RoundingMode.HALF_UP).add(BigDecimal.ONE)));
                }
                // due to the difficulty of testing the calculations here without outright copying the math from the implementation, we'll simply check for null and leave specific tests to verify the cost.
                assertThat(itemAssociationSplitDTO.getSubTotal()).isNotNull();
                assertThat(itemAssociationSplitDTO.getTaxes()).isNotNull();
                assertThat(itemAssociationSplitDTO.getTip()).isNotNull();
            }
        } else {
            assertThat(billSplitDTO.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
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
            assertEquals(item.getCost(), returnBillDTO.getItems().get(0).getCost());
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