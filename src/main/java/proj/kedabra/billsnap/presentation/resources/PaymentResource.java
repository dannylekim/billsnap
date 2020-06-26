package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PaymentResource implements Serializable {

    @Schema(description = "Id of the bill")
    @NotNull
    private Long id;

    @Schema(description = "Amount being paid towards the bill")
    @NotNull
    @DecimalMin(value = "0", message = "{amount.positiveOnly}", inclusive = false)
    @Digits(integer = 12, fraction = 2)
    private BigDecimal paymentAmount;

}
