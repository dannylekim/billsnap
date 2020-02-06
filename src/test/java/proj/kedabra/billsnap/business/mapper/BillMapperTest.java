package proj.kedabra.billsnap.business.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import proj.kedabra.billsnap.fixtures.BillSplitDTOFixture;

class BillMapperTest {

    @Mock
    private AccountMapper accountMapper;

    private BillMapperImpl billMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        billMapper = new BillMapperImpl(accountMapper);
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
        assertThat(pendingRegisteredBillSplitDTO).hasFieldOrPropertyWithValue("id", 9001L)
                .hasFieldOrPropertyWithValue("name", name);
    }
}