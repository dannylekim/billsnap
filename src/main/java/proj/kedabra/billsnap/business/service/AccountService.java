package proj.kedabra.billsnap.business.service;

import java.util.List;

import proj.kedabra.billsnap.business.dto.AccountDTO;
import proj.kedabra.billsnap.business.dto.BaseAccountDTO;
import proj.kedabra.billsnap.business.model.entities.Account;

public interface AccountService {

    Account registerAccount(AccountDTO accountDTO);

    Account getAccount(String email);

    Account edit(String email, BaseAccountDTO editInfo);

    List<Account> getAccounts(List<String> emails);
}
