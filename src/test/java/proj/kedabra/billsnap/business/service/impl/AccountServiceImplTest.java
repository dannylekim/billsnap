package proj.kedabra.billsnap.business.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.business.service.impl.AccountServiceImpl;
import proj.kedabra.billsnap.fixtures.AccountCreationResourceFixture;

class AccountServiceImplTest {

    private AccountServiceImpl accountServiceImpl;

    @Mock
    private AccountRepository accountRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        accountServiceImpl = new AccountServiceImpl(accountRepository);
    }

    @Test
    @DisplayName("Should return exception if email exists in database")
    void shouldReturnExceptionIfEmailExistsInDatabase() {
        //Given
        when(accountRepository.getAccountByEmail(any())).thenThrow(IllegalArgumentException.class);
        var creationResource = AccountCreationResourceFixture.getDefault();

        //When/Then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> accountServiceImpl.registerAccount(creationResource));
        assertEquals("This email already exists in the database", ex.getMessage());

    }


}