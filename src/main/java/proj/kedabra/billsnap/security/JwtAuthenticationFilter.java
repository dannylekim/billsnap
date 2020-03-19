package proj.kedabra.billsnap.security;

import java.io.IOException;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpMethod;
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

import lombok.extern.slf4j.Slf4j;

import proj.kedabra.billsnap.business.exception.LoginValidationException;
import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.service.AccountService;
import proj.kedabra.billsnap.business.service.impl.AccountServiceImpl;
import proj.kedabra.billsnap.presentation.resources.LoginResource;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;

@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    private final AccountService accountService;

    private final Validator validator;

    private final ObjectMapper mapper;

    private static final String AUTH_LOGIN_URL = "/login";

    private static final String TOKEN_HEADER = "Authorization";

    private static final String TOKEN_PREFIX = "Bearer ";

    public JwtAuthenticationFilter(
            final AuthenticationManager authenticationManager,
            final JwtService jwtService,
            final Validator validator,
            final ObjectMapper mapper,
            final AccountServiceImpl accountService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.accountService = accountService;
        this.validator = validator;
        this.mapper = mapper;
        setFilterProcessesUrl(AUTH_LOGIN_URL);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        validateRequestDetails(request);

        String username;
        String password;
        try {
            String requestData = request.getReader().lines().collect(Collectors.joining());
            LoginResource loginAttempt = mapper.readValue(requestData, LoginResource.class);
            Errors errors = new BeanPropertyBindingResult(LoginResource.class, LoginResource.class.getSimpleName());
            validator.validate(loginAttempt, errors);

            if (errors.hasErrors()) {
                log.warn("Error at Login validation check.", new LoginValidationException(errors.getAllErrors()));
                throw new LoginValidationException(errors.getAllErrors());
            }
            username = loginAttempt.getEmail();
            password = loginAttempt.getPassword();

        } catch (IOException e) {
            log.error("Error when processing login request content.", new AuthenticationServiceException(e.getMessage()));
            throw new AuthenticationServiceException(e.getMessage());
        }

        var authenticationToken = new UsernamePasswordAuthenticationToken(username, password);

        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain filterChain, Authentication authResult) throws IOException {
        User user = ((User) authResult.getPrincipal());

        String token = jwtService.generateToken(user);
        Account account = accountService.getAccount(user.getUsername());

        response.addHeader(TOKEN_HEADER, TOKEN_PREFIX + token);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(jwtService.loginSuccessJson(token, account.getFirstName(), account.getLastName()));
    }

    private void validateRequestDetails(HttpServletRequest request) {
        if (!request.getMethod().equals(HttpMethod.POST.name())) {
            final var ex = new AuthenticationServiceException(ErrorMessageEnum.INCORRECT_LOGIN_METHOD.getMessage());
            log.warn("Error at Login POST method check.", ex);
            throw ex;
        }
      
        final var contentType = request.getContentType();
        if (contentType == null || !contentType.equals(MediaType.APPLICATION_JSON_VALUE)) {
            final var ex = new AuthenticationServiceException(ErrorMessageEnum.MEDIA_TYPE_NOT_JSON.getMessage());
            log.warn("Error at Login request content-type check.", ex);
            throw ex;
        }
    }
}
