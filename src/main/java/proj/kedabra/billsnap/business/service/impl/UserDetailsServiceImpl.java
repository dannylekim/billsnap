package proj.kedabra.billsnap.business.service.impl;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.model.entities.AccountBill;
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
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        final Optional<Account> optionalUser = Optional.ofNullable(accountRepository.getAccountByEmail(email));
        return optionalUser.map(this::toUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException(ErrorMessageEnum.NO_USER_FOUND_WITH_EMAIL.getMessage(email)));

    }

    private UserDetails toUserDetails(Account user) {

        final var authorities = new ArrayList<GrantedAuthority>();

        user.getBills().stream().map(AccountBill::getBill).forEach(bill -> {
            //for all bills where the user is responsible, we set them to have the authority prefix: RESPONSIBLE_
            if (bill.getResponsible().getEmail().equals(user.getEmail())) {
                authorities.add(new SimpleGrantedAuthority("RESPONSIBLE_" + bill.getId()));
            }

            //all other participants of the bill will simply get the billId in their authority
            authorities.add(new SimpleGrantedAuthority(bill.getId().toString()));
        });

        user.getNotifications().forEach(notification ->
                authorities.add(new SimpleGrantedAuthority("INVITATION_" + notification.getId()))
        );

        return User.withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }

}
