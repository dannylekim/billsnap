package proj.kedabra.billsnap.business.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.MockitoAnnotations;

import proj.kedabra.billsnap.fixtures.BillSplitDTOFixture;

class BillMapperTest {

    private BillMapperImpl billMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        billMapper = new BillMapperImpl(Mappers.getMapper(AccountMapper.class));
    }

    @Test
    @DisplayName("Should map BillSplitDTO to PendingRegisteredBillSplitDTO")
    void shouldMapBillSplitDTOtoPendingRegisteredBillSplitDTO() {
        //Given
        final var billSplitDTO = BillSplitDTOFixture.getDefault();
        billSplitDTO.setId(9001L);
        final var name = "billmappertest";
        billSplitDTO.setName(name);

        //When
        final var pendingRegisteredBillSplitDTO = billMapper.toPendingRegisteredBillSplitDTO(billSplitDTO);

        //Then
        assertThat(pendingRegisteredBillSplitDTO.getId()).isEqualTo(billSplitDTO.getId());
        assertThat(pendingRegisteredBillSplitDTO.getName()).isEqualTo(billSplitDTO.getName());
        assertThat(pendingRegisteredBillSplitDTO.getBalance()).isEqualTo(billSplitDTO.getBalance());
        assertThat(pendingRegisteredBillSplitDTO.getCategory()).isEqualTo(billSplitDTO.getCategory());
        assertThat(pendingRegisteredBillSplitDTO.getCompany()).isEqualTo(billSplitDTO.getCompany());
        assertThat(pendingRegisteredBillSplitDTO.getCreator()).isEqualTo(billSplitDTO.getCreator());
        assertThat(pendingRegisteredBillSplitDTO.getItemsPerAccount()).isEqualTo(billSplitDTO.getItemsPerAccount());
        assertThat(pendingRegisteredBillSplitDTO.getResponsible()).isEqualTo(billSplitDTO.getResponsible());
        assertThat(pendingRegisteredBillSplitDTO.getSplitBy()).isEqualTo(billSplitDTO.getSplitBy());
        assertThat(pendingRegisteredBillSplitDTO.getStatus()).isEqualTo(billSplitDTO.getStatus());
        assertThat(pendingRegisteredBillSplitDTO.getTotalTip()).isEqualTo(billSplitDTO.getTotalTip());
        assertThat(pendingRegisteredBillSplitDTO.getCreated()).isCloseTo(billSplitDTO.getCreated(), within(500, ChronoUnit.MILLIS));
        assertThat(pendingRegisteredBillSplitDTO.getPendingAccounts()).isNull();

    }
}