package proj.kedabra.billsnap.business.facade.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import proj.kedabra.billsnap.business.dto.PaymentOwedDTO;
import proj.kedabra.billsnap.business.service.AccountService;
import proj.kedabra.billsnap.business.service.PaymentService;
import proj.kedabra.billsnap.business.service.impl.BillServiceImpl;
import proj.kedabra.billsnap.fixtures.AccountEntityFixture;

public class PaymentFacadeImplTest {

    private PaymentFacadeImpl paymentFacadeImpl;

    @Mock
    private AccountService accountService;

    @Mock
    private BillServiceImpl billService;

    @Mock
    private PaymentService paymentService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        paymentFacadeImpl = new PaymentFacadeImpl(accountService, billService, paymentService);
    }

    @Test
    @DisplayName("Should return empty PaymentOwedDTO when no bills are found")
    void shouldReturnEmptyPaymentOwedDTOWhenNoBillsAreFound() {
        //Given
        final String testEmail = "abc@123.ca";
        final var account = AccountEntityFixture.getDefaultAccount();
        when(accountService.getAccount(testEmail)).thenReturn(account);
        when(billService.calculateAmountOwed(account)).thenReturn(new ArrayList<PaymentOwedDTO>());

        //when
        final List<PaymentOwedDTO> listPaymentOwed = paymentFacadeImpl.getAmountsOwed(testEmail);

        //Then
        assertThat(listPaymentOwed.size()).isEqualTo(0);

    }

    @Test
    @DisplayName("Should return list of one account mapped to amount owed")
    void shouldReturnListOfOneAccountMappedToAmountOwed() {
        //Given
        final String testEmail = "abc@123.ca";
        final var account = AccountEntityFixture.getDefaultAccount();
        final var paymentOwed = new PaymentOwedDTO();
        final var paymentsOwedList = new ArrayList<PaymentOwedDTO>();
        paymentOwed.setEmail("owed@yomama.com");
        paymentOwed.setAmount(BigDecimal.valueOf(69));
        paymentsOwedList.add(paymentOwed);
        when(accountService.getAccount(testEmail)).thenReturn(account);
        when(billService.calculateAmountOwed(account)).thenReturn(paymentsOwedList);

        //when
        final List<PaymentOwedDTO> listPaymentOwed = paymentFacadeImpl.getAmountsOwed(testEmail);

        //then
        assertThat(listPaymentOwed.size()).isEqualTo(1);
        assertThat(listPaymentOwed.get(0).getEmail()).isEqualTo("owed@yomama.com");
        assertThat(listPaymentOwed.get(0).getAmount()).isEqualTo(BigDecimal.valueOf(69));
    }

}
