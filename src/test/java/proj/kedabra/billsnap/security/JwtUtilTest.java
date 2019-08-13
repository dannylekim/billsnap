package proj.kedabra.billsnap.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.User;

import io.jsonwebtoken.Claims;
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
    @DisplayName("Should return JSON success string given a String token")
    void ShouldReturnJsonFormatSuccessGivenAStringToken() throws Exception {
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
    @DisplayName("Generated JWT should contain Username and role when parsed")
    void GeneratedJWTShouldContainUsernameAndRole() {
        //Given
        User someUser = UserFixture.getDefault();
        String userRole = someUser.getAuthorities().iterator().next().toString();

        //When
        String generatedToken = jwtUtil.generateToken(someUser);
        Claims jwtBody = jwtUtil.getJwtBody(generatedToken);
        JwsHeader jwtHeader = jwtUtil.getJwtHeaders(generatedToken);

        //Then
        assertEquals(someUser.getUsername(), jwtBody.getSubject());
        assertEquals(1, jwtUtil.getJwtAuthorities(jwtBody).size());
        assertEquals(userRole, jwtUtil.getJwtAuthorities(jwtBody).iterator().next().toString());

        assertEquals("JWT", jwtHeader.getType());
        assertEquals("HS512", jwtHeader.getAlgorithm());
    }
}