package proj.kedabra.billsnap.business.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;

@Data
public class BillCompleteDTO {

    private Long id;

    private String name;

    private String category;

    private String company;

    private List<ItemDTO> items = new ArrayList<>();

    private List<AccountDTO> accountsList = new ArrayList<>();

    private BigDecimal tipAmount;

    private BigDecimal tipPercent;

    private AccountDTO creator;

    private AccountDTO responsible;

    private BillStatusEnum status;

    private ZonedDateTime created;

    private ZonedDateTime updated;

    private BigDecimal balance;

}
