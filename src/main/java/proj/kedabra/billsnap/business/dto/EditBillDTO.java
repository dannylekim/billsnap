package proj.kedabra.billsnap.business.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class EditBillDTO {

    private String name;

    private AccountDTO responsible;

    private String company;

    private String category;

    private BigDecimal tipPercent;

    private List<ItemAssociationDTO> items;

}
