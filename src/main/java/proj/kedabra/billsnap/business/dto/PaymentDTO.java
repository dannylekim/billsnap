package proj.kedabra.billsnap.business.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class PaymentDTO {

    private String email;

    private Long billId;

    private BigDecimal amount;
}
