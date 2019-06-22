package proj.kedabra.billsnap.business.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.business.service.AccountService;
import proj.kedabra.billsnap.business.dto.AccountDTO;
import proj.kedabra.billsnap.presentation.resources.AccountCreationResource;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository){
        this.accountRepository = accountRepository;
    }

    @Override
    public AccountDTO registerAccount(AccountCreationResource accountCreationResource) {

        if(accountRepository.existsAccountByEmail(accountCreationResource.getEmail())){
            throw new IllegalArgumentException("This email already exists in the database");
        }

        return null;
    }
}
