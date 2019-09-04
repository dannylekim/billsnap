package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import proj.kedabra.billsnap.business.entities.Account;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;

@Data
public class BillResource implements Serializable {

    private static final long serialVersionUID = 8297193114531722626L;

    @ApiModelProperty(name = "Id of the bill")
    private Long id;

    @ApiModelProperty(name = "Name of the bill", position = 1)
    private String name;

    @ApiModelProperty(name = "User that created the bill", position = 2)
    private AccountResource creator;

    @ApiModelProperty(name = "User that is responsible for the bill", position = 3)
    private AccountResource responsible;

    @ApiModelProperty(name = "status of the bill", position = 4)
    private BillStatusEnum status;

    @ApiModelProperty(name = "The company this bill is associated with", position = 5)
    private String company;

    @ApiModelProperty(name = "The category of the bill", position = 6)
    private String category;

    @ApiModelProperty(name = "Time that the bill is created", position = 7)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss Z")
    private ZonedDateTime created;

    @ApiModelProperty(name = "Time that the bill is updated", position = 8)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss Z")
    private ZonedDateTime updated;

    @ApiModelProperty(name = "List of items that were on the bill", position = 9)
    private List<ItemResource> items;

    @ApiModelProperty(name = "List of accounts associated to the bill", position = 10)
    private List<Account> accountsEntityList;

    @ApiModelProperty(name = "the total amount of the bill", position = 11)
    private BigDecimal balance;

}
