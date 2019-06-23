package proj.kedabra.billsnap.business.facade.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.dto.AccountDTO;
import proj.kedabra.billsnap.business.facade.AccountFacade;
import proj.kedabra.billsnap.business.service.AccountService;

@Service
public class AccountFacadeImpl implements AccountFacade {

    private final AccountService accountService;

    @Autowired
    public AccountFacadeImpl(AccountService accountService) {
        this.accountService = accountService;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccountDTO registerAccount(AccountDTO accountDTO) {
        return accountService.registerAccount(accountDTO);
    }
}
