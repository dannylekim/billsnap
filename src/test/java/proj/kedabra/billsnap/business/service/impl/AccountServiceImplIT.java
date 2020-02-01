package proj.kedabra.billsnap.business.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.transaction.Transactional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.business.utils.enums.AccountStatusEnum;
import proj.kedabra.billsnap.fixtures.AccountDTOFixture;
import proj.kedabra.billsnap.utils.SpringProfiles;

@Tag("integration")
@ActiveProfiles(SpringProfiles.TEST)
@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
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
    @DisplayName("Should save in the db with a proper bcrypt encrypted password and registered.")
    void shouldSaveInDbWithBcryptPassword() {
        //Given
        var creationResource = AccountDTOFixture.getCreationDTO();
        creationResource.setEmail("aNonExistent@email.com");

        //When
        accountService.registerAccount(creationResource);

        //Then
        Account account = accountRepository.getAccountByEmail(creationResource.getEmail());
        assertTrue(encoder.matches(creationResource.getPassword(), account.getPassword()));
        assertEquals(AccountStatusEnum.REGISTERED, account.getStatus());

    }

    @Test
    @DisplayName("Should return the same account ID as in DB.")
    void shouldReturnAccountId() {
        //Given
        var creationResource = AccountDTOFixture.getCreationDTO();

        creationResource.setEmail("nonExistentEmail@email.com");

        //When
        Account dto = accountService.registerAccount(creationResource);


        //Then
        Account account = accountRepository.getAccountByEmail(creationResource.getEmail());
        assertEquals(dto.getId(), account.getId());
    }

    @Test
    @DisplayName("Should throw exception if account is not found in DB")
    void shouldThrowExceptionIfNotFound() {
        //Given
        final String email = "nonExistentEmail@email.com";

        //When/Then
        assertThrows(ResourceNotFoundException.class, () -> accountService.getAccount(email));
    }

    @Test
    @DisplayName("Should return an account from the DB")
    void shouldReturnAnAccountFromDB() {
        //Given
        var creationResource = AccountDTOFixture.getCreationDTO();

        //When
        final Account account = accountService.getAccount(creationResource.getEmail());
        assertThat(account.getId()).isEqualTo(creationResource.getId());

    }

}