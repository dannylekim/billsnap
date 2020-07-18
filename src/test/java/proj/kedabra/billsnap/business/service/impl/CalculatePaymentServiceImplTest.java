package proj.kedabra.billsnap.business.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import proj.kedabra.billsnap.business.model.entities.Tax;
import proj.kedabra.billsnap.business.service.CalculatePaymentService;
import proj.kedabra.billsnap.fixtures.BillEntityFixture;
import proj.kedabra.billsnap.fixtures.ItemEntityFixture;

class CalculatePaymentServiceImplTest {

    private final CalculatePaymentService calculatePaymentService = new CalculatePaymentServiceImpl();

    @Test
    @DisplayName("Should calculate the subtotal of items within a bill")
    void shouldCalculateSubtotal() {
        // Given
        final var bill = BillEntityFixture.getDefault();
        bill.getItems().add(ItemEntityFixture.getDefault());

        // When
        final var subTotal = calculatePaymentService.calculateSubTotal(bill);

        //Then
        assertThat(subTotal).isEqualByComparingTo(new BigDecimal("14.00"));
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

    @Test
    @DisplayName("Must calculate the amount remaining")
    void shouldCalculateAmountRemaining() {
        // Given
        final var total = new BigDecimal("10.00");
        final var amountPaid = new BigDecimal("2.00");

        // When
        final var balance = calculatePaymentService.calculateAmountRemaining(total, amountPaid);

        //Then
        assertThat(balance).isEqualByComparingTo(new BigDecimal("8.00"));
    }
}