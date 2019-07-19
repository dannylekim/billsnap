package proj.kedabra.billsnap.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

public class JwtAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        // Empty for now as we do not re-direct users to a page.
        // Authentication success is first handled in JwtAuthenticationFilter's successfulAuthentication(...)
        // then delegated to this success handler for additional handling.
    }

}

