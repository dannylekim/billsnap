package proj.kedabra.billsnap.fixtures;

import proj.kedabra.billsnap.presentation.resources.PaymentOwedResource;

import java.math.BigDecimal;

public class PaymentOwedResourceFixture {

    private PaymentOwedResourceFixture() {}

    public static PaymentOwedResource getDefault() {
        final var paymentsOwedResource = new PaymentOwedResource();
        paymentsOwedResource.setEmail("payment@testmail.com");
        paymentsOwedResource.setAmount(BigDecimal.valueOf(250));

        return paymentsOwedResource;
    }

}
