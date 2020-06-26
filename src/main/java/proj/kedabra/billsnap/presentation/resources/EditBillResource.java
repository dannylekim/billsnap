package proj.kedabra.billsnap.presentation.resources;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.constraints.Digits;

import org.hibernate.validator.constraints.Range;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditBillResource {

    @Schema(description = "Name of the bill")
    private String name;

    @Schema(description = "User that is responsible for the bill")
    private String responsible;

    @Schema(description = "The company this bill is associated with")
    private String company;

    @Schema(description = "The category of the bill")
    private String category;

    @Schema(description = "The tip amount. Only one of tipAmount or tipPercent is allowed")
    @Digits(integer = 12, fraction = 2)
    @Range(message = "the number must be positive")
    private BigDecimal tipAmount;

    @Schema(description = "The tip percent. Only one of tipAmount or tipPercent is allowed")
    @Digits(integer = 3, fraction = 4)
    @Range(message = "the number must be positive")
    private BigDecimal tipPercent;

    @Schema(description = "List of items to add in bill")
    private List<ItemResource> items;
}
