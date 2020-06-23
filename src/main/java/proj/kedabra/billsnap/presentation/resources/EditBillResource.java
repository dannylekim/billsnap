package proj.kedabra.billsnap.presentation.resources;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.constraints.Digits;

import org.hibernate.validator.constraints.Range;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditBillResource {

    @ApiModelProperty(name = "Name of the bill", position = 1)
    private String name;

    @ApiModelProperty(name = "User that is responsible for the bill", position = 2)
    private String responsible;

    @ApiModelProperty(name = "The company this bill is associated with", position = 3)
    private String company;

    @ApiModelProperty(name = "The category of the bill", position = 4)
    private String category;

    @ApiModelProperty(name = "The tip amount. Only one of tipAmount or tipPercent is allowed", position = 5)
    @Digits(integer = 12, fraction = 2)
    @Range(message = "the number must be positive")
    private BigDecimal tipAmount;

    @ApiModelProperty(name = "The tip percent. Only one of tipAmount or tipPercent is allowed", position = 6)
    @Digits(integer = 3, fraction = 4)
    @Range(message = "the number must be positive")
    private BigDecimal tipPercent;

    @ApiModelProperty(name = "List of items to add in bill", position = 7)
    private List<ItemResource> items;
}
