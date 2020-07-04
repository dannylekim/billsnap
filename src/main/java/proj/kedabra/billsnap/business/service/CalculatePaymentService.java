package proj.kedabra.billsnap.business.service;

import java.math.BigDecimal;
import java.util.Collection;

import org.springframework.lang.Nullable;

import proj.kedabra.billsnap.business.model.entities.Bill;
import proj.kedabra.billsnap.business.model.entities.Tax;

public interface CalculatePaymentService {

    /**
     * Calculate the full balance in accordance to the bill
     *
     * @param bill the bill in question
     * @return the full balance set to 2 decimal places and rounded {@link java.math.RoundingMode#HALF_UP}
     */
    BigDecimal calculateBalance(Bill bill);

    /**
     * Calculate the taxes for a subtotal. It will return a value at the end set to 2 decimal places and rounded {@link java.math.RoundingMode#HALF_UP}
     *
     * @param subTotal the subTotal to be applied taxes
     * @param taxes    the taxes to apply
     * @return a value with 2 decimal places rounded {@link java.math.RoundingMode#HALF_UP}
     */
    BigDecimal calculateTaxes(BigDecimal subTotal, Collection<Tax> taxes);

    /**
     * Calculate the tip for a passed total. It will return a value at the end set to 2 decimal places and rounded {@link java.math.RoundingMode#HALF_UP}
     * <p>
     * Only one of tip amount/tip percent is allowed. The other value can be null
     *
     * @param tipAmount  A flat tip amount. Can be null.
     * @param tipPercent A tip percentage. Can be null.
     * @param total      The total value to apply tip on. Cannot be null.
     * @return a value with 2 decimal places rounded {@link java.math.RoundingMode#HALF_UP}
     */
    BigDecimal calculateTip(@Nullable BigDecimal tipAmount, @Nullable BigDecimal tipPercent, BigDecimal total);
}
