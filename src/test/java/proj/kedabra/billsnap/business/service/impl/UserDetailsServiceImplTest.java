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

import proj.kedabra.billsnap.business.dto.AccountDTO;
import proj.kedabra.billsnap.business.entities.Account;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.fixtures.AccountDTOFixture;
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
        final String email = "nonexistent@email.com";
        when(accountRepository.getAccountByEmail(email)).thenReturn(null);

        //When /Then
        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(email));
        assertEquals("No user found with email 'nonexistent@email.com'.", ex.getMessage());
    }

    @Test
    @DisplayName("Should return UserDetails built from inputted Account")
    void ShouldReturnUserDetailsBuiltFromAccount(){
        //Given
        var accountObj = AccountEntityFixture.getDefaultAccount();
        accountObj.setEmail("userdetails@unittest.com");
        when(accountRepository.getAccountByEmail("userdetails@unittest.com")).thenReturn(accountObj);

        //When
        UserDetails userDetailsObj = userDetailsService.loadUserByUsername("userdetails@unittest.com");

        //Then
        assertEquals(accountObj.getEmail(), userDetailsObj.getUsername());
        assertEquals(accountObj.getPassword(), userDetailsObj.getPassword());
        assertEquals(1,  userDetailsObj.getAuthorities().size());
        assertEquals("ROLE_USER", userDetailsObj.getAuthorities().stream().findFirst().orElseThrow().getAuthority());
    }
}