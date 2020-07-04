package proj.kedabra.billsnap.business.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import proj.kedabra.billsnap.business.model.entities.Tax;
import proj.kedabra.billsnap.business.service.CalculatePaymentService;
import proj.kedabra.billsnap.fixtures.BillEntityFixture;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;

class CalculatePaymentServiceImplTest {

    private final CalculatePaymentService calculatePaymentService = new CalculatePaymentServiceImpl();

    @Test
    @DisplayName("Only one tipping method is supported")
    void shouldCalculateTipByPercentage() {
        // When/Then
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> calculatePaymentService.calculateTip(null, null, BigDecimal.TEN)).withMessage(ErrorMessageEnum.MULTIPLE_TIP_METHOD.getMessage());
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> calculatePaymentService.calculateTip(BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN)).withMessage(ErrorMessageEnum.MULTIPLE_TIP_METHOD.getMessage());
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> calculatePaymentService.calculateTip(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.TEN)).withMessage(ErrorMessageEnum.MULTIPLE_TIP_METHOD.getMessage());
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> calculatePaymentService.calculateTip(BigDecimal.ZERO, null, BigDecimal.TEN)).withMessage(ErrorMessageEnum.MULTIPLE_TIP_METHOD.getMessage());
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> calculatePaymentService.calculateTip(null, BigDecimal.ZERO, BigDecimal.TEN)).withMessage(ErrorMessageEnum.MULTIPLE_TIP_METHOD.getMessage());


    }

    @Test
    @DisplayName("Must return tip by amount")
    void shouldCalculateTipByAmount() {
        // When
        final var tip = calculatePaymentService.calculateTip(BigDecimal.TEN, null, BigDecimal.TEN);

        // Then
        assertThat(tip).isEqualByComparingTo(BigDecimal.TEN);
    }


    @Test
    @DisplayName("Must return tip by percent")
    void shouldCalculateTipByPercent() {
        // When
        final var tip = calculatePaymentService.calculateTip(null, new BigDecimal("10.12"), BigDecimal.TEN);

        // Then
        assertThat(tip).isEqualByComparingTo(new BigDecimal("1.01"));
    }

    @Test
    @DisplayName("Must calculate taxes on a subTotal")
    void shouldCalculateTaxesOnASubTotal() {
        // Given
        final var tax = new Tax();
        tax.setPercentage(BigDecimal.TEN);
        final var tax2 = new Tax();
        tax2.setPercentage(new BigDecimal("25.6"));

        // When
        final var taxes = this.calculatePaymentService.calculateTaxes(BigDecimal.TEN, List.of(tax, tax2));

        //Then
        assertThat(taxes).isEqualByComparingTo(new BigDecimal("3.82"));
    }

    @Test
    @DisplayName("Must calculate the balance with taxes and tip amount")
    void shouldCalculateBalanceWithTaxesAndTipAmount() {
        // Given
        final var bill = BillEntityFixture.getDefault();

        // When
        final var balance = calculatePaymentService.calculateBalance(bill);

        //Then
        assertThat(balance).isEqualByComparingTo(new BigDecimal("14.40"));
    }

    @Test
    @DisplayName("Must calculate the balance with taxes and tip percent")
    void shouldCalculateBalanceWithTaxesAndTipPercentage() {
        // Given
        final var bill = BillEntityFixture.getDefault();
        bill.setTipAmount(null);
        bill.setTipPercent(BigDecimal.TEN);

        // When
        final var balance = calculatePaymentService.calculateBalance(bill);

        //Then
        assertThat(balance).isEqualByComparingTo(new BigDecimal("4.84"));
    }
}