package proj.kedabra.billsnap.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.service.impl.AccountServiceImpl;
import proj.kedabra.billsnap.fixtures.LoginResourceFixture;
import proj.kedabra.billsnap.presentation.ApiError;
import proj.kedabra.billsnap.presentation.ApiSubError;
import proj.kedabra.billsnap.utils.SpringProfiles;

@Tag("integration")
@ActiveProfiles(SpringProfiles.TEST)
@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("squid:S00112")
@AutoConfigureTestDatabase
class JwtAuthenticationIT {

    @Resource
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountServiceImpl accountService;

    @Autowired
    private ObjectMapper mapper;

    private final String ENDPOINT = "/login";

    private final String LOGIN_SUCCESS = "Successfully logged in";

    private final String INVALID_INPUTS = "Invalid Login Inputs. Please fix the following errors";

    private final String INVALID_EMAIL = "Must be in an email format. ex: test@email.com.";

    private final String MUST_NOT_BE_BLANK = "must not be blank";

    private final String WRONG_SIZE = "size must be between 0 and 50";

    private final String BAD_CREDENTIALS = "Username or password is incorrect.";

    private final String WRONG_REQ_METHOD = "Incorrect login request method.";

    private final String NOT_JSON_CONTENT = "Login request input is not JSON content-type.";

    private final String USER_FIELD = "email";

    private final String PASSWORD_FIELD = "password";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilter(springSecurityFilterChain)
                .build();
    }

    @Test
    @DisplayName("Login with correct credentials should return OK 200.")
    void LoginWithCorrectCredentialsShouldReturnSuccess() throws Exception {
        //Given
        var loginResource = LoginResourceFixture.getLoginResourceRegistered();

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsString(loginResource))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().is2xxSuccessful()).andReturn();
        String content = result.getResponse().getContentAsString();
        JSONObject contentJson = new JSONObject(content);

        assertNotNull(result.getResponse().getHeader("Authorization"));
        String trimmedAuthorizationHeader = result.getResponse().getHeader("Authorization").replace("Bearer ", "");

        final Account account = accountService.getAccount(loginResource.getEmail());

        assertThat(HttpServletResponse.SC_OK).isEqualTo(result.getResponse().getStatus());
        assertThat(LOGIN_SUCCESS).isEqualTo(contentJson.getString("message"));
        assertThat(trimmedAuthorizationHeader).isEqualTo(contentJson.getString("token"));

        JSONObject profile = new JSONObject(contentJson.getString("profile"));
        assertThat(account.getId().toString()).isEqualTo(profile.getString("id"));
        assertThat(account.getMiddleName()).isEqualTo(profile.getString("middleName"));
        assertThat(account.getFirstName()).isEqualTo(profile.getString("firstName"));
        assertThat(account.getLastName()).isEqualTo(profile.getString("lastName"));
        assertThat(account.getPhoneNumber()).isEqualTo(profile.getString("phoneNumber"));
        assertThat(account.getGender().toString()).isEqualTo(profile.getString("gender"));
        assertThat(account.getEmail()).isEqualTo(profile.getString("email"));
    }

    @Test
    @DisplayName("Login with empty email should return error 400.")
    void LoginWithEmptyEmailShouldReturnError() throws Exception {
        //Given
        var loginResource = LoginResourceFixture.getDefault();
        loginResource.setEmail("");

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsString(loginResource))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);


        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(MUST_NOT_BE_BLANK, error.getErrors().get(0).getMessage());
        assertEquals("", error.getErrors().get(0).getRejectedValue());
        assertEquals(USER_FIELD, error.getErrors().get(0).getField());
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, result.getResponse().getStatus());
    }

    @Test
    @DisplayName("Login with empty password should return error 400.")
    void LoginWithEmptyPasswordShouldReturnError() throws Exception {
        //Given
        var loginResource = LoginResourceFixture.getDefault();
        loginResource.setPassword("");

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsString(loginResource))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(MUST_NOT_BE_BLANK, error.getErrors().get(0).getMessage());
        assertEquals("", error.getErrors().get(0).getRejectedValue());
        assertEquals(PASSWORD_FIELD, error.getErrors().get(0).getField());
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, result.getResponse().getStatus());
    }

    @Test
    @DisplayName("Login with blank email should return error 400.")
    void LoginWithBlankEmailShouldReturnError() throws Exception {
        //Given
        var loginResource = LoginResourceFixture.getDefault();
        loginResource.setEmail(" ");

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsString(loginResource))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(2, error.getErrors().size());

        List<String> errorMessages = error.getErrors().stream().map(ApiSubError::getMessage)
                .filter(msg -> msg.equals(INVALID_EMAIL) || msg.equals(MUST_NOT_BE_BLANK)).collect(Collectors.toList());
        assertEquals(2, errorMessages.size());
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, result.getResponse().getStatus());
    }

    @Test
    @DisplayName("Login with blank password should return error 400.")
    void LoginWithBlankPasswordShouldReturnError() throws Exception {
        //Given
        var loginResource = LoginResourceFixture.getDefault();
        loginResource.setPassword(" ");

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsString(loginResource))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(MUST_NOT_BE_BLANK, error.getErrors().get(0).getMessage());
        assertEquals(" ", error.getErrors().get(0).getRejectedValue());
        assertEquals(PASSWORD_FIELD, error.getErrors().get(0).getField());
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, result.getResponse().getStatus());
    }

    @Test
    @DisplayName("Login where email is too long should return error 400.")
    void LoginWithEmailTooLongShouldReturnError() throws Exception {
        //Given
        var loginResource = LoginResourceFixture.getDefault();
        loginResource.setEmail("emailtoolongggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg@email.com");

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsBytes(loginResource))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(2, error.getErrors().size());
        List<String> errorMessages = error.getErrors().stream().map(ApiSubError::getMessage)
                .filter(msg -> msg.equals(INVALID_EMAIL) || msg.equals(WRONG_SIZE)).collect(Collectors.toList());
        assertEquals(2, errorMessages.size());
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, result.getResponse().getStatus());

    }

    @Test
    @DisplayName("Login where email is null should return error 400.")
    void LoginWithNullEmailShouldReturnError() throws Exception {
        //Given
        var loginResource = LoginResourceFixture.getDefault();
        loginResource.setEmail(null);

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsString(loginResource))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(MUST_NOT_BE_BLANK, error.getErrors().get(0).getMessage());
        assertNull(error.getErrors().get(0).getRejectedValue());
        assertEquals(USER_FIELD, error.getErrors().get(0).getField());
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, result.getResponse().getStatus());
    }

    @Test
    @DisplayName("Login with invalid email format should return error 400.")
    void LoginWithInvalidEmailFormatShouldReturnError() throws Exception {
        //Given
        var loginResource = LoginResourceFixture.getDefault();
        String invalidEmail = "invalid email";
        loginResource.setEmail(invalidEmail);

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsBytes(loginResource))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertEquals(INVALID_INPUTS, error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(INVALID_EMAIL, error.getErrors().get(0).getMessage());
        assertEquals(invalidEmail, error.getErrors().get(0).getRejectedValue());
        assertEquals(USER_FIELD, error.getErrors().get(0).getField());
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, result.getResponse().getStatus());
    }

    @Test
    @DisplayName("Login with non-registered email should return error 401.")
    void LoginWithNonRegisteredEmailShouldReturnError() throws Exception {
        //Given
        var loginResource = LoginResourceFixture.getDefault();
        loginResource.setEmail("nonregistered@email.com");

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsString(loginResource))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertTrue(error.getErrors().isEmpty());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, result.getResponse().getStatus());
        assertEquals(BAD_CREDENTIALS, error.getMessage());
    }

    @Test
    @DisplayName("Login with registered email and wrong password should return error 401.")
    void LoginWithWrongPasswordShouldReturnError() throws Exception {
        //Given
        var loginResource = LoginResourceFixture.getLoginResourceRegistered();
        loginResource.setPassword("wrongpassword");

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsString(loginResource))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertTrue(error.getErrors().isEmpty());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, result.getResponse().getStatus());
        assertEquals(BAD_CREDENTIALS, error.getMessage());
    }

    @Test
    @DisplayName("Login with incorrect request method should return error.")
    void LoginUsingIncorrectRequestMethodShouldReturnError() throws Exception {
        //Given
        var loginResource = LoginResourceFixture.getDefault();

        //When/Then
        MvcResult result = mockMvc.perform(get(ENDPOINT).content(mapper.writeValueAsString(loginResource))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertTrue(error.getErrors().isEmpty());
        assertEquals(HttpServletResponse.SC_FORBIDDEN, result.getResponse().getStatus());
        assertEquals(WRONG_REQ_METHOD, error.getMessage());
    }

    @Test
    @DisplayName("Login where request content-type is null should return error.")
    void LoginWithRequestNullContentTypeShouldReturnError() throws Exception {
        //Given
        var loginResource = LoginResourceFixture.getDefault();

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsString(loginResource)))
                .andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertTrue(error.getErrors().isEmpty());
        assertEquals(HttpServletResponse.SC_FORBIDDEN, result.getResponse().getStatus());
        assertEquals(NOT_JSON_CONTENT, error.getMessage());
    }

    @Test
    @DisplayName("Login where request content-type is not JSON type should return error.")
    void LoginWithRequestNotJsonContentTypeShouldReturnError() throws Exception {
        //Given
        var loginResource = LoginResourceFixture.getDefault();

        //When/Then
        MvcResult result = mockMvc.perform(post(ENDPOINT).content(mapper.writeValueAsString(loginResource))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().is4xxClientError()).andReturn();
        String content = result.getResponse().getContentAsString();
        ApiError error = mapper.readValue(content, ApiError.class);

        assertTrue(error.getErrors().isEmpty());
        assertEquals(HttpServletResponse.SC_FORBIDDEN, result.getResponse().getStatus());
        assertEquals(NOT_JSON_CONTENT, error.getMessage());
    }
}