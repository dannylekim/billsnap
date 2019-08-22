package proj.kedabra.billsnap.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.GrantedAuthority;

import io.jsonwebtoken.JwsHeader;

import proj.kedabra.billsnap.fixtures.UserFixture;
import proj.kedabra.billsnap.presentation.resources.LoginResponseResource;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    private final static String JSON_SUCCESS = "Successfully logged in";

    private final static String JWT_SECRET = "*F-JaNdRgUkXp2s5v8y/B?D(G+KbPeShVmYq3t6w9z$C&F)H@McQfTjWnZr4u7x!";

    private final static Long JWT_EXP = 900000L;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        jwtUtil = new JwtUtil(mapper, JWT_SECRET, JWT_EXP);
    }

    @Test
    @DisplayName("Should return JSON success String given a token")
    void ShouldReturnJsonFormatSuccessGivenAToken() throws Exception {
        //Given
        String testToken = "tester_token";

        //When
        String jsonSuccess = jwtUtil.loginSuccessJson(testToken);
        LoginResponseResource response = mapper.readValue(jsonSuccess, LoginResponseResource.class);

        //Then
        assertEquals(JSON_SUCCESS, response.getMessage());
        assertEquals("tester_token", response.getToken());
    }

    @Test
    @DisplayName("JWT should contain Username used to create the token")
    void JWTShouldContainUsername() {
        //Given
        String token = jwtUtil.generateToken(UserFixture.getDefault());

        //When
        String username = jwtUtil.getJwtUsername(token);

        //Then
        assertEquals(UserFixture.getDefault().getUsername(), username);
    }

    @Test
    @DisplayName("JWT should contain Authorities used to create the token")
    void JwtShouldContainAuthorities() {
        //Given
        String token = jwtUtil.generateToken(UserFixture.getDefault());
        String userRole = UserFixture.getDefault().getAuthorities().iterator().next().toString();

        //When
        Collection<GrantedAuthority> authorities = jwtUtil.getJwtAuthorities(token);

        //Then
        assertEquals(1, authorities.size());
        assertEquals(userRole, authorities.iterator().next().toString());
    }

    @Test
    @DisplayName("JWT should contain Type and Algorithm in Headers")
    void JwtShouldContainTypeAndAlgorithmInHeaders() {
        //Given
        String token = jwtUtil.generateToken(UserFixture.getDefault());

        //When
        JwsHeader jwtHeaders = jwtUtil.getJwtHeaders(token);

        //Then
        assertEquals("JWT", jwtHeaders.getType());
        assertEquals("HS512", jwtHeaders.getAlgorithm());
    }
}