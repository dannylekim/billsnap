package proj.kedabra.billsnap.config;

import javax.validation.Validation;
import javax.validation.Validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

import proj.kedabra.billsnap.business.service.impl.UserDetailsServiceImpl;
import proj.kedabra.billsnap.security.JwtAuthenticationFailureHandler;
import proj.kedabra.billsnap.security.JwtAuthenticationFilter;
import proj.kedabra.billsnap.security.JwtAuthenticationSuccessHandler;
import proj.kedabra.billsnap.security.JwtUtil;
import proj.kedabra.billsnap.security.LoginValidator;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsServiceImpl userDetailsServiceImpl;

    private final JwtUtil jwtUtil;

    private final ObjectMapper mapper;

    @Autowired
    public SecurityConfig(UserDetailsServiceImpl userDetailsServiceImpl, JwtUtil jwtUtil, ObjectMapper mapper) {
        this.userDetailsServiceImpl = userDetailsServiceImpl;
        this.jwtUtil = jwtUtil;
        this.mapper = mapper;
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsServiceImpl)
                .passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/**")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .addFilter(jwtAuthenticationFilter())
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .headers()
                .frameOptions()
                .sameOrigin()
        ;
    }

    private JwtAuthenticationFilter jwtAuthenticationFilter() throws Exception {
        Validator javaxValidator = Validation.buildDefaultValidatorFactory().getValidator();
        SpringValidatorAdapter adapter = new SpringValidatorAdapter(javaxValidator);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(authenticationManager(), jwtUtil, new LoginValidator(adapter), mapper);

        JwtAuthenticationSuccessHandler successHandler = new JwtAuthenticationSuccessHandler();
        JwtAuthenticationFailureHandler failureHandler = new JwtAuthenticationFailureHandler(mapper);
        filter.setAuthenticationSuccessHandler(successHandler);
        filter.setAuthenticationFailureHandler(failureHandler);

        return filter;
    }

    private PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
