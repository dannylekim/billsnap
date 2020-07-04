package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class ItemAssociationSplitResource implements Serializable {

    private AccountResource account;

    private List<ItemPercentageSplitResource> items;

    private BigDecimal subTotal = BigDecimal.ZERO;

    private BigDecimal tip = BigDecimal.ZERO;

    private BigDecimal taxes = BigDecimal.ZERO;

    private BigDecimal total = BigDecimal.ZERO;
}
