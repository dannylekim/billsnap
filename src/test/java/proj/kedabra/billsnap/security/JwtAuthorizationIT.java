package proj.kedabra.billsnap.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.annotation.Resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import proj.kedabra.billsnap.fixtures.UserFixture;
import proj.kedabra.billsnap.presentation.ApiError;
import proj.kedabra.billsnap.utils.SpringProfiles;

@Tag("integration")
@ActiveProfiles(SpringProfiles.TEST)
@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("squid:S00112")
@AutoConfigureTestDatabase
class JwtAuthorizationIT {

    @Resource
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private JwtService jwtService;

    private static final String PRIVATE_ENDPOINT = "/api-test/private-api-test-controller";

    private static final String PRIVATE_ENDPOINT_MESSAGE = "Private API controller";

    private static final String TOKEN_PREFIX = "Bearer ";

    private static final String ACCESS_UNAUTHORIZED = "Access is unauthorized!";

    private static final String MALFORMED_TOKEN = "malformed-token";

    private static final String INVALID_SIGNATURE_TOKEN = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQHVzZXIuY29tIiwiZXhwIjoxNTY2ODU" +
            "5MTYyLCJyb2xlcyI6WyJST0xFX1VTRVIiXX0.aNcyQfL6G8HM0t83uGHuwb9xehz4S-xbvoG7bQwsoR-NUNn-URcZV_YFvp3rx5F39HU5yUJSHXMa-OcBkeWJhA";

    private static final String EXPIRED_TOKEN = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJzdHJpbmdAZW1haWwuY29tIiwiZXhwIjoxNTY2ODY1MDY2" +
            "LCJyb2xlcyI6WyJST0xFX1VTRVIiXX0.C9cYU1bb7s--S5wkSI1ofPGYlatG4e53IerkBhzkcuzM-hoki8lgv2-JdyoKC9psGnCDT-TCbiKlNwDtZ4whjA";

    private static final String UNSUPPORTED_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibm" +
            "FtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilter(springSecurityFilterChain)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("Request to private API with valid token should have success response")
    void RequestToPrivateAPIWithValidTokenShouldReturnSuccess() throws Exception {
        //Given
        User defaultUser = UserFixture.getDefault();
        String token = TOKEN_PREFIX + jwtService.generateToken(defaultUser);

        //When/Then
        MvcResult result = mockMvc.perform(get(PRIVATE_ENDPOINT).header("Authorization", token)).andExpect(status().is2xxSuccessful()).andReturn();
        assertEquals(PRIVATE_ENDPOINT_MESSAGE, result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Request to private API where token has no 'Bearer ' should return error response")
    void RequestToPrivateAPIWithNoBearerTokenShouldReturnError() throws Exception {
        //Given
        User defaultUser = UserFixture.getDefault();
        String token = jwtService.generateToken(defaultUser);

        //When/Then
        MvcResult result = mockMvc.perform(get(PRIVATE_ENDPOINT).header("Authorization", token)).andExpect(status().isUnauthorized()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertEquals(ACCESS_UNAUTHORIZED, error.getMessage());
        assertEquals(0, error.getErrors().size());
    }

    @Test
    @DisplayName("Request to private API with expired token should return error")
    void RequestToPrivateAPIWithExpiredTokenShouldReturnError() throws Exception {
        //Given/When/Then
        MvcResult result = mockMvc.perform(get(PRIVATE_ENDPOINT).header("Authorization", EXPIRED_TOKEN)).andExpect(status().isUnauthorized()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertEquals(ACCESS_UNAUTHORIZED, error.getMessage());
        assertEquals(0, error.getErrors().size());
    }

    @Test
    @DisplayName("Request to private API with Malformed token should return error")
    void RequestToPrivateAPIWithMalformedTokenShouldReturnError() throws Exception {
        //Given/When/Then
        MvcResult result = mockMvc.perform(get(PRIVATE_ENDPOINT).header("Authorization", MALFORMED_TOKEN)).andExpect(status().isUnauthorized()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertEquals(ACCESS_UNAUTHORIZED, error.getMessage());
        assertEquals(0, error.getErrors().size());
    }

    @Test
    @DisplayName("Request to private API with Unsupported token should return error")
    void RequestToPrivateAPIWithUnsupportedTokenShouldReturnError() throws Exception {
        //Given/When/Then
        MvcResult result = mockMvc.perform(get(PRIVATE_ENDPOINT).header("Authorization", UNSUPPORTED_TOKEN)).andExpect(status().isUnauthorized()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertEquals(ACCESS_UNAUTHORIZED, error.getMessage());
        assertEquals(0, error.getErrors().size());
    }

    @Test
    @DisplayName("Request to private API with empty token should return error")
    void RequestToPrivateAPIWithEmptyTokenShouldReturnError() throws Exception {
        //Given/When/Then
        MvcResult result = mockMvc.perform(get(PRIVATE_ENDPOINT).header("Authorization", "")).andExpect(status().isUnauthorized()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertEquals(ACCESS_UNAUTHORIZED, error.getMessage());
        assertEquals(0, error.getErrors().size());
    }

    @Test
    @DisplayName("Request to private API with invalid signature token should return error")
    void RequestToPrivateAPIWithInvalidSignatureTokenShouldReturnError() throws Exception {
        //Given/When/Then
        MvcResult result = mockMvc.perform(get(PRIVATE_ENDPOINT).header("Authorization", INVALID_SIGNATURE_TOKEN)).andExpect(status().isUnauthorized()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertEquals(ACCESS_UNAUTHORIZED, error.getMessage());
        assertEquals(0, error.getErrors().size());
    }

}
