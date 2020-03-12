package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.math.BigDecimal;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;

@Data
public class ShortBillResource implements Serializable {

    private static final long serialVersionUID = 51325686923749077L;

    @ApiModelProperty(name = "Id of the bill")
    private Long id;

    @ApiModelProperty(name = "Name of the bill", position = 1)
    private String name;

    @ApiModelProperty(name = "status of the bill", position = 2)
    private BillStatusEnum status;

    @ApiModelProperty(name = "The category of the bill", position = 3)
    private String category;

    @ApiModelProperty(name = "the total amount of the bill", position = 4)
    private BigDecimal balance;

}
