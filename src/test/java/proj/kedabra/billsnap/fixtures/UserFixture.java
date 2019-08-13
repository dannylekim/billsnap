package proj.kedabra.billsnap.fixtures;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class UserFixture {

    private UserFixture() {}

    public static User getDefault() {
        String email = "user@userdetails.com";
        String role = "ROLE_USER";
        List<GrantedAuthority> authorities = new ArrayList<>() {
            private static final long serialVersionUID = 821620366318873776L;

            {add(new SimpleGrantedAuthority(role));}
        };
        return new User(email, "pass", authorities);
    }
}
