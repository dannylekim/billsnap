package proj.kedabra.billsnap.business.facade.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import proj.kedabra.billsnap.business.mapper.AccountMapper;
import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.service.AccountService;
import proj.kedabra.billsnap.fixtures.AccountDTOFixture;
import proj.kedabra.billsnap.fixtures.AccountEntityFixture;

@ExtendWith(MockitoExtension.class)
public class AccountFacadeImplTest {

    @InjectMocks
    private AccountFacadeImpl accountFacade;

    @Mock
    private AccountService accountService;

    @Mock
    private AccountMapper accountMapper;

    @Test
    @DisplayName("Should return an accountDTO")
    void shouldReturnAnAccountDTO() {
        // Given
        final var email = "test@email.com";
        final var accountToRetrieveEnt = AccountEntityFixture.getDefaultAccount();
        final var accountToRetrieveDTO = AccountDTOFixture.getMappedDefaultAccount();

        when(accountService.getAccount(email)).thenReturn(accountToRetrieveEnt);
        when(accountMapper.toDTO(any(Account.class))).thenReturn(accountToRetrieveDTO);

        // When
        final var accountDTO = accountFacade.getAccount(email);

        // Then
        assertThat(accountDTO.getId()).isEqualTo(accountToRetrieveDTO.getId());
        assertThat(accountDTO.getEmail()).isEqualTo(accountToRetrieveDTO.getEmail());
        assertThat(accountDTO.getLastName()).isEqualTo(accountToRetrieveDTO.getLastName());
        assertThat(accountDTO.getFirstName()).isEqualTo(accountToRetrieveDTO.getFirstName());
        assertThat(accountDTO.getMiddleName()).isEqualTo(accountToRetrieveDTO.getMiddleName());
        assertThat(accountDTO.getGender()).isEqualTo(accountToRetrieveDTO.getGender());
        assertThat(accountDTO.getPhoneNumber()).isEqualTo(accountToRetrieveDTO.getPhoneNumber());
    }

}
