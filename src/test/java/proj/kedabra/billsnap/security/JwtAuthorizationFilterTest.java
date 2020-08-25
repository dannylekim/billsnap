package proj.kedabra.billsnap.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;

import proj.kedabra.billsnap.business.service.impl.UserDetailsServiceImpl;
import proj.kedabra.billsnap.fixtures.UserFixture;

class JwtAuthorizationFilterTest {

    private JwtAuthorizationFilter filter;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    private static final String TOKEN_PREFIX = "Bearer ";

    private static final String MALFORMED_TOKEN = "malformed-token";

    private static final String INVALID_SIGNATURE_TOKEN = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQHVzZXIuY29tIiwiZXhwIjoxNTY2ODU" +
            "5MTYyLCJyb2xlcyI6WyJST0xFX1VTRVIiXX0.aNcyQfL6G8HM0t83uGHuwb9xehz4S-xbvoG7bQwsoR-NUNn-URcZV_YFvp3rx5F39HU5yUJSHXMa-OcBkeWJhA";

    private static final String EXPIRED_TOKEN = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJzdHJpbmdAZW1haWwuY29tIiwiZXhwIjoxNTY2ODY1MDY2" +
            "LCJyb2xlcyI6WyJST0xFX1VTRVIiXX0.C9cYU1bb7s--S5wkSI1ofPGYlatG4e53IerkBhzkcuzM-hoki8lgv2-JdyoKC9psGnCDT-TCbiKlNwDtZ4whjA";

    private static final String UNSUPPORTED_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibm" +
            "FtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        filter = new JwtAuthorizationFilter(authenticationManager, jwtService, userDetailsService);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Request to private API with valid token should set Authentication to SecurityContext")
    void RequestToPrivateAPIWithValidTokenShouldReturnSuccess() throws Exception {
        //Given
        MockHttpServletRequest req = new MockHttpServletRequest();
        HttpServletResponse resp = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        User defaultUser = UserFixture.getDefault();
        when(jwtService.getJwtUsername(any())).thenReturn(defaultUser.getUsername());
        when(userDetailsService.loadUserByUsername(any())).thenReturn(defaultUser);

        final String testToken = TOKEN_PREFIX + "test-token";
        req.addHeader("Authorization", testToken);

        //When
        filter.doFilterInternal(req, resp, filterChain);

        //Then
        verify(securityContext).setAuthentication(any());
    }

    @Test
    @DisplayName("Request to private API where token has no 'Bearer ' should not set Authentication to SecurityContext")
    void RequestToPrivateAPIWithNoBearerTokenShouldReturnError() throws Exception {
        //Given
        MockHttpServletRequest req = new MockHttpServletRequest();
        HttpServletResponse resp = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        final String testToken = "test token";
        req.addHeader("Authorization", testToken);

        //When
        filter.doFilterInternal(req, resp, filterChain);

        //Then
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    @DisplayName("Request to private API with expired token should not set Authentication to SecurityContext")
    void RequestToPrivateAPIWithExpiredTokenShouldReturnError() throws Exception {
        //Given
        MockHttpServletRequest req = new MockHttpServletRequest();
        HttpServletResponse resp = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        req.addHeader("Authorization", TOKEN_PREFIX + EXPIRED_TOKEN);

        when(jwtService.getJwtUsername(any())).thenThrow(ExpiredJwtException.class);

        //When
        filter.doFilterInternal(req, resp, filterChain);

        //Then
        verify(securityContext, never()).setAuthentication(any());}

    @Test
    @DisplayName("Request to private API with Malformed token should not set Authentication to SecurityContext")
    void RequestToPrivateAPIWithMalformedTokenShouldReturnError() throws Exception {
        //Given
        MockHttpServletRequest req = new MockHttpServletRequest();
        HttpServletResponse resp = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        req.addHeader("Authorization", TOKEN_PREFIX + MALFORMED_TOKEN);

        when(jwtService.getJwtUsername(any())).thenThrow(MalformedJwtException.class);

        //When
        filter.doFilterInternal(req, resp, filterChain);

        //Then
        verify(securityContext, never()).setAuthentication(any());}

    @Test
    @DisplayName("Request to private API with Unsupported token should not set Authentication to SecurityContext")
    void RequestToPrivateAPIWithUnsupportedTokenShouldReturnError() throws Exception {
        //Given
        MockHttpServletRequest req = new MockHttpServletRequest();
        HttpServletResponse resp = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        req.addHeader("Authorization", TOKEN_PREFIX + UNSUPPORTED_TOKEN);

        when(jwtService.getJwtUsername(any())).thenThrow(UnsupportedJwtException.class);

        //When
        filter.doFilterInternal(req, resp, filterChain);

        //Then
        verify(securityContext, never()).setAuthentication(any());}

    @Test
    @DisplayName("Request to private API with empty token should not set Authentication to SecurityContext")
    void RequestToPrivateAPIWithEmptyTokenShouldReturnError() throws Exception {
        //Given
        MockHttpServletRequest req = new MockHttpServletRequest();
        HttpServletResponse resp = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        req.addHeader("Authorization", TOKEN_PREFIX + "");

        when(jwtService.getJwtUsername(any())).thenThrow(IllegalArgumentException.class);

        //When
        filter.doFilterInternal(req, resp, filterChain);

        //Then
        verify(securityContext, never()).setAuthentication(any());}

    @Test
    @DisplayName("Request to private API with invalid signature token not set Authentication to SecurityContext")
    void RequestToPrivateAPIWithInvalidSignatureTokenShouldReturnError() throws Exception {
        //Given
        MockHttpServletRequest req = new MockHttpServletRequest();
        HttpServletResponse resp = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        req.addHeader("Authorization", TOKEN_PREFIX + INVALID_SIGNATURE_TOKEN);

        when(jwtService.getJwtUsername(any())).thenThrow(SignatureException.class);

        //When
        filter.doFilterInternal(req, resp, filterChain);

        //Then
        verify(securityContext, never()).setAuthentication(any());}
}