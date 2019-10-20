package proj.kedabra.billsnap.business.model.projections;

import java.math.BigDecimal;

public interface PaymentOwed {

    String getEmail();

    BigDecimal getAmount();

    void setEmail(String email);

    void setAmount(BigDecimal amount);
}
