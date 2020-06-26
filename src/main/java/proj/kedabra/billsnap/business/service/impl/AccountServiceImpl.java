package proj.kedabra.billsnap.business.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import proj.kedabra.billsnap.business.dto.AccountDTO;
import proj.kedabra.billsnap.business.exception.ResourceNotFoundException;
import proj.kedabra.billsnap.business.mapper.AccountMapper;
import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.business.service.AccountService;
import proj.kedabra.billsnap.business.utils.enums.AccountStatusEnum;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;
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
    public Account registerAccount(final AccountDTO accountDTO) {

        if (accountRepository.existsAccountByEmail(accountDTO.getEmail())) {
            throw new IllegalArgumentException(ErrorMessageEnum.EMAIL_ALREADY_EXISTS.getMessage());
        }

        Account newAccount = mapper.toEntity(accountDTO);
        newAccount.setPassword(passwordEncoder.encode(newAccount.getPassword()));
        newAccount.setStatus(AccountStatusEnum.REGISTERED);
        return accountRepository.save(newAccount);
    }

    @Override
    public Account getAccount(String email) {
        return Optional.ofNullable(accountRepository.getAccountByEmail(email))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessageEnum.ACCOUNT_DOES_NOT_EXIST.getMessage()));
    }

    @Override
    public List<Account> getAccounts(final List<String> emails) {
        final List<Account> accountsList = accountRepository.getAccountsByEmailIn(emails).collect(Collectors.toList());

        if (emails.size() > accountsList.size()) {
            final List<String> nonExistentEmails = new ArrayList<>(emails);
            final List<String> accountsStringList = accountsList.stream().map(Account::getEmail).collect(Collectors.toList());
            nonExistentEmails.removeAll(accountsStringList);
            throw new ResourceNotFoundException(ErrorMessageEnum.LIST_ACCOUNT_DOES_NOT_EXIST.getMessage(nonExistentEmails.toString()));
        }

        return accountsList;
    }

}
