package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class PaymentResource implements Serializable {

    private static final long serialVersionUID = 2371908843524318428L;

    @ApiModelProperty(name = "Id of the bill")
    @NotNull
    private Long id;

    @ApiModelProperty(name = "Amount being paid towards the bill", position = 1)
    @NotNull
    @DecimalMin(value = "0", message = "{amount.positiveOnly}", inclusive = false)
    @Digits(integer = 12, fraction = 2)
    private BigDecimal paymentAmount;

}
