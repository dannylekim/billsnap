package proj.kedabra.billsnap.business.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemPercentageSplitDTO extends ItemPercentageDTO {

    private String name;

    private BigDecimal cost;

}
