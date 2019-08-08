package proj.kedabra.billsnap.presentation.resources;

import java.math.BigDecimal;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class ItemResource {

    @NotBlank
    private String name;

    //TODO validation
    private BigDecimal cost;
}
