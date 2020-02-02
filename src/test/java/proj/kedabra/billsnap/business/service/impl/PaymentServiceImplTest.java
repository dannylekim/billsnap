package proj.kedabra.billsnap.business.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import proj.kedabra.billsnap.business.repository.PaymentRepository;
import proj.kedabra.billsnap.business.service.PaymentService;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.PaymentStatusEnum;
import proj.kedabra.billsnap.fixtures.AccountBillFixture;
import proj.kedabra.billsnap.fixtures.AccountEntityFixture;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;

class PaymentServiceImplTest {

    @Mock
    private PaymentRepository repository;

    private PaymentService paymentService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        paymentService = new PaymentServiceImpl(repository);
    }

    @Test
    @DisplayName("Should throw exception if trying to pay more than the remaining balance")
    void shouldThrowExceptionOnLargerPaymentThanBalance() {
        //Given
        when(repository.getTotalAmountOwedToBill(any(), any())).thenReturn(BigDecimal.TEN);
        final var account = AccountEntityFixture.getDefaultAccount();
        final var paymentAmount = BigDecimal.TEN;

        final var accountBill = AccountBillFixture.getDefault();
        final var bill = accountBill.getBill();
        accountBill.setAmountPaid(new BigDecimal("5"));
        accountBill.setBill(bill);
        account.setBills(Set.of(accountBill));

        //when/then
        assertThatIllegalArgumentException().isThrownBy(() -> paymentService.payBill(account, bill, paymentAmount)).withMessage(ErrorMessageEnum.CANNOT_PAY_MORE_THAN_OWED.getMessage());
    }

    @Test
    @DisplayName("Should pay remaining balance and set to PAID")
    void shouldPayRemainingBalance() {
        //Given
        when(repository.getTotalAmountOwedToBill(any(), any())).thenReturn(BigDecimal.TEN);
        final var account = AccountEntityFixture.getDefaultAccount();
        final var paymentAmount = BigDecimal.TEN;

        final var accountBill = AccountBillFixture.getDefault();
        final var bill = accountBill.getBill();
        final var responsible = AccountEntityFixture.getDefaultAccount();
        responsible.setId(12345L);
        bill.setResponsible(responsible);
        accountBill.setAmountPaid(null);

        final var responsibleAccountBill = AccountBillFixture.getDefault();
        responsibleAccountBill.setAccount(responsible);

        final var unpaidAccountBill = AccountBillFixture.getDefault();

        account.setBills(Set.of(accountBill));
        unpaidAccountBill.setPaymentStatus(PaymentStatusEnum.IN_PROGRESS);
        bill.setAccounts(Set.of(accountBill, unpaidAccountBill, responsibleAccountBill));

        //when
        final var remainingBalance = paymentService.payBill(account, bill, paymentAmount);

        //then
        assertThat(remainingBalance).isEqualTo(BigDecimal.ZERO);
        assertThat(accountBill.getAmountPaid()).isEqualTo(BigDecimal.TEN);
        assertThat(accountBill.getPaymentStatus()).isEqualTo(PaymentStatusEnum.PAID);
        assertThat(bill.getStatus()).isEqualTo(BillStatusEnum.OPEN);
    }

    @Test
    @DisplayName("Should pay the last remaining balance and set bill to Resolved")
    void shouldPayTheFinalRemainingBalanceAndSetBillToResolved() {
        //Given
        when(repository.getTotalAmountOwedToBill(any(), any())).thenReturn(BigDecimal.TEN);
        final var account = AccountEntityFixture.getDefaultAccount();
        final var paymentAmount = BigDecimal.TEN;

        final var accountBill = AccountBillFixture.getDefault();
        final var bill = accountBill.getBill();
        final var responsible = AccountEntityFixture.getDefaultAccount();
        bill.setResponsible(responsible);
        accountBill.setAmountPaid(null);
        account.setBills(Set.of(accountBill));

        //when
        final var remainingBalance = paymentService.payBill(account, bill, paymentAmount);

        //then
        assertThat(remainingBalance).isEqualTo(BigDecimal.ZERO);
        assertThat(accountBill.getAmountPaid()).isEqualTo(BigDecimal.TEN);
        assertThat(accountBill.getPaymentStatus()).isEqualTo(PaymentStatusEnum.PAID);
        assertThat(bill.getStatus()).isEqualTo(BillStatusEnum.RESOLVED);
    }

    @Test
    @DisplayName("Should throw exception if bill is already resolved")
    void shouldThrowExceptionForResolvedBill() {
        //Given
        when(repository.getTotalAmountOwedToBill(any(), any())).thenReturn(BigDecimal.TEN);
        final var account = AccountEntityFixture.getDefaultAccount();
        final var paymentAmount = BigDecimal.TEN;

        final var accountBill = AccountBillFixture.getDefault();
        final var bill = accountBill.getBill();
        bill.setStatus(BillStatusEnum.RESOLVED);
        accountBill.setAmountPaid(null);
        account.setBills(Set.of(accountBill));

        //when/then
        assertThatIllegalStateException().isThrownBy(() -> paymentService.payBill(account, bill, paymentAmount)).withMessage(ErrorMessageEnum.BILL_ALREADY_RESOLVED.getMessage());
    }

    @Test
    @DisplayName("Should throw exception if accountBill is PAID")
    void shouldThrowExceptionForPaidAccountBill() {
        //Given
        when(repository.getTotalAmountOwedToBill(any(), any())).thenReturn(BigDecimal.TEN);
        final var account = AccountEntityFixture.getDefaultAccount();
        final var paymentAmount = BigDecimal.TEN;

        final var accountBill = AccountBillFixture.getDefault();
        final var bill = accountBill.getBill();
        accountBill.setAmountPaid(null);
        accountBill.setPaymentStatus(PaymentStatusEnum.PAID);
        account.setBills(Set.of(accountBill));

        //when/then
        assertThatIllegalStateException().isThrownBy(() -> paymentService.payBill(account, bill, paymentAmount)).withMessage(ErrorMessageEnum.BILL_ALREADY_PAID_FOR.getMessage());

    }


}