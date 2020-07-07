package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;

import lombok.Data;

import proj.kedabra.billsnap.business.utils.enums.InvitationStatusEnum;

@Data
public class AccountStatusResource implements Serializable {

    private AccountResource account;

    private InvitationStatusEnum status;
}
