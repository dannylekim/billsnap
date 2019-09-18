package proj.kedabra.billsnap.business.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class ItemAssociationSplitDTO {

    private AccountDTO account;

    private List<ItemPercentageSplitDTO> items;

    private BigDecimal cost;
}
