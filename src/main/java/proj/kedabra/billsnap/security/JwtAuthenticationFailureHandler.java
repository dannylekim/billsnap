package proj.kedabra.billsnap.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import proj.kedabra.billsnap.business.exception.LoginValidationException;
import proj.kedabra.billsnap.presentation.ApiError;

public class JwtAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper mapper;

    public JwtAuthenticationFailureHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {

        if (exception instanceof LoginValidationException) {
            ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, exception.getMessage(), ((LoginValidationException) exception).getErrorsList().getAllErrors());
            writeToHttpResponse(apiError, response);
        }
        else if (exception instanceof AuthenticationServiceException) {
            ApiError apiError = new ApiError(HttpStatus.FORBIDDEN, exception.getMessage());
            writeToHttpResponse(apiError, response);
        }
        else if (exception instanceof BadCredentialsException){
            ApiError apiError = new ApiError(HttpStatus.UNAUTHORIZED, "Username or password is incorrect.");
            writeToHttpResponse(apiError, response);
        }
        else {
            ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Server error has occurred, please try again later.");
            writeToHttpResponse(apiError, response);
        }
    }

    private void writeToHttpResponse(ApiError apiError, HttpServletResponse response) throws IOException{
        response.setStatus(apiError.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(mapper.writeValueAsString(apiError));
    }
}
