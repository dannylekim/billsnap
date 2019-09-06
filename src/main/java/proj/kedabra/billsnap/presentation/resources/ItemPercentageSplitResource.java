package proj.kedabra.billsnap.presentation.resources;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

@Data
public class ItemPercentageSplitResource implements Serializable {

    private static final long serialVersionUID = 7674177331342109047L;

    private Long itemId;

    private String name;

    private BigDecimal cost;

    private BigDecimal percentage;
}
