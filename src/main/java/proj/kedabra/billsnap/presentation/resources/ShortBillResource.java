package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;

@Data
public class ShortBillResource implements Serializable {

    @Schema(description = "Id of the bill")
    private Long id;

    @Schema(description = "Name of the bill")
    private String name;

    @Schema(description = "status of the bill")
    private BillStatusEnum status;

    @Schema(description = "The category of the bill")
    private String category;

    @Schema(description = "the total amount of the bill")
    private BigDecimal balance;

}
