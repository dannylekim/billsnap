package proj.kedabra.billsnap.business.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.fixtures.AccountEntityFixture;

class UserDetailsServiceImplTest {

    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        userDetailsService = new UserDetailsServiceImpl(accountRepository);
    }

    @Test
    @DisplayName("Should return exception if account cannot be retrieved from database")
    void ShouldReturnExceptionIfAccountCannotBeRetrieved() {
        //Given
        final String nonExistentEmail = "nonexistent@email.com";
        when(accountRepository.getAccountByEmail(nonExistentEmail)).thenReturn(null);

        //When /Then
        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(nonExistentEmail));
        assertEquals(String.format("No user found with email '%s'", nonExistentEmail), ex.getMessage());
    }

    @Test
    @DisplayName("Should return UserDetails built from inputted Account")
    void ShouldReturnUserDetailsBuiltFromAccount(){
        //Given
        var accountObj = AccountEntityFixture.getDefaultAccount();
        final String email = "userdetails@unittest.com";
        accountObj.setEmail(email);
        when(accountRepository.getAccountByEmail(email)).thenReturn(accountObj);

        //When
        UserDetails userDetailsObj = userDetailsService.loadUserByUsername(email);

        //Then
        assertEquals(accountObj.getEmail(), userDetailsObj.getUsername());
        assertEquals(accountObj.getPassword(), userDetailsObj.getPassword());
        assertEquals(1,  userDetailsObj.getAuthorities().size());
        assertEquals("ROLE_USER", userDetailsObj.getAuthorities().stream().findFirst().orElseThrow().getAuthority());
    }
}