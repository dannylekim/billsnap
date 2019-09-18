package proj.kedabra.billsnap.business.facade.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import proj.kedabra.billsnap.business.dto.PaymentsOwedDTO;
import proj.kedabra.billsnap.business.repository.BillRepository;
import proj.kedabra.billsnap.fixtures.BillEntityFixture;

public class PaymentFacadeImplTest {

    private PaymentFacadeImpl paymentFacadeImpl;

    @Mock
    private BillRepository billRepository;

    @Test
    @DisplayName("Should return an exception if given an email that does not exist")
    void shouldReturnExceptionIfEmailDoesNotExist() {
        //Given
        final String testEmail = "abc@123.ca";
        final var bill = BillEntityFixture.getDefault();
        //when(billRepository.findBillsByStatusAccount(testEmail)).thenReturn(bill);

        //When/then
        final ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class,
                () -> paymentFacadeImpl.getAmountsOwed(testEmail));
        assertThat(resourceNotFoundException.getMessage()).isEqualTo("Account does not exist");
    }

    @Test
    @DisplayName("Should return a valid PaymentOwedDTO")
    void shouldReturnAValidPaymentOwedDTO() {
        //Given
        final String testEmail = "abc@123.ca";

        //when(billRepository.findBillsByStatusAccount(testEmail)).thenReturn(null);

        //When
        PaymentsOwedDTO paymentsOwedDTO = paymentFacadeImpl.getAmountsOwed(testEmail);

        //Then

    }

}
