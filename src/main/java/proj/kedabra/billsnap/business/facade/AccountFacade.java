package proj.kedabra.billsnap.business.facade;

import proj.kedabra.billsnap.business.dto.AccountDTO;
import proj.kedabra.billsnap.presentation.resources.AccountCreationResource;

public interface AccountFacade {

    AccountDTO registerAccount(AccountCreationResource accountCreationResource);

}
