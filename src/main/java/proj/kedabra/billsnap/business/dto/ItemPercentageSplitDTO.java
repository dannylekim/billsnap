package proj.kedabra.billsnap.business.dto;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ItemPercentageSplitDTO extends ItemPercentageDTO {

    private String name;

    private BigDecimal cost;

}
