package proj.kedabra.billsnap.business.facade.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import proj.kedabra.billsnap.business.dto.PaymentOwedDTO;
import proj.kedabra.billsnap.business.entities.Bill;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.business.repository.BillRepository;
import proj.kedabra.billsnap.business.service.impl.BillServiceImpl;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.fixtures.AccountEntityFixture;
import proj.kedabra.billsnap.fixtures.BillEntityFixture;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;

public class PaymentFacadeImplTest {

    private PaymentFacadeImpl paymentFacadeImpl;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private BillServiceImpl billService;

    @Test
    @DisplayName("Should return an exception if given an email that does not exist")
    void shouldReturnExceptionIfEmailDoesNotExist() {
        //Given
        final String testEmail = "abc@123.ca";

        //when
        when(accountRepository.getAccountByEmail(testEmail)).thenReturn(null);

        //then
        final ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class,
                () -> paymentFacadeImpl.getAmountsOwed(testEmail));
        assertThat(resourceNotFoundException.getMessage()).isEqualTo("Account does not exist");
    }

    @Test
    @DisplayName("Should return empty PaymentOwedDTO when no bills are found")
    void shouldReturnEmptyPaymentOwedDTOWhenNoBillsAreFound() {
        //Given
        final String testEmail = "abc@123.ca";
        final var account = AccountEntityFixture.getDefaultAccount();

        //when
        when(accountRepository.getAccountByEmail(testEmail)).thenReturn(account);
        when(billService.getBillsByStatusAndAccounts(BillStatusEnum.OPEN, account)).thenReturn(null);

        //Then
        List<PaymentOwedDTO> listPaymentOwed = paymentFacadeImpl.getAmountsOwed(testEmail);
        assertThat(listPaymentOwed.size()).isEqualTo(0);

    }

    @Test
    @DisplayName("Should return ...")
    void shouldReturn() {
        //Given
        final String testEmail = "abc@123.ca";
        final var account = AccountEntityFixture.getDefaultAccount();
        final var bill = BillEntityFixture.getDefault();
        final Stream<Bill> billStream = Stream.of(bill);

        //when
        when(accountRepository.getAccountByEmail(testEmail)).thenReturn(account);
        when(billService.getBillsByStatusAndAccounts(BillStatusEnum.OPEN, account)).thenReturn(billStream);

        //then
        List<PaymentOwedDTO> listPaymentOwed = paymentFacadeImpl.getAmountsOwed(testEmail);
        assertThat(listPaymentOwed.size()).isEqualTo(1);
        assertThat(listPaymentOwed.get(0).getEmail()).isEqualTo(bill.getCreator().getEmail());
        assertThat(listPaymentOwed.get(0).getAmount()).isEqualTo(bill.getItems().iterator().next().getCost());
    }

}
