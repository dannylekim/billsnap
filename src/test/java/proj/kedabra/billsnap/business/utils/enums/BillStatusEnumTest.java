package proj.kedabra.billsnap.business.utils.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BillStatusEnumTest {

    @Test
    @DisplayName("is() should return true if BillStatus is Resolved")
    void IsShouldReturnTrueIfBillStatusIsResolved() {
        //Given
        final BillStatusEnum status = BillStatusEnum.RESOLVED;

        //When/Then
        assertThat(status.is("RESOLVED")).isTrue();

    }

    @Test
    @DisplayName("is() should return false if BillStatus is not Resolved")
    void IsShouldReturnFalseIfBillStatusIsNotResolved() {
        //Given
        final BillStatusEnum status = BillStatusEnum.IN_PROGRESS;

        //When/Then
        assertThat(status.is("RESOLVED")).isFalse();

    }

    @Test
    @DisplayName("is() should return true if BillStatus is In Progress")
    void IsShouldReturnTrueIfBillStatusIsInProgress() {
        //Given
        final BillStatusEnum status = BillStatusEnum.IN_PROGRESS;

        //When/Then
        assertThat(status.is("IN_PROGRESS")).isTrue();

    }

    @Test
    @DisplayName("is() should return false if BillStatus is not In Progress")
    void IsShouldReturnFalseIfBillStatusIsNotInProgress() {
        //Given
        final BillStatusEnum status = BillStatusEnum.OPEN;

        //When/Then
        assertThat(status.is("IN_PROGRESS")).isFalse();

    }

    @Test
    @DisplayName("is() should return true if BillStatus is Open")
    void IsShouldReturnTrueIfBillStatusIsOpen() {
        //Given
        final BillStatusEnum status = BillStatusEnum.OPEN;

        //When/Then
        assertThat(status.is("OPEN")).isTrue();

    }

    @Test
    @DisplayName("is() should return false if BillStatus is not Open")
    void IsShouldReturnFalseIfBillStatusIsNotOpen() {
        //Given
        final BillStatusEnum status = BillStatusEnum.RESOLVED;

        //When/Then
        assertThat(status.is("OPEN")).isFalse();

    }
}