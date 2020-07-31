package proj.kedabra.billsnap.business.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import proj.kedabra.billsnap.business.model.entities.AccountBill;
import proj.kedabra.billsnap.business.model.entities.Notifications;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.fixtures.AccountEntityFixture;
import proj.kedabra.billsnap.fixtures.BillEntityFixture;
import proj.kedabra.billsnap.fixtures.NotificationsFixture;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private AccountRepository accountRepository;

    @Test
    @DisplayName("Should return exception if account cannot be retrieved from database")
    void ShouldReturnExceptionIfAccountCannotBeRetrieved() {
        //Given
        final String nonExistentEmail = "nonexistent@email.com";
        when(accountRepository.getAccountByEmail(nonExistentEmail)).thenReturn(null);

        //When /Then
        final UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(nonExistentEmail));
        assertThat(ex.getMessage()).isEqualTo(String.format("No user found with email '%s'", nonExistentEmail));
    }

    @Test
    @DisplayName("Should return UserDetails built from inputted Account")
    void ShouldReturnUserDetailsBuiltFromAccount() {
        //Given
        final var accountObj = AccountEntityFixture.getDefaultAccount();
        final String email = "userdetails@unittest.com";
        accountObj.setEmail(email);
        when(accountRepository.getAccountByEmail(email)).thenReturn(accountObj);

        //When
        final UserDetails userDetailsObj = userDetailsService.loadUserByUsername(email);

        //Then
        assertThat(userDetailsObj.getUsername()).isEqualTo(accountObj.getEmail());
        assertThat(userDetailsObj.getPassword()).isEqualTo(accountObj.getPassword());
        assertThat(userDetailsObj.getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("Should return UserDetails built from inputted Account with rights of responsible and normal participants")
    void ShouldReturnUserDetailsBuiltFromAccountWithResponsibleAndNormalRights() {
        //Given
        final var accountObj = AccountEntityFixture.getDefaultAccount();
        final String email = "userdetails@unittest.com";
        accountObj.setEmail(email);
        when(accountRepository.getAccountByEmail(email)).thenReturn(accountObj);
        final var bill1 = BillEntityFixture.getDefault();
        bill1.setResponsible(accountObj);
        final var bill2 = BillEntityFixture.getDefault();
        bill2.setId(123L);

        final var accountBill1 = new AccountBill();
        accountBill1.setBill(bill1);
        final var accountBill2 = new AccountBill();
        accountBill2.setBill(bill2);

        accountObj.setBills(Set.of(accountBill1, accountBill2));


        //When
        final UserDetails userDetailsObj = userDetailsService.loadUserByUsername(email);

        //Then
        assertThat(userDetailsObj.getUsername()).isEqualTo(accountObj.getEmail());
        assertThat(userDetailsObj.getPassword()).isEqualTo(accountObj.getPassword());
        assertThat(userDetailsObj.getAuthorities()).hasSize(3);
        final var authorities = userDetailsObj.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        assertThat(authorities).containsOnly("RESPONSIBLE_" + bill1.getId(), bill1.getId().toString(), bill2.getId().toString());
    }
}