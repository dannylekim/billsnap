package proj.kedabra.billsnap.security;

import java.io.IOException;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import proj.kedabra.billsnap.business.exception.LoginValidationException;
import proj.kedabra.billsnap.presentation.resources.LoginResource;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    private final Validator validator;

    private final ObjectMapper mapper;

    private static final String AUTH_LOGIN_URL = "/login";

    private static final String TOKEN_HEADER = "Authorization";

    private static final String TOKEN_PREFIX = "Bearer ";

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil, Validator validator, ObjectMapper mapper) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.validator = validator;
        this.mapper = mapper;
        setFilterProcessesUrl(AUTH_LOGIN_URL);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Incorrect login request method.");
        }
        String username;
        String password;

        if (request.getContentType() == null || !request.getContentType().equals(MediaType.APPLICATION_JSON_VALUE)) {
            throw new AuthenticationServiceException("Login request input is not JSON content-type.");
        } else {
            try {
                String requestData = request.getReader().lines().collect(Collectors.joining());
                LoginResource loginAttempt = mapper.readValue(requestData, LoginResource.class);
                Errors errors = new BeanPropertyBindingResult(LoginResource.class, "Login Resource");
                validator.validate(loginAttempt, errors);

                if (errors.hasErrors()) {
                    throw new LoginValidationException(errors.getAllErrors());
                }
                username = loginAttempt.getEmail();
                password = loginAttempt.getPassword();

            } catch (IOException e) {
                throw new AuthenticationServiceException(e.getMessage());
            }
        }
        var authenticationToken = new UsernamePasswordAuthenticationToken(username, password);

        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain filterChain, Authentication authResult) throws IOException {
        User user = ((User) authResult.getPrincipal());

        String token = jwtUtil.generateToken(user);

        response.addHeader(TOKEN_HEADER, TOKEN_PREFIX + token);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(jwtUtil.loginSuccessJson(token));
    }
}
