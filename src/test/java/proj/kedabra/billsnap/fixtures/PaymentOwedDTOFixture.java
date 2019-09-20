package proj.kedabra.billsnap.fixtures;

import proj.kedabra.billsnap.business.dto.PaymentOwedDTO;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class PaymentOwedDTOFixture {

    public PaymentOwedDTOFixture() {}

    public static PaymentOwedDTO getDefault() {

        final var paymentsOwedDTO = new PaymentOwedDTO();
        paymentsOwedDTO.setEmail("abc@123.com");
        paymentsOwedDTO.setAmount(BigDecimal.valueOf(250));

        return paymentsOwedDTO;
    }

}
