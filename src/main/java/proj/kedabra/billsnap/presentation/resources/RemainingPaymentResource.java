package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RemainingPaymentResource implements Serializable {


    @Schema(description = "Remaining amount left to pay towards the bill")
    private BigDecimal remainingBalance;
}
