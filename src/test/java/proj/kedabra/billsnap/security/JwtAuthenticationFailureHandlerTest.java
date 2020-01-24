package proj.kedabra.billsnap.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import proj.kedabra.billsnap.business.exception.LoginValidationException;
import proj.kedabra.billsnap.presentation.ApiError;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;

class JwtAuthenticationFailureHandlerTest {

    private JwtAuthenticationFailureHandler failureHandler;

    private final ObjectMapper mapper = new ObjectMapper();

    private final String MUST_NOT_BE_BLANK = "must not be blank";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        failureHandler = new JwtAuthenticationFailureHandler(mapper);
    }

    @Test
    @DisplayName("Handler response for LoginValidationException should have list of errors and status 400")
    void LoginValidationExceptionResponseShouldHaveListOfErrors() throws Exception {
        //Given
        HttpServletRequest req = mock(HttpServletRequest.class);
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        List<ObjectError> errors = List.of(new FieldError("BlankError", "username", "", false, new String[0], new Object[0], MUST_NOT_BE_BLANK));
        LoginValidationException ex = new LoginValidationException(errors);

        //When
        failureHandler.onAuthenticationFailure(req, mockResponse, ex);

        //Then
        ApiError error = convertContentToApiError(mockResponse.getContentAsString());

        assertEquals(MediaType.APPLICATION_JSON_VALUE, mockResponse.getContentType());
        assertEquals(ErrorMessageEnum.INVALID_LOGIN_INPUTS.getMessage(), error.getMessage());
        assertEquals(1, error.getErrors().size());
        assertEquals(MUST_NOT_BE_BLANK, error.getErrors().get(0).getMessage());
        assertEquals("", error.getErrors().get(0).getRejectedValue());
        assertEquals("username", error.getErrors().get(0).getField());
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, mockResponse.getStatus());
    }

    @Test
    @DisplayName("Handler response for AuthenticationServiceException should have error message and status 403")
    void AuthenticationServiceExceptionResponseShouldHaveErrorMessage() throws Exception {
        //Given
        HttpServletRequest req = mock(HttpServletRequest.class);
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        AuthenticationServiceException ex = new AuthenticationServiceException(ErrorMessageEnum.WRONG_REQ_METHOD.getMessage());

        //When
        failureHandler.onAuthenticationFailure(req, mockResponse, ex);

        //Then
        ApiError error = convertContentToApiError(mockResponse.getContentAsString());

        assertEquals(MediaType.APPLICATION_JSON_VALUE, mockResponse.getContentType());
        assertEquals(ErrorMessageEnum.WRONG_REQ_METHOD.getMessage(), error.getMessage());
        assertEquals(0, error.getErrors().size());
        assertEquals(HttpServletResponse.SC_FORBIDDEN, mockResponse.getStatus());
    }

    @Test
    @DisplayName("Handler response for BadCredentialsException should have error message and status 401")
    void BadCredentialsExceptionResponseShouldHaveErrorMessage() throws Exception {
        //Given
        HttpServletRequest req = mock(HttpServletRequest.class);
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        BadCredentialsException ex = new BadCredentialsException("");

        //When
        failureHandler.onAuthenticationFailure(req, mockResponse, ex);

        //Then
        ApiError error = convertContentToApiError(mockResponse.getContentAsString());

        assertEquals(MediaType.APPLICATION_JSON_VALUE, mockResponse.getContentType());
        assertEquals(ErrorMessageEnum.BAD_CREDENTIALS.getMessage(), error.getMessage());
        assertEquals(0, error.getErrors().size());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, mockResponse.getStatus());
    }

    @Test
    @DisplayName("Handler response for explicitly unhandled exception should have error message and status 500")
    void ExplicitlyUnhandledExceptionResponseShouldHaveErrorMessage() throws Exception {
        //Given
        HttpServletRequest req = mock(HttpServletRequest.class);
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        SessionAuthenticationException ex = new SessionAuthenticationException("");

        //When
        failureHandler.onAuthenticationFailure(req, mockResponse, ex);

        //Then
        ApiError error = convertContentToApiError(mockResponse.getContentAsString());

        assertEquals(MediaType.APPLICATION_JSON_VALUE, mockResponse.getContentType());
        assertEquals(ErrorMessageEnum.INTERNAL_SERVER_ERROR.getMessage(), error.getMessage());
        assertEquals(0, error.getErrors().size());
        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, mockResponse.getStatus());
    }

    private ApiError convertContentToApiError(String content) throws Exception {
        String formattedTimestamp = LocalDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

        JsonNode node = mapper.readTree(content);
        ((ObjectNode) node).put("timestamp", formattedTimestamp);
        String modifiedContent = node.toString();
        return mapper.readValue(modifiedContent, ApiError.class);
    }
}