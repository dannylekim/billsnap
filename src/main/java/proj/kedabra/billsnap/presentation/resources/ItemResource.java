package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ItemResource implements Serializable {

    @Schema(description = "Id of the item")
    private Long id;

    @Schema(description = "Name of the item")
    private String name;

    @Schema(description = "Cost of the item")
    private BigDecimal cost;

}
