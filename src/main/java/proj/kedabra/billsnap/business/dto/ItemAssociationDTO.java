package proj.kedabra.billsnap.business.dto;

import java.util.List;

import lombok.Data;

@Data
public class ItemAssociationDTO {

    private String email;

    private List<ItemPercentageDTO> items;
}
