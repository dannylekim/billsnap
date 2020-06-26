package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Range;

import lombok.Data;

@Data
public class ItemPercentageResource implements Serializable {

    @NotNull
    private Long itemId;

    @NotNull
    @Digits(integer = 3, fraction = 4)
    @Range(message = "the number must be positive")
    private BigDecimal percentage;
}
