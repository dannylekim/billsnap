package proj.kedabra.billsnap.business.service.impl;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;

import proj.kedabra.billsnap.business.mapper.ItemMapper;
import proj.kedabra.billsnap.business.model.entities.Item;
import proj.kedabra.billsnap.business.repository.ItemRepository;
import proj.kedabra.billsnap.fixtures.AccountEntityFixture;
import proj.kedabra.billsnap.fixtures.BillEntityFixture;
import proj.kedabra.billsnap.fixtures.EditBillDTOFixture;
import proj.kedabra.billsnap.fixtures.ItemEntityFixture;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;

public class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemMapper itemMapper;

    private ItemServiceImpl itemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        itemService = new ItemServiceImpl(itemRepository, itemMapper);
    }

    @Test
    @DisplayName("Should get item from repository")
    void shouldGetItem() {
        // Given
        final Item item = ItemEntityFixture.getDefault();
        when(itemRepository.findById(any())).thenReturn(Optional.of(item));

        // When
        final var result = itemService.getItem(10L);

        // Then
        assertThat(result.getId()).isEqualTo(item.getId());
    }

    @Test
    @DisplayName("Should throw error when item is not found")
    void shouldThrowErrorWhenItemIsNotFound() {
        // Given
        when(itemRepository.findById(any()))
                .thenThrow(new ResourceNotFoundException(ErrorMessageEnum.ITEM_ID_DOES_NOT_EXIST.getMessage(Long.toString(10L))));

        // When/then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> itemService.getItem(10L))
                .withMessage(ErrorMessageEnum.ITEM_ID_DOES_NOT_EXIST.getMessage(Long.toString(10L)));

    }

    @Test
    @DisplayName("Should edit items successfully")
    void shouldEditItemsSuccessfully() {
        // Given
        final var bill = BillEntityFixture.getDefault();
        final var account = AccountEntityFixture.getDefaultAccount();
        final var editBill = EditBillDTOFixture.getDefault();
        final var newItem = ItemEntityFixture.getDefault();
        newItem.setId(1L);
        final var repoItem = ItemEntityFixture.getDefault();
        repoItem.setCost(BigDecimal.valueOf(69));

        when(itemMapper.toEntity(any())).thenReturn(newItem);
        when(itemRepository.findById(any())).thenReturn(Optional.of(repoItem));

        // When
        itemService.editNewItems(bill, account, editBill);

        // then
        final var items = new ArrayList<>(bill.getItems());
        if (items.get(0).getId().equals(1000L)) {
            assertThat(items.get(0).getCost().toString()).isEqualTo(repoItem.getCost().toString());
            assertThat(items.get(1).getCost().toString()).isEqualTo(editBill.getItems().get(1).getCost().toString());
        } else {
            assertThat(items.get(0).getCost().toString()).isEqualTo(editBill.getItems().get(1).getCost().toString());
            assertThat(items.get(1).getCost().toString()).isEqualTo(repoItem.getCost().toString());
        }

    }
}
