package proj.kedabra.billsnap.business.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;

import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;

@Data
@EqualsAndHashCode(callSuper = false)
public class BillCompleteDTO extends BillDTO {

    private AccountDTO creator;

    private AccountDTO responsible;

    private BillStatusEnum status;

    private ZonedDateTime created;

    private ZonedDateTime updated;

    private BigDecimal balance;

}
