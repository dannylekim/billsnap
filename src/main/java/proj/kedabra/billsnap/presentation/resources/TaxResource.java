package proj.kedabra.billsnap.presentation.resources;

import java.math.BigDecimal;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TaxResource {

    @NotBlank
    @Size(max = 10)
    @ApiModelProperty(name = "The name of the tax like TVQ or TPS. Required")
    private String name;

    @Digits(integer = 3, fraction = 4)
    @ApiModelProperty(name = "The percentage used for this specific tax. Required")
    @Range(message = "the number must be positive")
    private BigDecimal percentage;
}
