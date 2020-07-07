package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import proj.kedabra.billsnap.presentation.deserializers.MoneySerializer;

@Data
public class PaymentOwedResource implements Serializable {

    @Schema(description = "account email")
    private String email;

    @Schema(description = "amount owed")
    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal amount;

}
