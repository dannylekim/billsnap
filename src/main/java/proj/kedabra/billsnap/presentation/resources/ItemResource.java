package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import proj.kedabra.billsnap.presentation.deserializers.MoneySerializer;

@Data
public class ItemResource implements Serializable {

    @Schema(description = "Id of the item")
    private Long id;

    @Schema(description = "Name of the item")
    private String name;

    @Schema(description = "Cost of the item")
    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal cost;

}
