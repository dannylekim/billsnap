package proj.kedabra.billsnap.business.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CostItemsPair {

    private BigDecimal cost;

    private List<ItemPercentageSplitDTO> itemList;
}
