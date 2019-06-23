package proj.kedabra.billsnap.business.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import proj.kedabra.billsnap.business.dto.AccountDTO;
import proj.kedabra.billsnap.business.entities.Account;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.fixtures.AccountDTOFixture;
import proj.kedabra.billsnap.utils.SpringProfiles;

@Tag("integration")
@ActiveProfiles(SpringProfiles.TEST)
@SpringBootTest
@AutoConfigureTestDatabase
class AccountServiceImplIT {

    @Autowired
    private AccountServiceImpl accountService;

    private PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired
    private AccountRepository accountRepository;

    @Test
    @DisplayName("Should return IllegalArgumentException if there's the same email inside the database")
    void shouldReturnExceptionIfSameEmailAsInDb() {
        //Given
        var creationResource = AccountDTOFixture.getCreationDTO();

        //When/Then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> accountService.registerAccount(creationResource));
        assertEquals("This email already exists in the database.", ex.getMessage());

    }

    @Test
    @DisplayName("Should save in the db with a proper bcrypt encrypted password.")
    void shouldSaveInDbWithBcryptPassword() {
        //Given
        var creationResource = AccountDTOFixture.getCreationDTO();
        creationResource.setEmail("aNonExistent@email.com");

        //When
        accountService.registerAccount(creationResource);

        //Then
        Account account = accountRepository.getAccountByEmail(creationResource.getEmail());
        assertTrue(encoder.matches(creationResource.getPassword(), account.getPassword()));

    }

    @Test
    @DisplayName("Should return the same account ID as in DB.")
    void shouldReturnAccountId() {
        //Given
        var creationResource = AccountDTOFixture.getCreationDTO();

        creationResource.setEmail("nonExistentEmail@email.com");

        //When
        AccountDTO dto = accountService.registerAccount(creationResource);


        //Then
        Account account = accountRepository.getAccountByEmail(creationResource.getEmail());
        assertEquals(dto.getId(), account.getId());
    }

}