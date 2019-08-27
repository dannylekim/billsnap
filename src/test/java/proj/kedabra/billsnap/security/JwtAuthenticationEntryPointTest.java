package proj.kedabra.billsnap.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.CredentialsExpiredException;

import proj.kedabra.billsnap.presentation.ApiError;

class JwtAuthenticationEntryPointTest {

    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private final ObjectMapper mapper = new ObjectMapper();

    private static final String ACCESS_UNAUTHORIZED = "Access is unauthorized!";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        jwtAuthenticationEntryPoint = new JwtAuthenticationEntryPoint(mapper);
    }

    @Test
    @DisplayName("Should return 401 Unauthorized Error in Response")
    void ResponseShouldHaveUnauthorizedError() throws Exception {
        //Given
        HttpServletRequest req = mock(HttpServletRequest.class);
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        CredentialsExpiredException exception = new CredentialsExpiredException("");

        //When
        jwtAuthenticationEntryPoint.commence(req, mockResponse, exception);

        //Then
        ApiError error = convertContentToApiError(mockResponse.getContentAsString());
        assertEquals(ACCESS_UNAUTHORIZED, error.getMessage());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, mockResponse.getStatus());
    }

    private ApiError convertContentToApiError(String content) throws Exception {
        String formattedTimestamp = LocalDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

        JsonNode node = mapper.readTree(content);
        ((ObjectNode) node).put("timestamp", formattedTimestamp);
        String modifiedContent = node.toString();
        return mapper.readValue(modifiedContent, ApiError.class);
    }
}