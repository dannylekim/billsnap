package proj.kedabra.billsnap.fixtures;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class UserFixture {

    private UserFixture() {}

    public static User getDefault() {
        String email = "user@user.com";
        String role = "ROLE_USER";
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
        return new User(email, "somepass", authorities);
    }
}
