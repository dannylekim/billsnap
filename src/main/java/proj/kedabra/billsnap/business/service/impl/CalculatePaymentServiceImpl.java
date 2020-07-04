package proj.kedabra.billsnap.business.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import proj.kedabra.billsnap.business.model.entities.Bill;
import proj.kedabra.billsnap.business.model.entities.Item;
import proj.kedabra.billsnap.business.model.entities.Tax;
import proj.kedabra.billsnap.business.service.CalculatePaymentService;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;

@Service
public class CalculatePaymentServiceImpl implements CalculatePaymentService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private static final int PERCENT_SCALE = 7;

    @Override
    public BigDecimal calculateBalance(final Bill bill) {
        final BigDecimal subTotal = bill.getItems().stream().map(Item::getCost).reduce(BigDecimal.ZERO, BigDecimal::add);
        final var taxes = calculateTaxes(subTotal, bill.getTaxes());
        final var total = subTotal.add(taxes);
        final BigDecimal tipTotal = calculateTip(bill.getTipAmount(), bill.getTipPercent(), total);

        return total.add(tipTotal).setScale(2, RoundingMode.HALF_EVEN);
    }

    @Override
    public BigDecimal calculateTaxes(final BigDecimal subTotal, final Collection<Tax> taxes) {
        final var total = taxes
                .stream()
                .map(Tax::getPercentage)
                .map(CalculatePaymentServiceImpl::setPercentScale)
                .map(CalculatePaymentServiceImpl::getDecimalPercentage)
                .map(BigDecimal.ONE::add)
                .reduce(subTotal, BigDecimal::multiply);

        //taxes are calculated on a half_up basis
        return total.subtract(subTotal).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateTip(@Nullable final BigDecimal billTipAmount, @Nullable final BigDecimal billTipPercent, @NonNull final BigDecimal total) {

        if (!CalculatePaymentServiceImpl.isOneTipMethod(billTipAmount, billTipPercent)) {
            throw new IllegalArgumentException(ErrorMessageEnum.MULTIPLE_TIP_METHOD.getMessage());
        }

        final var tipAmount = Optional.ofNullable(billTipAmount).orElse(BigDecimal.ZERO);

        final BigDecimal tipPercentAmount = Optional.ofNullable(billTipPercent)
                .map(CalculatePaymentServiceImpl::setPercentScale)
                .map(CalculatePaymentServiceImpl::getDecimalPercentage)
                .map(total::multiply)
                .orElse(BigDecimal.ZERO);

        return tipAmount.add(tipPercentAmount).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal getDecimalPercentage(BigDecimal tipPercent) {
        return tipPercent.divide(ONE_HUNDRED, RoundingMode.DOWN);
    }

    private static BigDecimal setPercentScale(BigDecimal tipPercent) {
        return tipPercent.setScale(PERCENT_SCALE, RoundingMode.DOWN);
    }

    /**
     * Verify that there's only 1 tip amount
     *
     * @param billTipAmount  the tip amount for the bill. Can be null or zero
     * @param billTipPercent the tip percentage for the bill. Can be null or zero
     * @return true if there's only 1 tip method between the two
     */
    private static boolean isOneTipMethod(@Nullable BigDecimal billTipAmount, @Nullable BigDecimal billTipPercent) {
        return (billTipAmount == null || BigDecimal.ZERO.compareTo(billTipAmount) == 0) ^ (billTipPercent == null || BigDecimal.ZERO.compareTo(billTipPercent) == 0);
    }
}
