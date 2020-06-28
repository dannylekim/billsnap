package proj.kedabra.billsnap.business.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditBillDTO {

    private String name;

    private String responsible;

    private String company;

    private String category;

    private BigDecimal tipPercent;

    private BigDecimal tipAmount;

    private List<ItemDTO> items;

    private List<TaxDTO> taxes;
}
