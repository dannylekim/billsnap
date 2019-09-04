package proj.kedabra.billsnap.business.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class BillDTO {

    private Long id;

    private String name;

    private String category;

    private String company;

    private List<ItemDTO> items = new ArrayList<>();

    private List<String> accountsStringList = new ArrayList<>();

    private BigDecimal tipAmount;

    private BigDecimal tipPercent;
}
