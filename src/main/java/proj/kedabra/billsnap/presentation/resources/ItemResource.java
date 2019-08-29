package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;

import lombok.Data;

@Data
public class ItemResource implements Serializable {

    private static final long serialVersionUID = 7201751679337910110L;

    @NotBlank
    @Size(max = 30)
    private String name;

    @Digits(integer = 12, fraction = 2)
    @Range(message = "the number must be positive")
    private BigDecimal cost;
}
