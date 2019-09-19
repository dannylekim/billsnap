package proj.kedabra.billsnap.fixtures;

import proj.kedabra.billsnap.business.dto.PaymentOwedDTO;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class PaymentOwedDTOFixture {

    public PaymentOwedDTOFixture() {}

    public static PaymentOwedDTO getDefault() {

        final var paymentsOwedDTO = new PaymentOwedDTO();

        final Map<String, BigDecimal> amountOwedMap = new HashMap<>();
        amountOwedMap.put("abc@123.com", new BigDecimal("15000"));
        paymentsOwedDTO.setAmountOwedList(amountOwedMap);

        return paymentsOwedDTO;
    }

}
