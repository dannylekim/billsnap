package proj.kedabra.billsnap.presentation.resources;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class PaymentsOwedRessource implements Serializable {

    private static final long serialVersionUID = 8297193114531722626L;

    @ApiModelProperty(name = "List of amount owed")
    private Map<String, BigDecimal> amountOwedMap;

}
