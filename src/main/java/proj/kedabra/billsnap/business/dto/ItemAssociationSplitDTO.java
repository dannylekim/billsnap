package proj.kedabra.billsnap.business.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ItemAssociationSplitDTO {

    private AccountDTO account;

    private List<ItemPercentageSplitDTO> items;

    private BigDecimal cost;
}
