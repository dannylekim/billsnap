package proj.kedabra.billsnap.fixtures;

import java.math.BigDecimal;

import proj.kedabra.billsnap.presentation.resources.PaymentResource;

public final class PaymentResourceFixture {

    private PaymentResourceFixture() {}

    public static PaymentResource getDefault() {
        final var paymentResource = new PaymentResource();
        paymentResource.setId(123L);
        paymentResource.setPaymentAmount(BigDecimal.TEN);

        return paymentResource;
    }
}
