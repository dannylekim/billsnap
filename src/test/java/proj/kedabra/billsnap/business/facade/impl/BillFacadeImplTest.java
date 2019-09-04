package proj.kedabra.billsnap.business.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;

import proj.kedabra.billsnap.business.mapper.BillMapper;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.business.service.BillService;
import proj.kedabra.billsnap.fixtures.AccountEntityFixture;
import proj.kedabra.billsnap.fixtures.BillDTOFixture;

class BillFacadeImplTest {

    private BillFacadeImpl billFacade;

    @Mock
    private BillMapper billMapper;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private BillService billService;


    @BeforeEach
    void setup() {

        MockitoAnnotations.initMocks(this);
        billFacade = new BillFacadeImpl(accountRepository, billService, billMapper);

    }

    @Test
    @DisplayName("Should return an exception if given an email that does not exist")
    void shouldReturnExceptionIfEmailDoesNotExist() {
        // Given
        final var billDTO = BillDTOFixture.getDefault();
        final String testEmail = "abc@123.ca";
        when(accountRepository.getAccountByEmail(testEmail)).thenReturn(null);

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
        final String nonExistentEmail = "abc@123.ca";
        final String existingEmail = "accountentity@test.com";
        billDTO.setAccountsList(List.of("existing2@email.com", nonExistentEmail));
        when(accountRepository.getAccountsByEmailIn(any())).thenReturn(null);
        when(accountRepository.getAccountByEmail(existingEmail)).thenReturn(AccountEntityFixture.getDefaultAccount());

        //When/Then
        final ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class,
                () -> billFacade.addPersonalBill(existingEmail, billDTO));
        assertEquals("An account in the list of accounts does not exist", resourceNotFoundException.getMessage());
    }

    @Test
    @DisplayName("Should return exception if list of emails contains bill creator email")
    void ShouldReturnExceptionIfBillCreatorIsInListOfEmails() {
        //TODO
    }

    @Test
    @DisplayName("Should return exception if both tipping methods are null")
    void shouldThrowExceptionIfBothTipNull() {
        //Given
        final var billDTO = BillDTOFixture.getDefault();
        billDTO.setTipAmount(null);
        billDTO.setTipPercent(null);
        final String testEmail = "abc@123.ca";

        //When/Then
        final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> billFacade.addPersonalBill(testEmail, billDTO));

        assertEquals("Only one type of tipping is supported. " +
                "Please make sure only either tip amount or tip percent is set.", illegalArgumentException.getMessage());
    }

    @Test
    @DisplayName("Should return exception if both tipping methods are not null")
    void shouldThrowExceptionIfBothTipNotNull() {
        //Given
        final var billDTO = BillDTOFixture.getDefault();
        billDTO.setTipAmount(BigDecimal.ZERO);
        billDTO.setTipPercent(BigDecimal.ZERO);
        final String testEmail = "abc@123.ca";

        //When/Then
        final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> billFacade.addPersonalBill(testEmail, billDTO));

        assertEquals("Only one type of tipping is supported. " +
                "Please make sure only either tip amount or tip percent is set.", illegalArgumentException.getMessage());

    }
}