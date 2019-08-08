package proj.kedabra.billsnap.presentation.resources;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class BillResource {

    private String name;

    private AccountResource creator;

    private AccountResource responsible;

    private String status;

    private String company;

    private String category;

    private LocalDateTime created;

    private LocalDateTime updated;

    private List<ItemResource> items;

    private BigDecimal balance;

}
