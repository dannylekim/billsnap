package proj.kedabra.billsnap.business.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Optional;

import org.springframework.stereotype.Service;

import proj.kedabra.billsnap.business.model.entities.Bill;
import proj.kedabra.billsnap.business.model.entities.Item;
import proj.kedabra.billsnap.business.model.entities.Tax;
import proj.kedabra.billsnap.business.service.CalculatePaymentService;

@Service
public class CalculatePaymentServiceImpl implements CalculatePaymentService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    @Override
    public BigDecimal calculateBalance(final Bill bill) {
        final BigDecimal subTotal = calculateSubTotal(bill);
        final var taxes = calculateTaxes(subTotal, bill.getTaxes());
        final var total = subTotal.add(taxes);
        final BigDecimal tipTotal = calculateTip(bill.getTipAmount(), bill.getTipPercent(), total);

        return total.add(tipTotal).setScale(DOLLAR_SCALE, RoundingMode.HALF_EVEN);
    }

    @Override
    public BigDecimal calculateSubTotal(Bill bill) {
        return bill.getItems().stream().map(Item::getCost).reduce(BigDecimal.ZERO, BigDecimal::add);
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
        return total.subtract(subTotal).setScale(DOLLAR_SCALE, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateTip(final BigDecimal billTipAmount, final BigDecimal billTipPercent, final BigDecimal total) {
        final var tipAmount = Optional.ofNullable(billTipAmount).orElse(BigDecimal.ZERO);

        final BigDecimal tipPercentAmount = Optional.ofNullable(billTipPercent)
                .map(CalculatePaymentServiceImpl::setPercentScale)
                .map(CalculatePaymentServiceImpl::getDecimalPercentage)
                .map(total::multiply)
                .orElse(BigDecimal.ZERO);

        return tipAmount.add(tipPercentAmount).setScale(DOLLAR_SCALE, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateAmountRemaining(final BigDecimal total, final BigDecimal amountPaid) {
        return total.subtract(amountPaid);
    }

    private static BigDecimal getDecimalPercentage(BigDecimal tipPercent) {
        return tipPercent.divide(ONE_HUNDRED, RoundingMode.DOWN);
    }

    private static BigDecimal setPercentScale(BigDecimal tipPercent) {
        return tipPercent.setScale(PERCENT_SCALE, RoundingMode.DOWN);
    }

}
