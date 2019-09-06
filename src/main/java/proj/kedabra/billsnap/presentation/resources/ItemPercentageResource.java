package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Range;

import lombok.Data;

@Data
public class ItemPercentageResource implements Serializable {

    private static final long serialVersionUID = 2800827086143046648L;

    @NotNull
    private Long itemId;

    @NotBlank
    @Digits(integer = 3, fraction = 4)
    @Range(message = "the number must be positive")
    private BigDecimal percentage;
}
