package proj.kedabra.billsnap.business.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaxDTO {

    private Long id;

    private String name;

    private BigDecimal percentage;

}
