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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class BillCreationResource implements Serializable {

    @NotBlank
    @Schema(description = "Name of the bill")
    @Size(max = 30)
    private String name;

    @Schema(description = "The category of the bill")
    @Size(max = 20)
    private String category;

    @Schema(description = "The company this bill is associated with")
    @Size(max = 20)
    private String company;

    @NotNull
    @Schema(description = "List of items that were on the bill")
    private List<@Valid ItemCreationResource> items;

    @Schema(description = "List of accounts to associate to the bill")
    private List<
            @NotBlank
            @Email(message = "{email.emailFormat}")
            @Size(max = 50) String> accountsList;

    @Schema(description = "The tip amount. Only one of tipAmount or tipPercent is allowed")
    @Digits(integer = 12, fraction = 2)
    @Range(message = "the number must be positive")
    private BigDecimal tipAmount;

    @Schema(description = "The tip percent. Only one of tipAmount or tipPercent is allowed")
    @Digits(integer = 3, fraction = 4)
    @Range(message = "the number must be positive")
    private BigDecimal tipPercent;

    @ApiModelProperty(name = "The taxes for the specific bill", position = 5)
    private List<@Valid TaxResource> taxes;
}
