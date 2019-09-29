package proj.kedabra.billsnap.model.projections;

import java.math.BigDecimal;

public interface PaymentOwed {

    String getEmail();

    BigDecimal getAmount();

}
