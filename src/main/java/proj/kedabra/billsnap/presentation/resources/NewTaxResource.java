package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewTaxResource implements Serializable {

    @NotBlank
    @Size(max = 10)
    @Schema(description = "The name of the tax like TVQ or TPS. Required")
    private String name;

    @Digits(integer = 3, fraction = 4)
    @Schema(description = "The percentage used for this specific tax. Required")
    @Range(message = "the number must be positive")
    private BigDecimal percentage;
}
