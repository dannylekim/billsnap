package proj.kedabra.billsnap.business.facade.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.test.context.ActiveProfiles;

import proj.kedabra.billsnap.business.dto.BillDTO;
import proj.kedabra.billsnap.business.dto.ItemDTO;
import proj.kedabra.billsnap.business.entities.AccountBill;
import proj.kedabra.billsnap.business.entities.Bill;
import proj.kedabra.billsnap.business.entities.Item;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.fixtures.BillDTOFixture;
import proj.kedabra.billsnap.utils.SpringProfiles;

@Tag("integration")
@ActiveProfiles(SpringProfiles.TEST)
@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class BillFacadeImplIT {

    @Autowired
    private BillFacadeImpl billFacade;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    @DisplayName("Should return an exception if the account does not exist")
    void shouldReturnExceptionIfAccountDoesNotExist() {
        // Given
        final var billDTO = BillDTOFixture.getDefault();
        final String testEmail = "abc@123.ca";

        // When/Then
        final ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class,
                () -> billFacade.addPersonalBill(testEmail, billDTO));
        assertEquals("Account does not exist", resourceNotFoundException.getMessage());
    }

    @Test
    @DisplayName("Should save bill to user in database")
    void shouldSaveBillToUserInDatabase() {

        // Given
        final var billDTO = BillDTOFixture.getDefault();
        final String testEmail = "test@email.com";

        // When
        final BillDTO returnBillDTO = billFacade.addPersonalBill(testEmail, billDTO);

        // Then
        final var account = accountRepository.getAccountByEmail(testEmail);

        final List<Bill> bills = account.getBills().stream()
                .map(AccountBill::getBill)
                .filter(b -> b.getId() == returnBillDTO.getId())
                .collect(Collectors.toList());

        assertAll(() -> {
            assertEquals(1, bills.size());
            final Bill bill = bills.get(0);
            assertEquals(BillStatusEnum.OPEN, bill.getStatus());
            assertEquals(returnBillDTO.getCategory(), bill.getCategory());
            assertEquals(returnBillDTO.getCompany(), bill.getCompany());
            final List<ItemDTO> items = returnBillDTO.getItems();
            final List<Item> billItems = bill.getItems();
            assertEquals(items.size(), billItems.size());
            final ItemDTO returnItemDTO = items.get(0);
            final Item item = billItems.get(0);
            assertEquals(returnItemDTO.getName(), item.getName());
            assertEquals(returnItemDTO.getCost(), item.getCost());
            assertEquals(returnBillDTO.getName(), bill.getName());
        });
    }

    @Test
    @DisplayName("Should save bill with 100% to user in database")
    void shouldSaveBill100ToUserInDatabase() {

        // Given
        final var billDTO = BillDTOFixture.getDefault();
        final String testEmail = "test@email.com";

        // When
        final BillDTO returnBillDTO = billFacade.addPersonalBill(testEmail, billDTO);

        // Then
        final var account = accountRepository.getAccountByEmail(testEmail);

        final List<AccountBill> accountBills = account.getBills().stream()
                .filter(ab -> ab.getBill().getId() == returnBillDTO.getId())
                .collect(Collectors.toList());

        assertEquals(1, accountBills.size());
        assertEquals(BigDecimal.valueOf(100), accountBills.get(0).getPercentage());


    }
}