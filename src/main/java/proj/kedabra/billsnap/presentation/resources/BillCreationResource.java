package proj.kedabra.billsnap.presentation.resources;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BillCreationResource {

    @NotBlank
    @ApiModelProperty(name = "Name of the bill")
    private String name;

    @ApiModelProperty(name = "The category of the bill", position = 1)
    private String category;

    @ApiModelProperty(name = "The company this bill is associated with", position = 2)
    private String company;

    @NotNull
    @ApiModelProperty(name = "List of items that were on the bill", position = 3)
    private List<ItemResource> items;

    @ApiModelProperty(name = "The tip amount. Only one of tipAmount or tipPercent is allowed", position = 4)
    private BigDecimal tipAmount;

    @ApiModelProperty(name = "The tip percent. Only one of tipAmount or tipPercent is allowed", position = 5)
    private BigDecimal tipPercent;
}
