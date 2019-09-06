package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class ItemAssociationSplitResource implements Serializable {

    private static final long serialVersionUID = 2200729998541265172L;

    private AccountResource account;

    private List<ItemPercentageSplitResource> items;

    private BigDecimal cost;
}
