package proj.kedabra.billsnap.business.facade.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.dto.AccountDTO;
import proj.kedabra.billsnap.business.facade.AccountFacade;
import proj.kedabra.billsnap.business.mapper.AccountMapper;
import proj.kedabra.billsnap.business.service.AccountService;
import proj.kedabra.billsnap.utils.annotations.ObfuscateArgs;

@Service
public class AccountFacadeImpl implements AccountFacade {

    private final AccountService accountService;

    private final AccountMapper accountMapper;

    @Autowired
    public AccountFacadeImpl(final AccountService accountService, final AccountMapper accountMapper) {
        this.accountService = accountService;
        this.accountMapper = accountMapper;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @ObfuscateArgs
    public AccountDTO registerAccount(final AccountDTO accountDTO) {
        return accountMapper.toDTO(accountService.registerAccount(accountDTO));
    }
}
