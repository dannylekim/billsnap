package proj.kedabra.billsnap.business.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.utils.SpringProfiles;

@Tag("integration")
@ActiveProfiles(SpringProfiles.TEST)
@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class UserDetailsServiceImplIT {

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    @DisplayName("Should return UserDetails object built from inputted Account object")
    void shouldReturnUserDetailsBuiltFromAccount() {
        //Given
        final var existingEmail = "userdetails@service.com";
        final Account accountObj = accountRepository.getAccountByEmail(existingEmail);

        //When
        final UserDetails userDetailsObj = userDetailsServiceImpl.loadUserByUsername(existingEmail);

        //Then
        assertThat(userDetailsObj.getUsername()).isEqualTo(accountObj.getEmail());
        assertThat(userDetailsObj.getPassword()).isEqualTo(accountObj.getPassword());
        assertThat(userDetailsObj.getAuthorities()).hasSize(4);
        assertThat(userDetailsObj.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList())).containsExactly("1002", "1100", "1400", "RESPONSIBLE_1400");
    }

    @Test
    @DisplayName("Should return exception if User cannot be loaded by Username (email)")
    void shouldReturnExceptionIfUserCannotBeLoadedByUsername() {
        //Given
        final var nonExistentEmail = "nonexistent@email.com";

        //When/Then
        final UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsServiceImpl.loadUserByUsername(nonExistentEmail));
        assertThat(ex.getMessage()).isEqualTo(String.format("No user found with email '%s'", nonExistentEmail));
    }
}
