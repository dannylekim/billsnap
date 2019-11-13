package proj.kedabra.billsnap.business.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ItemDTO {

    private Long id;

    private String name;

    private BigDecimal cost;
}
