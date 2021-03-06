package proj.kedabra.billsnap.fixtures;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

public final class UserFixture {

    private UserFixture() {}

    public static User getDefault() {
        return getDefaultWithEmailAndPassword("user@user.com", "somepass");
    }

    public static User getDefaultWithEmailAndPassword(final String email, final String password) {
        final String role = "ROLE_USER";
        final List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
        return new User(email, password, authorities);
    }
}
