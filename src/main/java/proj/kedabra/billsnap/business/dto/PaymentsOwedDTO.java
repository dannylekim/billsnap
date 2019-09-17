package proj.kedabra.billsnap.business.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
public class PaymentsOwedDTO {

    private Map<String, BigDecimal> amountsOwedList = new HashMap<>();

}
