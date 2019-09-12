package proj.kedabra.billsnap.business.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AmountsOwedDTO {

    private String email;

    private BigDecimal amountOwed;

}
