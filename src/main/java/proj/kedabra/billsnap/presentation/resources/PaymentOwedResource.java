package proj.kedabra.billsnap.presentation.resources;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class PaymentOwedResource implements Serializable {

    private static final long serialVersionUID = 8297193114531722626L;

    @ApiModelProperty(name = "account email")
    private String email;

    @ApiModelProperty(name = "amount owed")
    private BigDecimal amount;

}
