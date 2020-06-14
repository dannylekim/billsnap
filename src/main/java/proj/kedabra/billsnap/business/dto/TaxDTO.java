package proj.kedabra.billsnap.business.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class TaxDTO {

    private String name;

    private BigDecimal percentage;

}
