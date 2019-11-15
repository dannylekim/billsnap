package proj.kedabra.billsnap.presentation.resources;

import lombok.Data;

import proj.kedabra.billsnap.business.utils.enums.InvitationStatusEnum;

@Data
public class AccountStatusResource {

    private AccountResource account;

    private InvitationStatusEnum status;
}
