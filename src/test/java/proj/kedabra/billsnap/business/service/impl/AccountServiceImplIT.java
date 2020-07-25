package proj.kedabra.billsnap.business.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.transaction.Transactional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import proj.kedabra.billsnap.business.dto.BaseAccountDTO;
import proj.kedabra.billsnap.business.exception.ResourceNotFoundException;
import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.business.utils.enums.AccountStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.GenderEnum;
import proj.kedabra.billsnap.fixtures.AccountDTOFixture;
import proj.kedabra.billsnap.fixtures.AccountEntityFixture;
import proj.kedabra.billsnap.fixtures.BaseAccountDTOFixture;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;
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
        final var creationResource = AccountDTOFixture.getCreationDTO();

        //When
        final Account account = accountService.getAccount(creationResource.getEmail());
        assertThat(account.getId()).isEqualTo(creationResource.getId());

    }

    @Test
    @DisplayName("Should return exception if list of emails contains one email that does not exist")
    void ShouldReturnExceptionIfEmailInListOfEmailsDoesNotExist() {
        //Given
        final String existentEmail = "userdetails@service.com";
        final String anotherExistentEmail = "test@email.com";
        final String nonExistentEmail = "nonexistent@email.com";
        final var accountsList = List.of(existentEmail, nonExistentEmail, anotherExistentEmail);

        //When/Then
        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() -> accountService.getAccounts(accountsList))
                .withMessage(ErrorMessageEnum.LIST_ACCOUNT_DOES_NOT_EXIST.getMessage(List.of(nonExistentEmail).toString()));
    }

    @Test
    @DisplayName("Should return exception if list of emails contains many emails that do not exist")
    void ShouldReturnExceptionIfManyEmailsInListOfEmailsDoNotExist() {
        //Given
        final String existentEmail = "userdetails@service.com";
        final String anotherExistentEmail = "test@email.com";
        final String nonExistentEmail = "nonexistent@email.com";
        final var secondNonExistentEmail = "anothernonexistent@email.com";
        final var accountsList = List.of(existentEmail, nonExistentEmail, anotherExistentEmail, secondNonExistentEmail);

        //When/Then
        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() -> accountService.getAccounts(accountsList))
                .withMessage(ErrorMessageEnum.LIST_ACCOUNT_DOES_NOT_EXIST.getMessage(List.of(nonExistentEmail, secondNonExistentEmail).toString()));
    }

    @Test
    @DisplayName("Should edit account info successfully")
    void shouldEditAccountInfoSuccessfully() {
        //Given
        final String existentEmail = "test@email.com";
        final var editAccount = BaseAccountDTOFixture.getDefault();

        //When
        final var result = accountService.edit(existentEmail, editAccount);

        //Then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getEmail()).isNotNull();
        assertThat(result.getPassword()).isNotNull();
        assertThat(result.getFirstName()).isEqualTo(editAccount.getFirstName());
        assertThat(result.getLastName()).isEqualTo(editAccount.getLastName());
        assertThat(result.getMiddleName()).isEqualTo(editAccount.getMiddleName());
        assertThat(result.getGender()).isEqualByComparingTo(GenderEnum.FEMALE);
        assertThat(result.getPhoneNumber()).isEqualTo(editAccount.getPhoneNumber());
        assertThat(result.getBirthDate().getYear()).isEqualTo(2000);
        assertThat(result.getLocation().getCity()).isEqualTo(editAccount.getLocation().getCity());

    }

    @Test
    @DisplayName("Should throw exception when edit account")
    void shouldThrowExceptionWhenEditAccount() {
        //Given
        final String nonExistentEmail = "nonExistent@email.com";
        final var editAccount = BaseAccountDTOFixture.getDefault();

        //When/Then
        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() -> accountService.edit(nonExistentEmail, editAccount))
                .withMessage(ErrorMessageEnum.ACCOUNT_DOES_NOT_EXIST.getMessage());

    }
}