package proj.kedabra.billsnap.security;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private JwtService jwtService;

    private static final String TOKEN_HEADER = "Authorization";

    private static final String TOKEN_PREFIX = "Bearer ";

    private static final String EXPIRED_TOKEN_LOG = "Request to parse expired JWT: {} failed: {}";

    private static final String UNSUPPORTED_TOKEN_LOG = "Request to parse unsupported JWT: {} failed: {}";

    private static final String MALFORMED_TOKEN_LOG = "Request to parse invalid JWT: {} failed: {}";

    private static final String EMPTY_TOKEN_LOG = "Request to parse empty JWT: {} failed: {}";

    private static final String INVALID_SIGNATURE_TOKEN_LOG = "Request to parse JWT with invalid signature: {} failed: {}";

    private static final String INVALID_AUTHORIZATION_HEADER_LOG = "JWT is empty or does not start with 'Bearer ' in Authorization header";

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, JwtService jwtService) {
        super(authenticationManager);
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {
        Optional<UsernamePasswordAuthenticationToken> authentication = getAuthentication(request);
        if (authentication.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }
        SecurityContextHolder.getContext().setAuthentication(authentication.get());
        filterChain.doFilter(request, response);
    }

    private Optional<UsernamePasswordAuthenticationToken> getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(TOKEN_HEADER);
        if (StringUtils.isNotEmpty(token) && token.startsWith(TOKEN_PREFIX)) {
            try {
                String username = jwtService.getJwtUsername(token);
                Collection<GrantedAuthority> authorities = jwtService.getJwtAuthorities(token);

                if (StringUtils.isNotEmpty(username)) {
                    return Optional.of(new UsernamePasswordAuthenticationToken(username, null, authorities));
                }
            } catch (ExpiredJwtException ex) {
                log.warn(EXPIRED_TOKEN_LOG, token, ex.getMessage());
            } catch (UnsupportedJwtException ex) {
                log.warn(UNSUPPORTED_TOKEN_LOG, token, ex.getMessage());
            } catch (MalformedJwtException ex) {
                log.warn(MALFORMED_TOKEN_LOG, token, ex.getMessage());
            } catch (IllegalArgumentException ex) {
                log.warn(EMPTY_TOKEN_LOG, token, ex.getMessage());
            } catch (SignatureException ex) {
                log.warn(INVALID_SIGNATURE_TOKEN_LOG, token, ex.getMessage());
            }
        } else {
            log.warn(INVALID_AUTHORIZATION_HEADER_LOG);
            return Optional.empty();
        }
        return Optional.empty();
    }
}
