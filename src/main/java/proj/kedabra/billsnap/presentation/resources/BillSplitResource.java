package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.SplitByEnum;

@Data
public class BillSplitResource implements Serializable {

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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss Z")
    private ZonedDateTime created;

    @Schema(description = "Time that the bill is updated")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss Z")
    private ZonedDateTime updated;

    @Schema(description = "List of items associated per account on the bill")
    private List<ItemAssociationSplitResource> informationPerAccount;

    @Schema(description = "By which method the bill is split by")
    private SplitByEnum splitBy;

    @Schema(description = "Total tip of the bill")
    private BigDecimal totalTip;

    @Schema(description = "the total amount of the bill")
    private BigDecimal balance;

    @Schema(description = "The taxes for the specific bill")
    private List<TaxResource> taxes;

}
