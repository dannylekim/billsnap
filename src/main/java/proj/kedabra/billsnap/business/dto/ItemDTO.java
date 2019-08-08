package proj.kedabra.billsnap.business.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ItemDTO {

    private String name;

    private BigDecimal cost;
}
