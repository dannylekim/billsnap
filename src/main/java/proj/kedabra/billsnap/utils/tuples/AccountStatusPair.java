package proj.kedabra.billsnap.utils.tuples;

import lombok.AllArgsConstructor;
import lombok.Data;

import proj.kedabra.billsnap.business.dto.AccountDTO;
import proj.kedabra.billsnap.business.utils.enums.InvitationStatusEnum;

@AllArgsConstructor
@Data
public class AccountStatusPair {

    private AccountDTO account;

    private InvitationStatusEnum status;
}