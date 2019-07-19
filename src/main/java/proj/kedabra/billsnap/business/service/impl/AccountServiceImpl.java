package proj.kedabra.billsnap.business.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import proj.kedabra.billsnap.business.dto.AccountDTO;
import proj.kedabra.billsnap.business.entities.Account;
import proj.kedabra.billsnap.business.mapper.AccountMapper;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.business.service.AccountService;
import proj.kedabra.billsnap.business.utils.enums.AccountStatusEnum;
import proj.kedabra.billsnap.utils.annotations.ObfuscateArgs;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    private final AccountMapper mapper;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AccountServiceImpl(final AccountRepository accountRepository, final AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.mapper = accountMapper;
        //We could also use DelegatingPasswordFactories.createDelegating... but that's mostly about having multiple encodings + migrations, which we do not care for
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    @ObfuscateArgs
    public AccountDTO registerAccount(final AccountDTO accountDTO) {

        if (accountRepository.existsAccountByEmail(accountDTO.getEmail())) {
            throw new IllegalArgumentException("This email already exists in the database.");
        }

        Account newAccount = mapper.toEntity(accountDTO);
        newAccount.setPassword(passwordEncoder.encode(newAccount.getPassword()));
        newAccount.setStatus(AccountStatusEnum.REGISTERED);
        return mapper.toDTO(accountRepository.save(newAccount));

    }
}
