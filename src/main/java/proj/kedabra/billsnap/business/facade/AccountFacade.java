package proj.kedabra.billsnap.business.facade;

import proj.kedabra.billsnap.business.dto.AccountDTO;

public interface AccountFacade {

    AccountDTO registerAccount(AccountDTO accountDTO);

    AccountDTO getAccount(String email);

}
