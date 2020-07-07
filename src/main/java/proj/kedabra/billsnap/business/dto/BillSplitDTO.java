package proj.kedabra.billsnap.business.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.SplitByEnum;

@Data
public class BillSplitDTO {

    private Long id;

    private String name;

    private String category;

    private String company;

    private AccountDTO creator;

    private AccountDTO responsible;

    private BillStatusEnum status;

    private ZonedDateTime created;

    private ZonedDateTime updated;

    private List<ItemAssociationSplitDTO> informationPerAccount;

    private SplitByEnum splitBy;

    private BigDecimal totalTip;

    private List<TaxDTO> taxes = new ArrayList<>();

    private BigDecimal balance;
}
