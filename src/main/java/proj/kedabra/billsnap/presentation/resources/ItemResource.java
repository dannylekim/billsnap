package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.math.BigDecimal;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ItemResource implements Serializable {

    private static final long serialVersionUID = -7613044043746246657L;

    @ApiModelProperty(name = "Id of the item")
    private Long id;

    @ApiModelProperty(name = "Name of the item", position = 1)
    private String name;

    @ApiModelProperty(name = "Cost of the item", position = 2)
    private BigDecimal cost;

}
