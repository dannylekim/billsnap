package proj.kedabra.billsnap.business.service;

import proj.kedabra.billsnap.business.dto.AccountDTO;
import proj.kedabra.billsnap.presentation.resources.AccountCreationResource;

public interface AccountService {

    AccountDTO registerAccount(AccountCreationResource accountCreationResource);

}
