package proj.kedabra.billsnap.business.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import proj.kedabra.billsnap.business.dto.EditBillDTO;
import proj.kedabra.billsnap.business.dto.ItemDTO;
import proj.kedabra.billsnap.business.dto.PaymentOwedDTO;
import proj.kedabra.billsnap.business.dto.TaxDTO;
import proj.kedabra.billsnap.business.exception.AccessForbiddenException;
import proj.kedabra.billsnap.business.exception.FunctionalWorkflowException;
import proj.kedabra.billsnap.business.exception.ResourceNotFoundException;
import proj.kedabra.billsnap.business.mapper.BillMapper;
import proj.kedabra.billsnap.business.mapper.PaymentMapper;
import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.model.entities.AccountBill;
import proj.kedabra.billsnap.business.model.entities.AccountItem;
import proj.kedabra.billsnap.business.model.entities.Bill;
import proj.kedabra.billsnap.business.model.entities.Item;
import proj.kedabra.billsnap.business.model.projections.PaymentOwed;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.business.repository.BillRepository;
import proj.kedabra.billsnap.business.repository.PaymentRepository;
import proj.kedabra.billsnap.business.service.ItemService;
import proj.kedabra.billsnap.business.service.NotificationService;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.InvitationStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.PaymentStatusEnum;
import proj.kedabra.billsnap.fixtures.AccountBillEntityFixture;
import proj.kedabra.billsnap.fixtures.AccountEntityFixture;
import proj.kedabra.billsnap.fixtures.AssociateBillDTOFixture;
import proj.kedabra.billsnap.fixtures.BillDTOFixture;
import proj.kedabra.billsnap.fixtures.BillEntityFixture;
import proj.kedabra.billsnap.fixtures.EditBillDTOFixture;
import proj.kedabra.billsnap.fixtures.ItemAssociationDTOFixture;
import proj.kedabra.billsnap.fixtures.ItemEntityFixture;
import proj.kedabra.billsnap.fixtures.ItemPercentageDTOFixture;
import proj.kedabra.billsnap.fixtures.PaymentOwedProjectionFixture;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;

@ExtendWith(MockitoExtension.class)
class BillServiceImplTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BillMapper billMapper;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EntityManager entityManager;

    @Mock
    private ItemService itemService;

    @InjectMocks
    private BillServiceImpl billService;

    @Test
    @DisplayName("Should return default bill")
    void ShouldReturnDefaultBill() {
        //Given
        final var billDTO = BillDTOFixture.getDefault();

        when(accountRepository.getAccountByEmail(any())).thenReturn(AccountEntityFixture.getDefaultAccount());
        when(billMapper.toEntity(any())).thenReturn(BillEntityFixture.getMappedBillDTOFixture());
        when(billRepository.save(any(Bill.class))).thenAnswer(a -> a.getArguments()[0]);

        final Account account = accountRepository.getAccountByEmail("test@email.com");

        //When
        final Bill bill = billService.createBillToAccount(billDTO, account, new ArrayList<>());

        //Then
        final Set<AccountBill> accounts = bill.getAccounts();
        assertThat(accounts.size()).isEqualTo(1);

        final AccountBill accountBill = accounts.iterator().next();
        assertThat(accountBill.getAccount()).isEqualTo(account);
        assertThat(bill.getActive()).isTrue();
        assertThat(bill.getCategory()).isEqualTo(billDTO.getCategory());
        assertThat(bill.getCompany()).isEqualTo(billDTO.getCompany());
        assertThat(bill.getName()).isEqualTo(billDTO.getName());
        assertThat(bill.getTipAmount()).isEqualTo(billDTO.getTipAmount());
        assertThat(bill.getTipPercent()).isEqualTo(billDTO.getTipPercent());
        assertThat(bill.getItems().size()).isEqualTo(billDTO.getItems().size());
        assertThat(bill.getTaxes().size()).isEqualTo(billDTO.getTaxes().size());
        assertThat(accountBill.getPercentage()).isNull();
        assertThat(accountBill.getStatus()).isEqualTo(InvitationStatusEnum.ACCEPTED);

        assertThat(bill).isEqualTo(accountBill.getBill());

        final ItemDTO itemDTO = billDTO.getItems().get(0);
        final Item itemReturned = bill.getItems().iterator().next();
        assertThat(itemReturned.getName()).isEqualTo(itemDTO.getName());
        assertThat(itemReturned.getCost()).isEqualTo(itemDTO.getCost());

        final AccountItem accountItem = itemReturned.getAccounts().iterator().next();
        assertThat(account).isEqualTo(accountItem.getAccount());
        assertThat(new BigDecimal(100)).isEqualTo(accountItem.getPercentage());
    }

    @Test
    @DisplayName("Should return default bill with valid supplied accounts list")
    void ShouldReturnDefaultBillWithAccountsList() {
        //Given
        final var billDTO = BillDTOFixture.getDefault();
        final var accountEntity = AccountEntityFixture.getDefaultAccount();
        final var billEntity = BillEntityFixture.getMappedBillDTOFixture();
        final var secondAccountEntity = AccountEntityFixture.getDefaultAccount();
        final String someEmail = "some@email.com";
        secondAccountEntity.setEmail(someEmail);

        when(accountRepository.getAccountByEmail(any())).thenReturn(accountEntity);
        when(billMapper.toEntity(any())).thenReturn(billEntity);
        when(billRepository.save(any(Bill.class))).thenAnswer(a -> a.getArguments()[0]);

        final Account account = accountRepository.getAccountByEmail("test@email.com");

        //When
        final Bill bill = billService.createBillToAccount(billDTO, account, List.of(secondAccountEntity));

        //Then
        final Set<AccountBill> accounts = bill.getAccounts();
        assertThat(accounts).hasSize(2);

        final AccountBill creatorAccountBill = accounts.stream()
                .filter(a -> a.getAccount().equals(account))
                .iterator().next();
        assertThat(creatorAccountBill.getAccount()).isEqualTo(account);
        assertThat(bill.getActive()).isTrue();
        assertThat(bill.getCategory()).isEqualTo(billDTO.getCategory());
        assertThat(bill.getCompany()).isEqualTo(billDTO.getCompany());
        assertThat(bill.getName()).isEqualTo(billDTO.getName());
        assertThat(bill.getTipAmount()).isEqualTo(billDTO.getTipAmount());
        assertThat(bill.getTipPercent()).isEqualTo(billDTO.getTipPercent());
        assertThat(bill.getItems().size()).isEqualTo(billDTO.getItems().size());
        assertThat(bill.getTaxes().size()).isEqualTo(billDTO.getTaxes().size());
        assertThat(creatorAccountBill.getPercentage()).isNull();
        assertThat(creatorAccountBill.getStatus()).isEqualTo(InvitationStatusEnum.ACCEPTED);
        assertThat(bill).isEqualTo(creatorAccountBill.getBill());

        final AccountBill secondAccountBill = accounts.stream()
                .filter(a -> !a.getAccount().equals(account))
                .iterator().next();
        assertThat(secondAccountBill.getAccount().getEmail()).isEqualTo(someEmail);

        final ItemDTO itemDTO = billDTO.getItems().get(0);
        final Item itemReturned = bill.getItems().iterator().next();
        assertThat(itemReturned.getName()).isEqualTo(itemDTO.getName());
        assertThat(itemReturned.getCost()).isEqualTo(itemDTO.getCost());

        final AccountItem accountItem = itemReturned.getAccounts().iterator().next();
        assertThat(account).isEqualTo(accountItem.getAccount());
        assertThat(new BigDecimal(100)).isEqualTo(accountItem.getPercentage());
    }

    @Test
    @DisplayName("Should return list of payment owed with mapping")
    void shouldReturnListOfPaymentOwedWithMapping() {
        //Given
        final var account = AccountEntityFixture.getDefaultAccount();
        final var paymentOwed = PaymentOwedProjectionFixture.getDefault();
        final List<PaymentOwed> paymentOwedList = new ArrayList<>();
        paymentOwedList.add(paymentOwed);
        final Stream<PaymentOwed> paymentOwedStream = paymentOwedList.stream();

        final var paymentOwedDTO = new PaymentOwedDTO();
        paymentOwedDTO.setEmail(paymentOwed.getEmail());
        paymentOwedDTO.setAmount(paymentOwed.getAmount());

        when(paymentRepository.getAllAmountOwedByStatusAndAccount(any(BillStatusEnum.class), any(Account.class))).thenReturn(paymentOwedStream);
        when(paymentMapper.toDTO(any(PaymentOwed.class))).thenReturn(paymentOwedDTO);

        //When
        final List<PaymentOwedDTO> result = billService.calculateAmountOwed(account);

        //Then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getEmail()).isEqualTo(paymentOwed.getEmail());
        assertThat(result.get(0).getAmount()).isEqualTo(paymentOwed.getAmount());
    }

    @Test
    @DisplayName("Should throw exception if bill does not exist")
    void shouldThrowExceptionIfBillDoesNotExist() {

        //Given
        when(billRepository.findById(any())).thenReturn(Optional.empty());

        //When/Then
        assertThrows(ResourceNotFoundException.class, () -> billService.getBill(123L));
    }

    @Test
    @DisplayName("Should throw exception if the given User email is not the Bill Responsible")
    void shouldThrowExceptionIfGivenEmailIsNotBillResponsible() {
        //Given
        final Bill bill = BillEntityFixture.getDefault();
        final String notBillResponsible = "notbillresponsible@email.com";

        //When/Then
        assertThatExceptionOfType(AccessForbiddenException.class)
                .isThrownBy(() -> billService.verifyUserIsBillResponsible(bill, notBillResponsible))
                .withMessage(ErrorMessageEnum.USER_IS_NOT_BILL_RESPONSIBLE.getMessage());
    }

    @Test
    @DisplayName("Should do nothing if the given User email is the Bill Responsible")
    void shouldDoNothingIfGivenEmailIsBillResponsible() {
        //Given
        final Bill bill = BillEntityFixture.getDefault();
        final Account account = AccountEntityFixture.getDefaultAccount();
        final String billResponsible = "billresponsible@email.com";
        account.setEmail(billResponsible);
        bill.setResponsible(account);

        //When/Then
        assertThatCode(() -> billService.verifyUserIsBillResponsible(bill, billResponsible)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should create AccountBill with Pending status when inviting one Registered User")
    void shouldCreateAccountBillWhenInviteRegisteredPending() {
        //Given
        final var bill = BillEntityFixture.getDefault();
        final var account = AccountEntityFixture.getDefaultAccount();
        final List<Account> accountsList = List.of(account);
        final int originalBillAccountBillSize = bill.getAccounts().size();

        //When
        billService.inviteRegisteredToBill(bill, accountsList);

        //Then
        assertThat(bill.getAccounts().size()).isEqualTo(originalBillAccountBillSize + 1);
        final var accountBill = bill.getAccounts().stream().filter(ab -> ab.getAccount().equals(account)).findFirst().orElseThrow();
        assertThat(accountBill.getStatus()).isEqualTo(InvitationStatusEnum.PENDING);
        assertThat(accountBill.getPercentage()).isNull();
    }

    @Test
    @DisplayName("Should throw an exception for an association to an account with declined invitation status")
    void shouldThrowExceptionForDeclinedAssociation() {
        //Given
        final var bill = BillEntityFixture.getMappedBillSplitDTOFixture();

        final var accountBill = bill.getAccounts().iterator().next();
        accountBill.setStatus(InvitationStatusEnum.DECLINED);

        final var associateBillDTO = AssociateBillDTOFixture.getDefault();
        associateBillDTO.setId(bill.getId());
        final var account = accountBill.getAccount();
        final var itemAssociationDTO = associateBillDTO.getItems().get(0);
        itemAssociationDTO.setEmail(account.getEmail());
        final var item = ItemEntityFixture.getDefault();
        bill.setItems(Set.of(item));
        itemAssociationDTO.setItems(List.of(ItemPercentageDTOFixture.getDefaultWithId(item.getId())));

        when(billRepository.findById(associateBillDTO.getId())).thenReturn(Optional.of(bill));

        //When/Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> billService.associateItemsToAccountBill(associateBillDTO))
                .withMessage(ErrorMessageEnum.LIST_ACCOUNT_DECLINED.getMessage(List.of(account.getEmail()).toString()));
    }

    @ParameterizedTest
    @EnumSource(value = BillStatusEnum.class, names = {"IN_PROGRESS", "RESOLVED"})
    @DisplayName("Should throw exception if Bill is not Open")
    void shouldThrowExceptionIfBillIsNotOpen(BillStatusEnum status) {
        //Given
        final Bill bill = BillEntityFixture.getDefault();
        bill.setStatus(status);

        //When/Then
        assertThatExceptionOfType(FunctionalWorkflowException.class).isThrownBy(() -> billService.verifyBillStatus(bill, BillStatusEnum.OPEN))
                .withMessage(ErrorMessageEnum.WRONG_BILL_STATUS.getMessage(BillStatusEnum.OPEN.toString()));
    }

    @Test
    @DisplayName("Should throw Exception if Associate Users Bill call contains non-integer valued percentages")
    void shouldThrowExceptionIfAssociateCallHasNonIntegerPercentages() {
        //Given bill with 2 users
        final String billResponsible = "user@withABill.com";
        final BigDecimal nonIntegerValue = BigDecimal.valueOf(50.5);

        final var itemAssociationDTO1 = ItemAssociationDTOFixture.getDefault();
        itemAssociationDTO1.setEmail(billResponsible);
        itemAssociationDTO1.getItems().forEach(itemPercentageDTO -> {
            itemPercentageDTO.setItemId(9001L);
            itemPercentageDTO.setPercentage(nonIntegerValue);
        });
        final var itemAssociationDTO2 = ItemAssociationDTOFixture.getDefault();

        final var associateBillDTO = AssociateBillDTOFixture.getDefault();
        associateBillDTO.setItems(List.of(itemAssociationDTO1, itemAssociationDTO2));

        //When/Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> billService.associateItemsToAccountBill(associateBillDTO))
                .withMessage(ErrorMessageEnum.GIVEN_VALUES_NOT_INTEGER_VALUED.getMessage(List.of(nonIntegerValue).toString()));
    }

    @Test
    @DisplayName("Should throw Exception if /PATCH Associate Users Bill call contains duplicate email")
    void shouldThrowExceptionIfAssociateBillCallContainsDuplicateEmail() {
        //Given bill with 2 users
        final String billResponsible = "user@withABill.com";
        final BigDecimal fifty = BigDecimal.valueOf(50);

        final var itemAssociationDTO1 = ItemAssociationDTOFixture.getDefault();
        itemAssociationDTO1.setEmail(billResponsible);
        itemAssociationDTO1.getItems().forEach(itemPercentageDTO -> {
            itemPercentageDTO.setItemId(9001L);
            itemPercentageDTO.setPercentage(fifty);
        });
        final var itemAssociationDTO2 = ItemAssociationDTOFixture.getDefault();
        itemAssociationDTO2.setEmail(billResponsible);
        itemAssociationDTO2.getItems().forEach(itemPercentageDTO -> {
            itemPercentageDTO.setItemId(8999L);
            itemPercentageDTO.setPercentage(fifty);
        });

        final var associateBillDTO = AssociateBillDTOFixture.getDefault();
        associateBillDTO.setItems(List.of(itemAssociationDTO1, itemAssociationDTO2));

        //When/Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> billService.associateItemsToAccountBill(associateBillDTO))
                .withMessage(ErrorMessageEnum.DUPLICATE_EMAILS_IN_ASSOCIATE_USERS.getMessage(List.of(billResponsible).toString()));
    }

    @Test
    @DisplayName("Should throw Exception if /PATCH Associate Users Bill call contains user not in bill")
    void shouldThrowExceptionIfAssociateBillCallContainsUserNotInBill() {
        //Given bill with 2 users
        final String billResponsible = "user@withABill.com";
        final String billUser = "user@hasbills.com";
        final String userNotInBill = "notinbill@email.com";
        final BigDecimal fifty = BigDecimal.valueOf(50);
        final long existentBillId = 1003L;

        final var account1 = AccountEntityFixture.getDefaultAccount();
        account1.setEmail(billResponsible);
        final var account2 = AccountEntityFixture.getDefaultAccount();
        account2.setEmail(billUser);

        final var accountBill1 = AccountBillEntityFixture.getDefault();
        accountBill1.setAccount(account1);
        final var accountBill2 = AccountBillEntityFixture.getDefault();
        accountBill2.setAccount(account2);

        final var bill = BillEntityFixture.getDefault();
        bill.setId(existentBillId);
        bill.setAccounts(Set.of(accountBill1, accountBill2));

        final var itemAssociationDTO1 = ItemAssociationDTOFixture.getDefault();
        itemAssociationDTO1.setEmail(billResponsible);
        itemAssociationDTO1.getItems().forEach(itemPercentageDTO -> {
            itemPercentageDTO.setItemId(9001L);
            itemPercentageDTO.setPercentage(fifty);
        });
        final var itemAssociationDTO2 = ItemAssociationDTOFixture.getDefault();
        itemAssociationDTO2.setEmail(userNotInBill);
        itemAssociationDTO2.getItems().forEach(itemPercentageDTO -> {
            itemPercentageDTO.setItemId(8999L);
            itemPercentageDTO.setPercentage(fifty);
        });

        final var associateBillDTO = AssociateBillDTOFixture.getDefault();
        associateBillDTO.setItems(List.of(itemAssociationDTO1, itemAssociationDTO2));

        when(billRepository.findById(any())).thenReturn(Optional.of(bill));

        //When/Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> billService.associateItemsToAccountBill(associateBillDTO))
                .withMessage(ErrorMessageEnum.SOME_ACCOUNTS_NONEXISTENT_IN_BILL.getMessage(List.of(userNotInBill).toString()));
    }

    @Test
    @DisplayName("Should throw Exception if /PATCH Associate Users Bill call contains item not in bill")
    void shouldThrowExceptionIfAssociateBillCallContainsItemNotInBill() {
        //Given bill with 2 users
        final String billResponsible = "user@withABill.com";
        final String billUser = "user@hasbills.com";
        final long nonExistentItemId = 9999L;
        final long existentItemId = 1004L;
        final long existentBillId = 1003L;
        final BigDecimal fifty = BigDecimal.valueOf(50);

        final var account1 = AccountEntityFixture.getDefaultAccount();
        account1.setEmail(billResponsible);
        final var account2 = AccountEntityFixture.getDefaultAccount();
        account2.setEmail(billUser);

        final var accountBill1 = AccountBillEntityFixture.getDefault();
        accountBill1.setAccount(account1);
        final var accountBill2 = AccountBillEntityFixture.getDefault();
        accountBill2.setAccount(account2);

        final var bill = BillEntityFixture.getDefault();
        bill.setId(existentBillId);
        bill.setAccounts(Set.of(accountBill1, accountBill2));

        final var item = ItemEntityFixture.getDefault();
        item.setBill(bill);
        item.setId(existentItemId);

        bill.setItems(Set.of(item));

        final var itemAssociationDTO1 = ItemAssociationDTOFixture.getDefault();
        itemAssociationDTO1.setEmail(billResponsible);
        itemAssociationDTO1.getItems().forEach(itemPercentageDTO -> {
            itemPercentageDTO.setItemId(existentItemId);
            itemPercentageDTO.setPercentage(fifty);
        });
        final var itemAssociationDTO2 = ItemAssociationDTOFixture.getDefault();
        itemAssociationDTO2.setEmail(billUser);
        itemAssociationDTO2.getItems().forEach(itemPercentageDTO -> {
            itemPercentageDTO.setItemId(nonExistentItemId);
            itemPercentageDTO.setPercentage(fifty);
        });

        final var associateBillDTO = AssociateBillDTOFixture.getDefault();
        associateBillDTO.setItems(List.of(itemAssociationDTO1, itemAssociationDTO2));

        when(billRepository.findById(any())).thenReturn(Optional.of(bill));

        //When/Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> billService.associateItemsToAccountBill(associateBillDTO))
                .withMessage(ErrorMessageEnum.SOME_ITEMS_NONEXISTENT_IN_BILL.getMessage(List.of(nonExistentItemId).toString()));
    }

    @Test
    @DisplayName("Should return Bill with new Associations in /PATCH bills")
    void shouldReturnBillOnSuccessPATCHAssociateBill() {
        //Given bill with 2 users
        final String billResponsible = "user@withABill.com";
        final String billUser = "user@hasbills.com";
        final long existentItemId = 1004L;
        final long existentBillId = 1003L;
        final BigDecimal fifty = BigDecimal.valueOf(50);

        final var account1 = AccountEntityFixture.getDefaultAccount();
        account1.setEmail(billResponsible);
        final var account2 = AccountEntityFixture.getDefaultAccount();
        account2.setEmail(billUser);

        final var accountBill1 = AccountBillEntityFixture.getDefault();
        accountBill1.setAccount(account1);
        final var accountBill2 = AccountBillEntityFixture.getDefault();
        accountBill2.setAccount(account2);

        final var bill = BillEntityFixture.getDefault();
        bill.setId(existentBillId);
        bill.setAccounts(Set.of(accountBill1, accountBill2));

        final var item = ItemEntityFixture.getDefault();
        item.setBill(bill);
        item.setId(existentItemId);

        bill.setItems(Set.of(item));

        final var itemAssociationDTO1 = ItemAssociationDTOFixture.getDefault();
        itemAssociationDTO1.setEmail(billResponsible);
        itemAssociationDTO1.getItems().forEach(itemPercentageDTO -> {
            itemPercentageDTO.setItemId(existentItemId);
            itemPercentageDTO.setPercentage(fifty);
        });
        final var itemAssociationDTO2 = ItemAssociationDTOFixture.getDefault();
        itemAssociationDTO2.setEmail(billUser);
        itemAssociationDTO2.getItems().forEach(itemPercentageDTO -> {
            itemPercentageDTO.setItemId(existentItemId);
            itemPercentageDTO.setPercentage(fifty);
        });

        final var associateBillDTO = AssociateBillDTOFixture.getDefault();
        associateBillDTO.setItems(List.of(itemAssociationDTO1, itemAssociationDTO2));

        when(billRepository.findById(any())).thenReturn(Optional.of(bill));
        doNothing().when(entityManager).flush();

        //When
        final Bill returnedBill = billService.associateItemsToAccountBill(associateBillDTO);

        //Then
        final List<AccountItem> listAccountItems = returnedBill.getItems().stream()
                .map(Item::getAccounts).flatMap(Set::stream)
                .filter(ai -> ai.getPercentage().compareTo(fifty) == 0).collect(Collectors.toList());
        assertThat(listAccountItems.size()).isEqualTo(2);
    }

    @ParameterizedTest
    @EnumSource(value = BillStatusEnum.class, names = {"IN_PROGRESS", "RESOLVED"})
    @DisplayName("Should throw exception if Bill is not Open in Start Bill")
    void shouldThrowExceptionIfBillIsNotOpenInStartBill(BillStatusEnum status) {
        //Given
        final Bill bill = BillEntityFixture.getDefault();
        bill.setStatus(status);
        final long billId = 123L;
        final Account account = AccountEntityFixture.getDefaultAccount();
        final String billResponsible = "billresponsible@email.com";
        account.setEmail(billResponsible);
        bill.setResponsible(account);

        when(billRepository.findById(any())).thenReturn(Optional.of(bill));

        //When/Then
        assertThatExceptionOfType(FunctionalWorkflowException.class).isThrownBy(() -> billService.startBill(billId))
                .withMessage(ErrorMessageEnum.WRONG_BILL_STATUS.getMessage(BillStatusEnum.OPEN.toString()));
    }

    @Test
    @DisplayName("Should set payment status to IN_PROGRESS and start the bill")
    void shouldStartBill() {
        //Given
        final Bill bill = BillEntityFixture.getDefault();
        bill.setStatus(BillStatusEnum.OPEN);
        final long billId = 123L;
        final Account account = AccountEntityFixture.getDefaultAccount();
        final String billResponsible = "billresponsible@email.com";
        account.setEmail(billResponsible);
        bill.setResponsible(account);

        final var accountBill = new AccountBill();
        accountBill.setAccount(account);
        accountBill.setStatus(InvitationStatusEnum.ACCEPTED);
        accountBill.setBill(bill);

        final Account account2 = AccountEntityFixture.getDefaultAccount();
        account2.setId(234L);


        final var accountBill2 = new AccountBill();
        accountBill.setAccount(account2);
        accountBill.setStatus(InvitationStatusEnum.ACCEPTED);
        accountBill.setBill(bill);

        bill.setAccounts(Set.of(accountBill, accountBill2));

        when(billRepository.findById(any())).thenReturn(Optional.of(bill));

        //When
        final var startedBill = billService.startBill(billId);

        //Then
        assertThat(startedBill.getStatus()).isEqualTo(BillStatusEnum.IN_PROGRESS);
        startedBill.getAccounts().forEach(ab -> {
            if (ab.getStatus() == InvitationStatusEnum.ACCEPTED) {
                assertThat(ab.getPaymentStatus()).isEqualTo(PaymentStatusEnum.IN_PROGRESS);
            } else {
                assertThat(ab.getPaymentStatus()).isNull();
            }
        });

    }

    @Test
    @DisplayName("Should edit bill successfully by Tip Percentage")
    void shouldEditBillSuccessfully() {
        //Given
        final long billId = 123L;
        final Account account = AccountEntityFixture.getDefaultAccount();
        final EditBillDTO editBill = EditBillDTOFixture.getDefault();
        editBill.setResponsible(account.getEmail());

        final var accountBill = AccountBillEntityFixture.getDefault();
        accountBill.setAccount(account);

        final Bill bill = BillEntityFixture.getDefault();
        bill.setAccounts(Set.of(accountBill));
        bill.setTipAmount(null);
        bill.setTipPercent(BigDecimal.ZERO);

        final Item item1 = ItemEntityFixture.getDefault();
        item1.setId(9999L);
        item1.setCost(BigDecimal.valueOf(90));

        when(billRepository.findById(any())).thenReturn(Optional.of(bill));
        when(billRepository.save(any())).thenAnswer(s -> s.getArgument(0));

        //When
        final Bill result = billService.editBill(billId, account, editBill);

        //Then
        assertThat(result.getResponsible().getId()).isEqualTo(bill.getResponsible().getId());
        assertThat(result.getTipPercent()).isEqualTo(bill.getTipPercent());
        assertThat(result.getCategory()).isEqualTo(bill.getCategory());
        assertThat(result.getCompany()).isEqualTo(bill.getCompany());
    }

    @Test
    @DisplayName("Should edit bill successfully by Tip Amount")
    void shouldEditBillSuccessfullyByTipAmount() {
        //Given
        final long billId = 123L;
        final Account account = AccountEntityFixture.getDefaultAccount();
        final EditBillDTO editBill = EditBillDTOFixture.getDefault();
        editBill.setTipPercent(null);
        editBill.setTipAmount(BigDecimal.TEN);
        editBill.setResponsible(account.getEmail());

        final var accountBill = AccountBillEntityFixture.getDefault();
        accountBill.setAccount(account);

        final Bill bill = BillEntityFixture.getDefault();
        bill.setAccounts(Set.of(accountBill));

        final Item item1 = ItemEntityFixture.getDefault();
        item1.setId(9999L);
        item1.setCost(BigDecimal.valueOf(90));

        when(billRepository.findById(any())).thenReturn(Optional.of(bill));
        when(billRepository.save(any())).thenAnswer(s -> s.getArgument(0));

        //When
        final Bill result = billService.editBill(billId, account, editBill);

        //Then
        assertThat(result.getResponsible().getId()).isEqualTo(bill.getResponsible().getId());
        assertThat(result.getTipAmount()).isEqualTo(editBill.getTipAmount());
        assertThat(result.getCategory()).isEqualTo(bill.getCategory());
        assertThat(result.getCompany()).isEqualTo(bill.getCompany());
    }

    @Test
    @DisplayName("Should throw exception when bill already started")
    void shouldThrowExceptionWhenBillAlreadyStarted() {
        //Given
        final long billId = 123L;
        final Account account = AccountEntityFixture.getDefaultAccount();
        final EditBillDTO editBill = EditBillDTOFixture.getDefault();
        final Bill bill = BillEntityFixture.getDefault();
        bill.setStatus(BillStatusEnum.IN_PROGRESS);

        when(billRepository.findById(any())).thenReturn(Optional.of(bill));

        //When/Then
        assertThatExceptionOfType(FunctionalWorkflowException.class)
                .isThrownBy(() -> billService.editBill(billId, account, editBill))
                .withMessage(ErrorMessageEnum.WRONG_BILL_STATUS.getMessage(BillStatusEnum.OPEN.toString()));
    }

    @Test
    @DisplayName("Should throw if edit bill with wrong tip format where both tips have values")
    void shouldThrowIfEditBillWithWrongTipFormat() {
        //Given
        final long billId = 123L;
        final Account account = AccountEntityFixture.getDefaultAccount();
        final EditBillDTO editBill = EditBillDTOFixture.getDefault();
        editBill.setResponsible(account.getEmail());
        editBill.setTipAmount(BigDecimal.valueOf(20));
        editBill.setTipPercent(BigDecimal.TEN);

        final var accountBill = AccountBillEntityFixture.getDefault();
        accountBill.setAccount(account);

        final Bill bill = BillEntityFixture.getDefault();
        bill.setAccounts(Set.of(accountBill));
        bill.setTipAmount(null);

        when(billRepository.findById(any())).thenReturn(Optional.of(bill));

        //When/Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> billService.editBill(billId, account, editBill))
                .withMessage(ErrorMessageEnum.MULTIPLE_TIP_METHOD.getMessage());
    }

    @Test
    @DisplayName("Should throw if edit bill with wrong tip format where both tips do not have values")
    void shouldThrowIfEditBillWithTipBeingNull() {
        //Given
        final long billId = 123L;
        final Account account = AccountEntityFixture.getDefaultAccount();
        final EditBillDTO editBill = EditBillDTOFixture.getDefault();
        editBill.setResponsible(account.getEmail());
        editBill.setTipAmount(null);
        editBill.setTipPercent(null);

        final var accountBill = AccountBillEntityFixture.getDefault();
        accountBill.setAccount(account);

        final Bill bill = BillEntityFixture.getDefault();
        bill.setAccounts(Set.of(accountBill));
        bill.setTipAmount(null);

        when(billRepository.findById(any())).thenReturn(Optional.of(bill));

        //When/Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> billService.editBill(billId, account, editBill))
                .withMessage(ErrorMessageEnum.MULTIPLE_TIP_METHOD.getMessage());
    }


    @Test
    @DisplayName("Should throw if Tax Id does not exist in the current bill")
    void shouldThrowIfTaxIdDoesNotExistInCurrentBill() {
        //Given
        final long billId = 123L;
        final Account account = AccountEntityFixture.getDefaultAccount();
        final EditBillDTO editBill = EditBillDTOFixture.getDefault();
        editBill.setTipPercent(null);
        editBill.setTipAmount(BigDecimal.TEN);
        editBill.setResponsible(account.getEmail());
        final var taxDTO = new TaxDTO();
        taxDTO.setId(9999L);
        editBill.setTaxes(List.of(taxDTO));

        final var accountBill = AccountBillEntityFixture.getDefault();
        accountBill.setAccount(account);

        final Bill bill = BillEntityFixture.getDefault();
        bill.setAccounts(Set.of(accountBill));

        final Item item1 = ItemEntityFixture.getDefault();
        item1.setId(9999L);
        item1.setCost(BigDecimal.valueOf(90));

        when(billRepository.findById(any())).thenReturn(Optional.of(bill));

        //When/Then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> billService.editBill(billId, account, editBill))
                .withMessage(ErrorMessageEnum.TAX_ID_DOES_NOT_EXIST.getMessage(editBill.getTaxes().stream().map(TaxDTO::getId).collect(Collectors.toList()).toString()));
    }

}
