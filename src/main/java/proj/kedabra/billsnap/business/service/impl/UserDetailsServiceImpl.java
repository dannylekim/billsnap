package proj.kedabra.billsnap.business.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import proj.kedabra.billsnap.model.entities.Account;
import proj.kedabra.billsnap.business.repository.AccountRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Autowired
    public UserDetailsServiceImpl(final AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email){
        Optional<Account> optionalUser = Optional.ofNullable(accountRepository.getAccountByEmail(email));
        return toUserDetails(optionalUser.orElseThrow(
                () -> new UsernameNotFoundException(String.format("No user found with email '%s'.", email))));
    }

    private UserDetails toUserDetails(Account user) {
        return User.withUsername(user.getEmail())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }

}
