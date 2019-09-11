package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BillCreationResource implements Serializable {

    private static final long serialVersionUID = -8297742670434288964L;

    @NotBlank
    @ApiModelProperty(name = "Name of the bill")
    @Size(max = 30)
    private String name;

    @ApiModelProperty(name = "The category of the bill", position = 1)
    @Size(max = 20)
    private String category;

    @ApiModelProperty(name = "The company this bill is associated with", position = 2)
    @Size(max = 20)
    private String company;

    @NotNull
    @ApiModelProperty(name = "List of items that were on the bill", position = 3)
    private List<@Valid ItemResource> items;

    @ApiModelProperty(name = "List of accounts to associate to the bill", position = 4)
    private List<
            @NotBlank
            @Email(message = "{email.emailFormat}")
            @Size(max = 50) String> accountsList;

    @ApiModelProperty(name = "The tip amount. Only one of tipAmount or tipPercent is allowed", position = 5)
    @Digits(integer = 12, fraction = 2)
    @Range(message = "the number must be positive")
    private BigDecimal tipAmount;

    @ApiModelProperty(name = "The tip percent. Only one of tipAmount or tipPercent is allowed", position = 6)
    @Digits(integer = 3, fraction = 4)
    @Range(message = "the number must be positive")
    private BigDecimal tipPercent;
}
