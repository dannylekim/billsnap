package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import proj.kedabra.billsnap.presentation.deserializers.MoneySerializer;

@Data
public class RemainingPaymentResource implements Serializable {


    @Schema(description = "Remaining amount left to pay towards the bill")
    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal remainingBalance;
}
