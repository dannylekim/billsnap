package proj.kedabra.billsnap.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.micrometer.core.instrument.util.StringUtils;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthorizationFilter.class);

    private JwtUtil jwtUtil;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        super(authenticationManager);
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {
        var authentication = getAuthentication(request);
        if (authentication == null) {
            filterChain.doFilter(request, response);
        }
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        var token = request.getHeader(SecurityConstants.TOKEN_HEADER);
        if (StringUtils.isNotEmpty(token) && token.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            try {
                var parsedToken = jwtUtil.parseToken(token);

                var username = jwtUtil.getJwtUsername(parsedToken);

                var authorities = jwtUtil.getJwtAuthorities(parsedToken);

                if (StringUtils.isNotEmpty(username)) {
                    return new UsernamePasswordAuthenticationToken(username, null, authorities);
                }
            } catch (ExpiredJwtException ex) {
                log.warn("Request to parse expired JWT: {} failed: {}", token, ex.getMessage());
            } catch (UnsupportedJwtException ex) {
                log.warn("Request to parse unsupported JWT: {} failed: {}", token, ex.getMessage());
            } catch (MalformedJwtException ex) {
                log.warn("Request to parse invalid JWT: {} failed: {}", token, ex.getMessage());
            } catch (IllegalArgumentException ex) {
                log.warn("Request to parse empty or null JWT: {} failed: {}", token, ex.getMessage());
            } catch (SignatureException ex) {
                log.warn("Request to parse JWT with invalid signature: {} failed: {}", token, ex.getMessage());
            }
        }
        return null;
    }
}
