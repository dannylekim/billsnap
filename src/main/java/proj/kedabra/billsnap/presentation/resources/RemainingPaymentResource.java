package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.math.BigDecimal;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RemainingPaymentResource implements Serializable {

    private static final long serialVersionUID = -1903268471910086835L;

    @ApiModelProperty(name = "Remaining amount left to pay towards the bill")
    private BigDecimal remainingBalance;
}
