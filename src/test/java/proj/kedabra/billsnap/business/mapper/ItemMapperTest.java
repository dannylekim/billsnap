package proj.kedabra.billsnap.business.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import proj.kedabra.billsnap.fixtures.ItemEntityFixture;

class ItemMapperTest {

    private final ItemMapper mapper = new ItemMapperImpl();

    @Test
    @DisplayName("Should map from item entity to dto")
    void shouldMapFromItemEntityToDto() {
        //Given
        final var defaultItemEntity = ItemEntityFixture.getDefault();

        //When
        final var itemDTO = mapper.toDTO(defaultItemEntity);

        //then
        assertThat(itemDTO.getCost()).isEqualByComparingTo(defaultItemEntity.getCost());
        assertThat(itemDTO.getId()).isEqualTo(defaultItemEntity.getId());
        assertThat(itemDTO.getName()).isEqualTo(defaultItemEntity.getName());
    }

}