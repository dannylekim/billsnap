package proj.kedabra.billsnap.business.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import proj.kedabra.billsnap.business.dto.BaseAccountDTO;
import proj.kedabra.billsnap.business.exception.ResourceNotFoundException;
import proj.kedabra.billsnap.business.mapper.AccountMapperImpl;
import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.business.utils.enums.GenderEnum;
import proj.kedabra.billsnap.fixtures.AccountDTOFixture;
import proj.kedabra.billsnap.fixtures.AccountEntityFixture;
import proj.kedabra.billsnap.fixtures.BaseAccountDTOFixture;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;

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
        Account account = accountServiceImpl.registerAccount(creationResource);

        //Then
        assertEquals(creationResource.getEmail(), account.getEmail());
        assertEquals(creationResource.getFirstName(), account.getFirstName());
        assertEquals(creationResource.getLastName(), account.getLastName());
    }

    @Test
    @DisplayName("Should return Account")
    void shouldReturnAccount() {
        //Given
        final String email = "test@email.com";
        final Account accountEntity = AccountEntityFixture.getDefaultAccount();
        when(accountRepository.getAccountByEmail(email)).thenReturn(accountEntity);

        //when
        final Account account = accountServiceImpl.getAccount(email);

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
        when(accountRepository.getAccountByEmail(email)).thenReturn(null);

        //when/then
        assertThrows(ResourceNotFoundException.class, () -> accountServiceImpl.getAccount(email));
    }

    @Test
    @DisplayName("Should return exception if list of emails contains many emails that do not exist")
    void ShouldReturnExceptionIfManyEmailsInListOfEmailsDoNotExist() {
        //Given
        final var existentAccount = AccountEntityFixture.getDefaultAccount();
        final String existentEmail = "userdetails@service.com";
        existentAccount.setEmail(existentEmail);
        final var anotherExistentAccount = AccountEntityFixture.getDefaultAccount();
        final String anotherExistentEmail = "test@email.com";
        anotherExistentAccount.setEmail(anotherExistentEmail);
        final String nonExistentEmail = "nonexistent@email.com";
        final var secondNonExistentEmail = "anothernonexistent@email.com";
        final var accountsList = List.of(existentEmail, nonExistentEmail, anotherExistentEmail, secondNonExistentEmail);
        final var existentAccountsList = Stream.of(existentAccount, anotherExistentAccount);
        when(accountRepository.getAccountsByEmailIn(accountsList)).thenReturn(existentAccountsList);

        //When/Then
        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() -> accountServiceImpl.getAccounts(accountsList))
                .withMessage(ErrorMessageEnum.LIST_ACCOUNT_DOES_NOT_EXIST.getMessage(List.of(nonExistentEmail, secondNonExistentEmail).toString()));
    }

    @Test
    @DisplayName("Should edit account info successfully")
    void ShouldEditAccountInfoSuccessfully() {
        //Given
        final String email = "test@email.com";
        final Account accountEntity = AccountEntityFixture.getDefaultAccount();
        final BaseAccountDTO editAccount = BaseAccountDTOFixture.getDefault();

        when(accountRepository.getAccountByEmail(email)).thenReturn(accountEntity);
        when(accountRepository.save(any())).thenAnswer(s -> s.getArgument(0));

        //when
        final var result = accountServiceImpl.edit(email, editAccount);

        //then
        assertThat(result.getFirstName()).isEqualTo(editAccount.getFirstName());
        assertThat(result.getMiddleName()).isEqualTo(editAccount.getMiddleName());
        assertThat(result.getLastName()).isEqualTo(editAccount.getLastName());
        assertThat(result.getGender()).isEqualByComparingTo(GenderEnum.FEMALE);
        assertThat(result.getBirthDate().getYear()).isEqualTo(2000);
        assertThat(result.getLocation().getCity()).isEqualTo(editAccount.getLocation().getCity());
    }

    @Test
    @DisplayName("Should throw exception if account doesn't exist when edit account")
    void shouldThrowExceptionIfAccountDoesNotExistWhenEditAccount() {
        //Given
        final String email = "test@email.com";
        final BaseAccountDTO editAccount = BaseAccountDTOFixture.getDefault();
        when(accountRepository.getAccountByEmail(email)).thenReturn(null);

        //when/then
        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() -> accountServiceImpl.edit(email, editAccount))
                .withMessage(ErrorMessageEnum.ACCOUNT_DOES_NOT_EXIST.getMessage());
    }
}