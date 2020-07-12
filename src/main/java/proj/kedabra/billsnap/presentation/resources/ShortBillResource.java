package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.presentation.deserializers.MoneySerializer;

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

    @Schema(description = "The created date of the bill")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss z")
    private ZonedDateTime created;

    @Schema(description = "the total amount of the bill")
    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal balance;

}
