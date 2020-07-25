package proj.kedabra.billsnap.business.facade;

import proj.kedabra.billsnap.business.dto.AccountDTO;
import proj.kedabra.billsnap.business.dto.BaseAccountDTO;

public interface AccountFacade {

    AccountDTO registerAccount(AccountDTO accountDTO);

    AccountDTO getAccount(String email);

    AccountDTO edit(String email, BaseAccountDTO editAccount);

}
