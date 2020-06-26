package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PaymentOwedResource implements Serializable {

    @Schema(description = "account email")
    private String email;

    @Schema(description = "amount owed")
    private BigDecimal amount;

}
