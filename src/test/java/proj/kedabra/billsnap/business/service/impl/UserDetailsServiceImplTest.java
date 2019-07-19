package proj.kedabra.billsnap.business.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import proj.kedabra.billsnap.business.repository.AccountRepository;

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
}