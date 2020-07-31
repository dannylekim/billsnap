package proj.kedabra.billsnap.business.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;

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
                () -> new UsernameNotFoundException(ErrorMessageEnum.NO_USER_FOUND_WITH_EMAIL.getMessage(email))));
    }

    private UserDetails toUserDetails(Account user) {
        return User.withUsername(user.getEmail())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }

}
