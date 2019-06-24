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

import proj.kedabra.billsnap.business.dto.AccountDTO;
import proj.kedabra.billsnap.business.mapper.AccountMapperImpl;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.fixtures.AccountCreationResourceFixture;
import proj.kedabra.billsnap.fixtures.AccountDTOFixture;

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

}