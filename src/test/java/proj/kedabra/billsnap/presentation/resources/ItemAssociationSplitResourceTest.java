package proj.kedabra.billsnap.presentation.resources;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ItemAssociationSplitResourceTest {

    @Test
    @DisplayName("Get default total as 0")
    void shouldGetDefaultTotalAsZero() {
        //Given
        final var itemAssociationSplitDTO = new ItemAssociationSplitResource();

        //When
        final var total = itemAssociationSplitDTO.getTotal();

        //Then
        assertThat(total).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Get total from all necessary values")
    void shouldGetTotalFromAllValues() {
        //Given
        final var itemAssociationSplitDTO = new ItemAssociationSplitResource();
        itemAssociationSplitDTO.setTip(BigDecimal.ONE);
        itemAssociationSplitDTO.setTaxes(BigDecimal.TEN);
        itemAssociationSplitDTO.setSubTotal(new BigDecimal("30.50"));

        //When
        final var total = itemAssociationSplitDTO.getTotal();

        //Then
        assertThat(total).isEqualByComparingTo(new BigDecimal("41.50"));
    }
}