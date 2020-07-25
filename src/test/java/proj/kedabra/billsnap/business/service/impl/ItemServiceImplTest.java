package proj.kedabra.billsnap.business.service.impl;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import proj.kedabra.billsnap.business.dto.ItemDTO;
import proj.kedabra.billsnap.business.exception.ResourceNotFoundException;
import proj.kedabra.billsnap.business.mapper.ItemMapper;
import proj.kedabra.billsnap.business.model.entities.Item;
import proj.kedabra.billsnap.business.repository.ItemRepository;
import proj.kedabra.billsnap.fixtures.AccountEntityFixture;
import proj.kedabra.billsnap.fixtures.BillEntityFixture;
import proj.kedabra.billsnap.fixtures.EditBillDTOFixture;
import proj.kedabra.billsnap.fixtures.ItemEntityFixture;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

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
        repoItem.setId(1000L);
        repoItem.setCost(BigDecimal.valueOf(69));
        repoItem.setName("Repo Item");

        when(itemMapper.toEntity(any())).thenReturn(newItem);
        when(itemRepository.findById(any())).thenReturn(Optional.of(repoItem));
        doAnswer((invocation) -> {
            final var source = invocation.getArgument(0, ItemDTO.class);
            final var target = invocation.getArgument(1, Item.class);
            target.setName(source.getName());
            target.setCost(source.getCost());
            return null;
        }).when(itemMapper).updateItem(any(), eq(repoItem));

        // When
        itemService.editNewItems(bill, account, editBill);

        // then
        final var items = new ArrayList<>(bill.getItems());

        assertThat(items).hasSameSizeAs(editBill.getItems());

        editBill.getItems().forEach(editItem -> {
            final Item correspondingBillItem;
            if (editItem.getId() == null) {
                correspondingBillItem = items.stream().filter(i -> !i.getId().equals(repoItem.getId())).findFirst().orElseThrow();
            } else {
                correspondingBillItem = items.stream().filter(i -> i.getId().equals(editItem.getId())).findFirst().orElseThrow();
            }
            assertThat(correspondingBillItem.getCost()).isEqualByComparingTo(editItem.getCost());
            assertThat(correspondingBillItem.getName()).isEqualTo(editItem.getName());
        });

    }

    @Test
    @DisplayName("Should throw exception if Item does not belong in Bill")
    void shouldThrowExceptionIfItemIdDoesNotBelongInBill() {
        // Given
        final var bill = BillEntityFixture.getDefault();
        final var account = AccountEntityFixture.getDefaultAccount();
        final var editBill = EditBillDTOFixture.getDefault();
        final var newItem = ItemEntityFixture.getDefault();
        newItem.setId(1L);
        final var repoItem = ItemEntityFixture.getDefault();
        repoItem.getBill().setId(9000L);
        repoItem.setId(1000L);
        repoItem.setCost(BigDecimal.valueOf(69));
        repoItem.setName("Repo Item");

        when(itemRepository.findById(any())).thenReturn(Optional.of(repoItem));

        // When / Then
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> itemService.editNewItems(bill, account, editBill));

    }
}
