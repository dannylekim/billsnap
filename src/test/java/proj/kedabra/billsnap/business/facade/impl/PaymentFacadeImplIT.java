package proj.kedabra.billsnap.business.facade.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.dto.PaymentOwedDTO;
import proj.kedabra.billsnap.utils.SpringProfiles;

@Tag("integration")
@ActiveProfiles(SpringProfiles.TEST)
@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class PaymentFacadeImplIT {

    @Autowired
    private PaymentFacadeImpl paymentFacade;

    @Test
    @DisplayName("Should return an exception if the account does not exist")
    void shouldReturnExceptionIfAccountDoesNotExist() {
        //Given
        final String nonExistentEmail = "fuckingfakemail@castingcouch.com";

        //When/Then
        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() -> paymentFacade.getAmountsOwed(nonExistentEmail))
                .withMessage("Account does not exist");
    }

    @Test
    @DisplayName("Should Return a List of 2 Amount Owed")
    void shouldReturnListOf2AmountOwed() {
        //Given
        final String email = "paymentowed@test.com";

        //When
        final List<PaymentOwedDTO> paymentOwedList = paymentFacade.getAmountsOwed(email);

        //Then
        assertThat(paymentOwedList.size()).isEqualTo(2);
        assertThat(paymentOwedList.get(0).getEmail()).isEqualTo("user@user.com");
        assertThat(paymentOwedList.get(0).getAmount().toString()).isEqualTo("133.00");
        assertThat(paymentOwedList.get(1).getEmail()).isEqualTo("userdetails@service.com");
        assertThat(paymentOwedList.get(1).getAmount().toString()).isEqualTo("489.00");
    }

    @Test
    @DisplayName("Should return empty array for payments owed to oneself")
    void shouldReturnEmptyListIfSoleResponsible() {
        //Given
        final String email = "paymentOwedToMe@email.com";

        //When
        final List<PaymentOwedDTO> paymentOwedList = paymentFacade.getAmountsOwed(email);

        //Then
        assertThat(paymentOwedList.size()).isEqualTo(0);
    }

}
