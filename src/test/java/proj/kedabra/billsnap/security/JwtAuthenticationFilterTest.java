package proj.kedabra.billsnap.security;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;

import proj.kedabra.billsnap.business.exception.LoginValidationException;
import proj.kedabra.billsnap.business.mapper.AccountMapper;
import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.service.impl.AccountServiceImpl;
import proj.kedabra.billsnap.fixtures.AccountEntityFixture;
import proj.kedabra.billsnap.fixtures.LoginResourceFixture;
import proj.kedabra.billsnap.fixtures.UserFixture;
import proj.kedabra.billsnap.presentation.resources.LoginResource;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;

class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;

    private final ObjectMapper mapper = new ObjectMapper();

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private Validator validator;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private AccountServiceImpl accountService;

    private final String MUST_NOT_BE_BLANK = "must not be blank";

    private final String VALID_USER = "email@test.com";

    private final String VALID_PASSWORD = "password";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        filter = new JwtAuthenticationFilter(authenticationManager, jwtService, validator, mapper, accountService, accountMapper);
    }

    @Test
    @DisplayName("Should throw Exception if request has incorrect method (not POST)")
    void ShouldThrowExceptionWhenRequestHasIncorrectMethod() {
        //Given
        MockHttpServletRequest mockRequest = populateMockRequest();
        mockRequest.setMethod(HttpMethod.PUT.name());
        HttpServletResponse resp = mock(HttpServletResponse.class);

        //When/Then
        AuthenticationServiceException ex = assertThrows(AuthenticationServiceException.class, () -> filter.attemptAuthentication(mockRequest, resp));
        assertEquals(ErrorMessageEnum.WRONG_REQ_METHOD.getMessage(), ex.getMessage());
    }

    @Test
    @DisplayName("Should throw Exception if request is not JSON content-type")
    void ShouldThrowExceptionWhenRequestBodyIsNotJsonType() {
        //Given
        MockHttpServletRequest mockRequest = populateMockRequest();
        mockRequest.setContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        //When/Then
        AuthenticationServiceException ex = assertThrows(AuthenticationServiceException.class, () -> filter.attemptAuthentication(mockRequest, resp));
        assertEquals(ErrorMessageEnum.MEDIA_TYPE_NOT_JSON.getMessage(), ex.getMessage());
    }

    @Test
    @DisplayName("Should throw Exception if request content-type is null")
    void ShouldThrowExceptionWhenRequestBodyIsNull() {
        //Given
        MockHttpServletRequest mockRequest = populateMockRequest();
        mockRequest.setContentType(null);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        //When/Then
        AuthenticationServiceException ex = assertThrows(AuthenticationServiceException.class, () -> filter.attemptAuthentication(mockRequest, resp));
        assertEquals(ErrorMessageEnum.MEDIA_TYPE_NOT_JSON.getMessage(), ex.getMessage());
    }

    @Test
    @DisplayName("Should throw Exception if login request body has validation errors")
    void ShouldThrowExceptionWhenBodyHasValidationErrors() throws Exception {
        //Given
        MockHttpServletRequest mockRequest = populateMockRequest();
        HttpServletResponse resp = mock(HttpServletResponse.class);

        var loginResource = LoginResourceFixture.getDefault();
        loginResource.setEmail("");
        mockRequest.setContent(mapper.writeValueAsBytes(loginResource));

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(LoginResource.class, LoginResource.class.getSimpleName());
        errors.addError(new FieldError("BlankError", "username", "", false, new String[0], new Object[0], MUST_NOT_BE_BLANK));

        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((Errors) args[1]).addAllErrors(errors);
            return null;
        }).when(validator).validate(any(), any(Errors.class));

        //When/Then
        LoginValidationException ex = assertThrows(LoginValidationException.class, () -> filter.attemptAuthentication(mockRequest, resp));
        assertNotEquals(0, ex.getErrorsList().size());
        assertEquals(ErrorMessageEnum.INVALID_LOGIN_INPUTS.getMessage(), ex.getMessage());
    }

    @Test
    @DisplayName("Should return Authentication object upon successful authentication")
    void ShouldReturnAuthenticationObjectUponSuccessfulAuthentication() throws Exception {
        //Given
        MockHttpServletRequest mockRequest = populateMockRequest();
        HttpServletResponse resp = mock(HttpServletResponse.class);

        var loginResource = LoginResourceFixture.getDefault();
        loginResource.setEmail(VALID_USER);
        loginResource.setPassword(VALID_PASSWORD);
        mockRequest.setContent(mapper.writeValueAsBytes(loginResource));

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(VALID_USER, VALID_PASSWORD);
        when(authenticationManager.authenticate(any())).thenReturn(token);

        //When
        Authentication authAttempt = filter.attemptAuthentication(mockRequest, resp);

        //Then
        assertNotNull(authAttempt);
        assertEquals(VALID_USER, authAttempt.getPrincipal().toString());
    }

    @Test
    @DisplayName("Response should have token in Authorization header and message body upon successful authentication")
    void ResponseShouldHaveTokenUponSuccess() throws Exception {
        //Given
        MockHttpServletRequest mockRequest = populateMockRequest();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        final String jwtToken = "example_token";
        final Account account = AccountEntityFixture.getDefaultAccount();
        final String successMes = "{\"token\":\"%s\", \"profile\":\"%s\"}";
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(UserFixture.getDefault(), VALID_PASSWORD);

        when(jwtService.generateToken(any())).thenReturn(jwtToken);
        when(accountService.getAccount(any())).thenReturn(account);
        when(jwtService.loginSuccessJson(any(), any())).thenReturn(String.format(successMes, jwtToken, account.toString()));

        //When
        filter.successfulAuthentication(mockRequest, mockResponse, mock(FilterChain.class), token);

        //Then
        JSONObject contentJson = new JSONObject(mockResponse.getContentAsString());
        assertThat(jwtToken).isEqualTo(Objects.requireNonNull(mockResponse.getHeader("Authorization")).replace("Bearer ", ""));
        assertThat(MediaType.APPLICATION_JSON_VALUE).isEqualTo(mockResponse.getContentType());
        assertThat(jwtToken).isEqualTo(contentJson.getString("token"));
        assertThat(account.toString()).isEqualTo(contentJson.getString("profile"));
    }

    @Test
    @DisplayName("Should throw Exception upon unsuccessful authentication")
    void ShouldThrowExceptionUponUnsuccessfulAuthentication() throws Exception {
        //Given
        MockHttpServletRequest mockRequest = populateMockRequest();
        HttpServletResponse resp = mock(HttpServletResponse.class);

        var loginResource = LoginResourceFixture.getDefault();
        loginResource.setEmail("nonexistent@email.com");
        mockRequest.setContent(mapper.writeValueAsBytes(loginResource));

        doThrow(BadCredentialsException.class).when(authenticationManager).authenticate(any());

        //When/Then
        assertThrows(BadCredentialsException.class, () -> filter.attemptAuthentication(mockRequest, resp));
    }

    @Test
    @DisplayName("Should throw Exception upon Request content processing IOException")
    void ShouldThrowIOExceptionUponRequestContentProcessingError() {
        //Given
        MockHttpServletRequest mockRequest = populateMockRequest();
        HttpServletResponse resp = mock(HttpServletResponse.class);

        mockRequest.setContent("invalid request content".getBytes(UTF_8));

        //When/Then
        assertThrows(AuthenticationServiceException.class, () -> filter.attemptAuthentication(mockRequest, resp));
    }

    @Test
    @DisplayName("Should throw Exception upon Request getReader() IOException")
    void ShouldThrowIOExceptionUponRequestReaderError() throws Exception {
        //Given
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getMethod()).thenReturn(HttpMethod.POST.name());
        when(req.getContentType()).thenReturn(MediaType.APPLICATION_JSON_VALUE);
        when(req.getReader()).thenThrow(IOException.class);

        //When/Then
        assertThrows(AuthenticationServiceException.class, () -> filter.attemptAuthentication(req, resp));
    }

    private MockHttpServletRequest populateMockRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setMethod(HttpMethod.POST.name());
        return request;
    }
}