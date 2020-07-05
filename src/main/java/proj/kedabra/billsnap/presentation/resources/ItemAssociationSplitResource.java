package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import lombok.Data;

import proj.kedabra.billsnap.business.service.CalculatePaymentService;

@Data
public class ItemAssociationSplitResource implements Serializable {

    private AccountResource account;

    private List<ItemPercentageSplitResource> items;

    private BigDecimal subTotal = BigDecimal.ZERO;

    private BigDecimal tip = BigDecimal.ZERO;

    private BigDecimal taxes = BigDecimal.ZERO;

    public BigDecimal getTotal() {
        return subTotal.add(taxes).add(tip).setScale(CalculatePaymentService.DOLLAR_SCALE, RoundingMode.HALF_UP);
    }
}
