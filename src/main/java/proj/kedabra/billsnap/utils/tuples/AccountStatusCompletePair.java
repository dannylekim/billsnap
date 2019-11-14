package proj.kedabra.billsnap.utils.tuples;

import lombok.Data;

import proj.kedabra.billsnap.business.utils.enums.InvitationStatusEnum;
import proj.kedabra.billsnap.presentation.resources.AccountResource;

@Data
public class AccountStatusCompletePair {

    private AccountResource account;

    private InvitationStatusEnum status;
}
