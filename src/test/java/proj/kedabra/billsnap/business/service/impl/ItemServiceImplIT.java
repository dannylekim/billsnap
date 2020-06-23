package proj.kedabra.billsnap.business.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.ArrayList;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.model.entities.Item;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.fixtures.BillEntityFixture;
import proj.kedabra.billsnap.fixtures.EditBillDTOFixture;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;
import proj.kedabra.billsnap.utils.SpringProfiles;

@Tag("integration")
@ActiveProfiles(SpringProfiles.TEST)
@SpringBootTest
@Transactional
@AutoConfigureTestDatabase
public class ItemServiceImplIT {

    @Autowired
    private ItemServiceImpl itemService;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    @DisplayName("Should get existing item")
    void shouldGetExistingItem() {
        // Given
        final var existingItemId = 1000L;

        // When
        final var res = itemService.getItem(existingItemId);

        // Then
        assertThat(res.getId()).isEqualTo(existingItemId);
        assertThat(res.getCost().toString()).isEqualTo("69.00");
    }

    @Test
    @DisplayName("Should throw error when item id does not exist")
    void shouldThrowErrorWhenItemIdDoesNotExist() {
        // Given
        final var nonExistentItemId = 696969696969L;

        //When/Then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> itemService.getItem(nonExistentItemId))
                .withMessage(ErrorMessageEnum.ITEM_ID_DOES_NOT_EXIST.getMessage(Long.toString(nonExistentItemId)));
    }

    @Test
    @DisplayName("Should edit item in bill successfully")
    void shouldEditItemInBillSuccessfully() {
        // Given
        final var account = accountRepository.getAccountByEmail("test@email.com");
        final var bill = BillEntityFixture.getDefault();
        final var onlyItem = bill.getItems().iterator().next();
        onlyItem.setId(1000L);
        final var editBill = EditBillDTOFixture.getDefault();

        // When
        itemService.editNewItems(bill, account, editBill);

        // item
        final var items = new ArrayList<>(bill.getItems());

        assertThat(items).hasSameSizeAs(editBill.getItems());

        editBill.getItems().forEach(editItem -> {
            final Item correspondingBillItem;
            if (editItem.getId() == null) {
                correspondingBillItem = items.stream().filter(i -> !i.getId().equals(onlyItem.getId())).findFirst().orElseThrow();
            } else {
                correspondingBillItem = items.stream().filter(i -> i.getId().equals(editItem.getId())).findFirst().orElseThrow();
            }
            assertThat(correspondingBillItem.getCost()).isEqualByComparingTo(editItem.getCost());
            assertThat(correspondingBillItem.getName()).isEqualTo(editItem.getName());
        });
    }
}
