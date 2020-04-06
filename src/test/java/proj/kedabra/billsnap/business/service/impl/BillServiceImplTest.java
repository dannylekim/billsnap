package proj.kedabra.billsnap.business.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;

import proj.kedabra.billsnap.business.dto.ItemDTO;
import proj.kedabra.billsnap.business.dto.PaymentOwedDTO;
import proj.kedabra.billsnap.business.exception.AccessForbiddenException;
import proj.kedabra.billsnap.business.exception.FunctionalWorkflowException;
import proj.kedabra.billsnap.business.mapper.BillMapper;
import proj.kedabra.billsnap.business.mapper.PaymentMapper;
import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.model.entities.AccountBill;
import proj.kedabra.billsnap.business.model.entities.AccountItem;
import proj.kedabra.billsnap.business.model.entities.Bill;
import proj.kedabra.billsnap.business.model.entities.Item;
import proj.kedabra.billsnap.business.model.projections.PaymentOwed;
import proj.kedabra.billsnap.business.repository.AccountBillRepository;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.business.repository.BillRepository;
import proj.kedabra.billsnap.business.repository.PaymentRepository;
import proj.kedabra.billsnap.business.service.NotificationService;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.InvitationStatusEnum;
import proj.kedabra.billsnap.fixtures.AccountEntityFixture;
import proj.kedabra.billsnap.fixtures.BillDTOFixture;
import proj.kedabra.billsnap.fixtures.BillEntityFixture;
import proj.kedabra.billsnap.fixtures.PaymentOwedProjectionFixture;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;

class BillServiceImplTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private AccountBillRepository accountBillRepository;

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

    private BillServiceImpl billService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        billService = new BillServiceImpl(billRepository, billMapper, accountBillRepository, paymentMapper, paymentRepository, notificationService);
    }

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

    @ParameterizedTest
    @EnumSource(value = BillStatusEnum.class, names = {"IN_PROGRESS", "RESOLVED"})
    @DisplayName("Should throw exception if Bill is not Open")
    void shouldThrowExceptionIfBillIsNotOpen(BillStatusEnum status) {
        //Given
        final Bill bill = BillEntityFixture.getDefault();
        bill.setStatus(status);

        //When/Then
        assertThatExceptionOfType(FunctionalWorkflowException.class).isThrownBy(() -> billService.verifyBillIsOpen(bill))
                .withMessage(ErrorMessageEnum.BILL_IS_NOT_OPEN.getMessage());
    }

    @Test
    @DisplayName("Should do nothing if Bill is Open")
    void shouldDoNothingIfBillIsOpen() {
        //Given
        final Bill bill = BillEntityFixture.getDefault();
        bill.setStatus(BillStatusEnum.OPEN);

        //When/Then
        assertThatCode(() -> billService.verifyBillIsOpen(bill)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should set Status to IN_PROGRESS when executing startBill")
    void shouldChangeStatusToInProgressWhenStartBill() {
        //Given
        final long billId = 123L;
        final Bill bill = BillEntityFixture.getDefault();
        final Account account = AccountEntityFixture.getDefaultAccount();
        final String billResponsible = "billresponsible@email.com";
        account.setEmail(billResponsible);
        bill.setResponsible(account);
        bill.setStatus(BillStatusEnum.OPEN);

        when(billRepository.findById(any())).thenReturn(Optional.of(bill));

        //When
        final Bill returnedBill = billService.startBill(billId, billResponsible);

        //Then
        assertThat(returnedBill.getStatus()).isEqualTo(BillStatusEnum.IN_PROGRESS);

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
        assertThatExceptionOfType(FunctionalWorkflowException.class).isThrownBy(() -> billService.startBill(billId, billResponsible))
                .withMessage(ErrorMessageEnum.BILL_IS_NOT_OPEN.getMessage());
    }

    @Test
    @DisplayName("Should throw exception if the given User email is not the Bill Responsible in Start Bill")
    void shouldThrowExceptionIfGivenEmailIsNotBillResponsibleInStartBill() {
        //Given
        final long billId = 123L;
        final Bill bill = BillEntityFixture.getDefault();
        final String notBillResponsible = "notbillresponsible@email.com";
        bill.setStatus(BillStatusEnum.OPEN);

        when(billRepository.findById(any())).thenReturn(Optional.of(bill));

        //When/Then
        assertThatExceptionOfType(AccessForbiddenException.class)
                .isThrownBy(() -> billService.startBill(billId, notBillResponsible))
                .withMessage(ErrorMessageEnum.USER_IS_NOT_BILL_RESPONSIBLE.getMessage());
    }

}
