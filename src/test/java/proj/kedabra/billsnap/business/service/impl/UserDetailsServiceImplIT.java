package proj.kedabra.billsnap.business.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;

import proj.kedabra.billsnap.business.entities.Account;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.utils.SpringProfiles;

@Tag("integration")
@ActiveProfiles(SpringProfiles.TEST)
@SpringBootTest
@AutoConfigureTestDatabase
class UserDetailsServiceImplIT {

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    @DisplayName("Should return UserDetails object built from inputted Account object")
    void shouldReturnUserDetailsBuiltFromAccount() {
        //Given
        var existingEmail = "userdetails@service.com";
        Account accountObj = accountRepository.getAccountByEmail(existingEmail);

        //When
        UserDetails userDetailsObj = userDetailsServiceImpl.loadUserByUsername(existingEmail);

        //Then
        assertEquals(accountObj.getEmail(), userDetailsObj.getUsername());
        assertEquals(accountObj.getPassword(), userDetailsObj.getPassword());
        assertEquals(1, userDetailsObj.getAuthorities().size());
        assertEquals("ROLE_USER", userDetailsObj.getAuthorities().stream().findFirst().orElseThrow().getAuthority());
    }

    @Test
    @DisplayName("Should return exception if User cannot be loaded by Username (email)")
    void shouldReturnExceptionIfUserCannotBeLoadedByUsername() {
        //Given
        var nonExistentEmail = "nonexistent@email.com";

        //When/Then
        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsServiceImpl.loadUserByUsername(nonExistentEmail));
        assertEquals("No user found with email 'nonexistent@email.com'.", ex.getMessage());
    }
}
