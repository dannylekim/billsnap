package proj.kedabra.billsnap.business.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

import proj.kedabra.billsnap.business.utils.enums.InvitationStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.PaymentStatusEnum;

@AllArgsConstructor
@Data
public class DetailedAccountBillInformation {

    private BigDecimal cost;

    private List<ItemPercentageSplitDTO> itemList;

    private InvitationStatusEnum invitationStatus;

    private PaymentStatusEnum paidStatus;
}
