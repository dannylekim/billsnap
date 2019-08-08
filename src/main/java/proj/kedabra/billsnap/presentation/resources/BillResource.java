package proj.kedabra.billsnap.presentation.resources;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;

@Data
public class BillResource {

    @ApiModelProperty(name = "Name of the bill")
    private String name;

    @ApiModelProperty(name = "User that created the bill", position = 1)
    private AccountResource creator;

    @ApiModelProperty(name = "User that is responsible for the bill", position = 2)
    private AccountResource responsible;

    @ApiModelProperty(name = "status of the bill", position = 3)
    private BillStatusEnum status;

    @ApiModelProperty(name = "The company this bill is associated with", position = 4)
    private String company;

    @ApiModelProperty(name = "The category of the bill", position = 5)
    private String category;

    @ApiModelProperty(name = "Time that the bill is created", position = 6)
    private LocalDateTime created;

    @ApiModelProperty(name = "Time that the bill is updated", position = 7)
    private LocalDateTime updated;

    @ApiModelProperty(name = "List of items that were on the bill", position = 8)
    private List<ItemResource> items;

    @ApiModelProperty(name = "the total amount of the bill", position = 9)
    private BigDecimal balance;

}
