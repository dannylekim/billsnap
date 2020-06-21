package proj.kedabra.billsnap.fixtures;

import java.math.BigDecimal;

import proj.kedabra.billsnap.presentation.resources.TaxResource;

public final class TaxResourceFixture {

    private TaxResourceFixture() {}

    public static TaxResource getDefault() {
        final var taxResource = new TaxResource();
        taxResource.setName("Tax 1");
        taxResource.setPercentage(BigDecimal.TEN);
        return taxResource;
    }
}
