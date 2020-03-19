package proj.kedabra.billsnap.security;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import proj.kedabra.billsnap.presentation.resources.LoginResponseResource;

@Service
public class JwtService implements Serializable {

    private static final long serialVersionUID = 5249861017865007332L;

    private final String jwtSecret;

    private final Long jwtExpiration;

    private final ObjectMapper mapper;

    private static final String LOGIN_SUCCESS_MESSAGE = "Successfully logged in";

    private static final String TOKEN_TYPE = "JWT";

    private static final String ROLES = "roles";

    @Autowired
    public JwtService(ObjectMapper mapper, @Value("${jwt.secret}") String jwtSecret, @Value("${jwt.expiration}") Long jwtExpiration) {
        this.mapper = mapper;
        this.jwtSecret = jwtSecret;
        this.jwtExpiration = jwtExpiration;
    }

    public String generateToken(User user) {
        List<String> roles = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        byte[] signingKey = jwtSecret.getBytes(UTF_8);

        return Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(signingKey), SignatureAlgorithm.HS512)
                .setHeaderParam("alg", SignatureAlgorithm.HS512)
                .setHeaderParam("typ", TOKEN_TYPE)
                .setSubject(user.getUsername())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .claim(ROLES, roles)
                .compact();
    }

    public JwsHeader getJwtHeaders(String token) {
        byte[] signingKey = jwtSecret.getBytes(UTF_8);
        return Jwts.parser()
                .setSigningKey(signingKey)
                .parseClaimsJws(token.replace("Bearer ", ""))
                .getHeader();
    }

    public String getJwtUsername(String token) {
        return getJwtBody(token).getSubject();
    }

    public Collection<GrantedAuthority> getJwtAuthorities(String token) {
        return ((List<?>) getJwtBody(token).get(ROLES))
                .stream()
                .map(String.class::cast)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    String loginSuccessJson(String token, String firstname, String lastname) throws IOException {
        try {
            final LoginResponseResource loginResponseResource = new LoginResponseResource();
            loginResponseResource.setMessage(LOGIN_SUCCESS_MESSAGE);
            loginResponseResource.setToken(token);
            loginResponseResource.setFirstName(firstname);
            loginResponseResource.setLastName(lastname);

            return mapper.writeValueAsString(loginResponseResource);
        } catch (JsonProcessingException e) {
            throw new IOException(e);
        }
    }

    private Claims getJwtBody(String token) {
        byte[] signingKey = jwtSecret.getBytes(UTF_8);
        return Jwts.parser()
                .setSigningKey(signingKey)
                .parseClaimsJws(token.replace("Bearer ", ""))
                .getBody();
    }
}
