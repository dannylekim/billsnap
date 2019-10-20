package proj.kedabra.billsnap.fixtures;

import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import proj.kedabra.billsnap.business.model.projections.PaymentOwed;

import java.math.BigDecimal;

public class PaymentOwedProjectionFixture {

    private PaymentOwedProjectionFixture() {}

    public static PaymentOwed getDefault() {
        final ProjectionFactory factory = new SpelAwareProxyProjectionFactory();
        final PaymentOwed paymentOwed = factory.createProjection(PaymentOwed.class);
        paymentOwed.setEmail("ahshit@herewegoagain.com");
        paymentOwed.setAmount(BigDecimal.valueOf(69));

        return paymentOwed;
    }

}
