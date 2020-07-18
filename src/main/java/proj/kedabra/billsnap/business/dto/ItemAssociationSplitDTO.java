package proj.kedabra.billsnap.business.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

import proj.kedabra.billsnap.business.utils.enums.InvitationStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.PaymentStatusEnum;

@Data
public class ItemAssociationSplitDTO {

    private AccountDTO account;

    private List<ItemPercentageSplitDTO> items = new ArrayList<>();

    private BigDecimal subTotal = BigDecimal.ZERO;

    private BigDecimal tip = BigDecimal.ZERO;

    private BigDecimal taxes = BigDecimal.ZERO;

    private BigDecimal amountPaid = BigDecimal.ZERO;

    private BigDecimal amountRemaining = BigDecimal.ZERO;

    private InvitationStatusEnum invitationStatus;

    private PaymentStatusEnum paidStatus;

}
