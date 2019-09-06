package proj.kedabra.billsnap.business.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ItemPercentageDTO {

    private Long itemId;

    private BigDecimal percentage;
}
