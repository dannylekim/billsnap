package proj.kedabra.billsnap.business.dto;

import java.util.List;

import lombok.Data;

@Data
public class AssociateBillDTO {

    private Long id;

    private List<ItemAssociationDTO> items;
}
