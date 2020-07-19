package proj.kedabra.billsnap.business.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GetBillPaginationDTOTest {

    @Test
    @DisplayName("Should convert LocalDate to ZonedDateTime")
    void shouldConvertLocalDateToZonedDateTime() {
        //Given
        final var startDate = LocalDate.of(2019, 12, 1);
        final var endDate = LocalDate.of(2020, 4, 4);
        final var tmpPagination = new GetBillPaginationDTO();

        //When
        tmpPagination.setStartDate(startDate);
        tmpPagination.setEndDate(endDate);

        //Then
        final ZonedDateTime startDateZone = tmpPagination.getStartDate();
        final ZonedDateTime endDateZone = tmpPagination.getEndDate();

        assertThat(startDateZone.getYear()).isEqualTo(2019);
        assertThat(startDateZone.getMonth()).isEqualTo(Month.DECEMBER);
        assertThat(startDateZone.getDayOfMonth()).isEqualTo(1);
        assertThat(startDateZone.getHour()).isZero();
        assertThat(startDateZone.getMinute()).isZero();
        assertThat(startDateZone.getSecond()).isZero();
        assertThat(endDateZone.getYear()).isEqualTo(2020);
        assertThat(endDateZone.getMonth()).isEqualByComparingTo(Month.APRIL);
        assertThat(endDateZone.getDayOfMonth()).isEqualTo(4);
        assertThat(endDateZone.getHour()).isZero();
        assertThat(endDateZone.getMinute()).isZero();
        assertThat(endDateZone.getSecond()).isZero();

    }

}
