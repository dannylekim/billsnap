package proj.kedabra.billsnap.business.dto;

import java.util.List;

import lombok.Data;

@Data
public class BillDTO {

    private long id;

    private String name;

    private String category;

    private String company;

    private List<ItemDTO> items;
}
