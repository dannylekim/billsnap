package proj.kedabra.billsnap.business.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
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
    @DisplayName("Should pay remaining balance and set to Resolved")
    void shouldPayRemainingBalance() {
        //Given
        when(repository.getTotalAmountOwedToBill(any(), any())).thenReturn(BigDecimal.TEN);
        final var account = AccountEntityFixture.getDefaultAccount();
        final var paymentAmount = BigDecimal.TEN;

        final var accountBill = AccountBillFixture.getDefault();
        final var bill = accountBill.getBill();
        accountBill.setAmountPaid(null);
        account.setBills(Set.of(accountBill));

        //when
        final var remainingBalance = paymentService.payBill(account, bill, paymentAmount);

        //then
        assertThat(remainingBalance).isEqualTo(BigDecimal.ZERO);
        assertThat(accountBill.getAmountPaid()).isEqualTo(BigDecimal.TEN);
        assertThat(bill.getStatus()).isEqualTo(BillStatusEnum.RESOLVED);
    }


}