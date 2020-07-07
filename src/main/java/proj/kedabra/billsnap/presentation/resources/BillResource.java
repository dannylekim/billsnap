package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.presentation.deserializers.MoneySerializer;

@Data
public class BillResource implements Serializable {

    @Schema(description = "Id of the bill")
    private Long id;

    @Schema(description = "Name of the bill")
    private String name;

    @Schema(description = "User that created the bill")
    private AccountResource creator;

    @Schema(description = "User that is responsible for the bill")
    private AccountResource responsible;

    @Schema(description = "status of the bill")
    private BillStatusEnum status;

    @Schema(description = "The company this bill is associated with")
    private String company;

    @Schema(description = "The category of the bill")
    private String category;

    @Schema(description = "Time that the bill is created")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss z")
    private ZonedDateTime created;

    @Schema(description = "Time that the bill is updated")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss z")
    private ZonedDateTime updated;

    @Schema(description = "List of items that were on the bill")
    private List<ItemResource> items;

    @Schema(description = "List of accounts associated to the bill")
    private List<AccountStatusResource> accountsList;

    @Schema(description = "the total amount of the bill")
    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal balance;

}
