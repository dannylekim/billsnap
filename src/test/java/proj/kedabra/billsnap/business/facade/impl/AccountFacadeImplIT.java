package proj.kedabra.billsnap.business.facade.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.exception.ResourceNotFoundException;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;
import proj.kedabra.billsnap.utils.SpringProfiles;

@Tag("integration")
@ActiveProfiles(SpringProfiles.TEST)
@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
public class AccountFacadeImplIT {

    @Autowired
    private AccountFacadeImpl accountFacade;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    @DisplayName("Should return an accountDTO")
    void shouldReturnAnAccountDTO() {
        // Given
        final var email = "test@email.com";

        // When
        final var accountDTO = accountFacade.getAccount(email);

        // Then
        final var dbAccount = accountRepository.getAccountByEmail(email);
        assertThat(accountDTO.getId()).isEqualTo(dbAccount.getId());
        assertThat(accountDTO.getEmail()).isEqualTo(dbAccount.getEmail());
        assertThat(accountDTO.getLastName()).isEqualTo(dbAccount.getLastName());
        assertThat(accountDTO.getFirstName()).isEqualTo(dbAccount.getFirstName());
        assertThat(accountDTO.getMiddleName()).isEqualTo(dbAccount.getMiddleName());
        assertThat(accountDTO.getGender()).isEqualTo(dbAccount.getGender().toString());
        assertThat(accountDTO.getPhoneNumber()).isEqualTo(dbAccount.getPhoneNumber());
    }

    @Test
    @DisplayName("Should throw exception when email doesn't exist")
    void shouldThrowExceptionWhenEmailDoesntExist() {
        // Given
        final var nonExistentEmail = "nonExistent@email.com";

        // When // Then
        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() -> accountFacade.getAccount(nonExistentEmail))
                .withMessage(ErrorMessageEnum.ACCOUNT_DOES_NOT_EXIST.getMessage());
    }

}
