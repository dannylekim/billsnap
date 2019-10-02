package proj.kedabra.billsnap.business.facade.impl;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import proj.kedabra.billsnap.business.dto.PaymentOwedDTO;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.business.service.impl.BillServiceImpl;
import proj.kedabra.billsnap.fixtures.AccountEntityFixture;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

public class PaymentFacadeImplTest {

    private PaymentFacadeImpl paymentFacadeImpl;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private BillServiceImpl billService;

    @BeforeEach
    void setup() {

        MockitoAnnotations.initMocks(this);
        paymentFacadeImpl = new PaymentFacadeImpl(accountRepository, billService);

    }

    @Test
    @DisplayName("Should return an exception if given an email that does not exist")
    void shouldReturnExceptionIfEmailDoesNotExist() {
        //Given
        final String testEmail = "abc@123.ca";
        when(accountRepository.getAccountByEmail(testEmail)).thenReturn(null);

        //when//then
        final ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class,
                () -> paymentFacadeImpl.getAmountsOwed(testEmail));
        assertThatExceptionOfType(ReflectiveOperationException.class)
                .isThrownBy( () -> paymentFacadeImpl.getAmountsOwed(testEmail))
                .withMessage("Account does not exist");
    }

    @Test
    @DisplayName("Should return empty PaymentOwedDTO when no bills are found")
    void shouldReturnEmptyPaymentOwedDTOWhenNoBillsAreFound() {
        //Given
        final String testEmail = "abc@123.ca";
        final var account = AccountEntityFixture.getDefaultAccount();
        when(accountRepository.getAccountByEmail(testEmail)).thenReturn(account);
        when(billService.calculateAmountOwed(account)).thenReturn(new ArrayList<PaymentOwedDTO>());

        //when
        List<PaymentOwedDTO> listPaymentOwed = paymentFacadeImpl.getAmountsOwed(testEmail);

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
        when(accountRepository.getAccountByEmail(testEmail)).thenReturn(account);
        when(billService.calculateAmountOwed(account)).thenReturn(paymentsOwedList);

        //when
        List<PaymentOwedDTO> listPaymentOwed = paymentFacadeImpl.getAmountsOwed(testEmail);

        //then
        assertThat(listPaymentOwed.size()).isEqualTo(1);
        assertThat(listPaymentOwed.get(0).getEmail()).isEqualTo("owed@yomama.com");
        assertThat(listPaymentOwed.get(0).getAmount()).isEqualTo(BigDecimal.valueOf(69));
    }

}
