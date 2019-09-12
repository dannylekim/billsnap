package proj.kedabra.billsnap.presentation.resources;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class AmountsOwedRessource implements Serializable {

    private static final long serialVersionUID = 8297193114531722626L;

    @ApiModelProperty(name = "Account email")
    private String email;

    @ApiModelProperty(name = "Amount owed", position = 1)
    private BigDecimal amountOwed;

}
