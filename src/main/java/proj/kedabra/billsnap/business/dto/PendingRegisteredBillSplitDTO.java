package proj.kedabra.billsnap.business.dto;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PendingRegisteredBillSplitDTO extends BillSplitDTO {

    private List<String> pendingAccounts;

}
