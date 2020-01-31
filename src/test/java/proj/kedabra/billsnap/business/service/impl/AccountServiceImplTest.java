package proj.kedabra.billsnap.business.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;

import proj.kedabra.billsnap.business.dto.AccountDTO;
import proj.kedabra.billsnap.business.mapper.AccountMapperImpl;
import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.fixtures.AccountDTOFixture;
import proj.kedabra.billsnap.fixtures.AccountEntityFixture;

class AccountServiceImplTest {

    private AccountServiceImpl accountServiceImpl;

    @Mock
    private AccountRepository accountRepository;


    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        accountServiceImpl = new AccountServiceImpl(accountRepository, new AccountMapperImpl());
    }

    @Test
    @DisplayName("Should return exception if email exists in database")
    void shouldReturnExceptionIfEmailExistsInDatabase() {
        //Given
        when(accountRepository.existsAccountByEmail(any())).thenReturn(true);
        var creationResource = AccountDTOFixture.getCreationDTO();


        //When/Then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> accountServiceImpl.registerAccount(creationResource));
        assertEquals("This email already exists in the database.", ex.getMessage());

    }

    @Test
    @DisplayName("Should return AccountDTO with same values as accountCreationResource")
    void shouldReturnAccountDTOWithSameValues() {
        //Given
        var creationResource = AccountDTOFixture.getCreationDTO();

        when(accountRepository.save(any())).then(AdditionalAnswers.returnsFirstArg());

        //When
        AccountDTO accountDTO = accountServiceImpl.registerAccount(creationResource);

        //Then
        assertEquals(creationResource.getEmail(), accountDTO.getEmail());
        assertEquals(creationResource.getFirstName(), accountDTO.getFirstName());
        assertEquals(creationResource.getLastName(), accountDTO.getLastName());
    }

    @Test
    @DisplayName("Should return AccountDTO")
    void shouldReturnAccountDTO() {
        //Given
        final String email = "test@email.com";
        final Account accountEntity = AccountEntityFixture.getDefaultAccount();
        when(accountRepository.getAccountByEmail(email)).thenReturn(accountEntity);

        //when
        final AccountDTO account = accountServiceImpl.getAccount(email);

        //then
        assertEquals(account.getEmail(), accountEntity.getEmail());
        assertEquals(account.getFirstName(), accountEntity.getFirstName());
        assertEquals(account.getLastName(), accountEntity.getLastName());
    }

    @Test
    @DisplayName("Should throw exception if account doesn't exist")
    void shouldThrowExceptionIfAccountDoesNotExist() {
        //Given
        final String email = "test@email.com";
        final Account accountEntity = AccountEntityFixture.getDefaultAccount();
        when(accountRepository.getAccountByEmail(email)).thenReturn(null);

        //when/then
        assertThrows(ResourceNotFoundException.class, () -> accountServiceImpl.getAccount(email));
    }
}