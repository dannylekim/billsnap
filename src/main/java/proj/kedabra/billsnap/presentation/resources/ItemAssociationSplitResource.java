package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

import proj.kedabra.billsnap.business.service.CalculatePaymentService;
import proj.kedabra.billsnap.presentation.deserializers.MoneySerializer;

@Data
public class ItemAssociationSplitResource implements Serializable {

    private AccountResource account;

    private List<ItemPercentageSplitResource> items;

    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal subTotal = BigDecimal.ZERO;

    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal tip = BigDecimal.ZERO;

    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal taxes = BigDecimal.ZERO;

    @JsonSerialize(using = MoneySerializer.class)
    public BigDecimal getTotal() {
        return subTotal.add(taxes).add(tip).setScale(CalculatePaymentService.DOLLAR_SCALE, RoundingMode.HALF_UP);
    }
}
