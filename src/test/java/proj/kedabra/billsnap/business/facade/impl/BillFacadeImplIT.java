package proj.kedabra.billsnap.business.facade.impl;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.dto.BillCompleteDTO;
import proj.kedabra.billsnap.business.dto.ItemDTO;
import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.model.entities.AccountBill;
import proj.kedabra.billsnap.business.model.entities.Bill;
import proj.kedabra.billsnap.business.model.entities.Item;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.business.repository.BillRepository;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.InvitationStatusEnum;
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

    @Autowired
    private BillRepository billRepository;

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
    @DisplayName("Should return exception if list of emails contains one or more emails that do not exist")
    void ShouldReturnExceptionIfEmailInListOfEmailsDoesNotExist() {
        //Given a bill creator with existing email, but billDTO containing non-existent email in array of emails
        final var billDTO = BillDTOFixture.getDefault();
        final String existentEmail = "userdetails@service.com";
        final String anotherExistentEmail = "test@email.com";
        final String nonExistentEmail = "nonexistent@email.com";
        billDTO.setAccountsList(List.of(nonExistentEmail, anotherExistentEmail));

        //When/Then
        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() -> billFacade.addPersonalBill(existentEmail, billDTO))
                .withMessage("One or more accounts in the list of accounts does not exist: [%s]", nonExistentEmail);
    }

    @Test
    @DisplayName("Should return exception if list of emails contains bill creator email")
    void ShouldReturnExceptionIfBillCreatorIsInListOfEmails() {
        //Given
        final var billDTO = BillDTOFixture.getDefault();
        final String billCreator = "userdetails@service.com";
        final String anotherExistentEmail = "test@email.com";
        billDTO.setAccountsList(List.of(billCreator, anotherExistentEmail));

        //When/Then
        assertThatIllegalArgumentException().isThrownBy(() -> billFacade.addPersonalBill(billCreator, billDTO))
                .withMessage("List of emails cannot contain bill creator email");
    }

    @Test
    @DisplayName("Should save bill in database")
    void shouldSaveBillToUserInDatabase() {

        // Given
        final var billDTO = BillDTOFixture.getDefault();
        final String testEmail = "test@email.com";

        // When
        final BillCompleteDTO returnBillDTO = billFacade.addPersonalBill(testEmail, billDTO);

        // Then
        final var bill = billRepository.findById(returnBillDTO.getId()).orElseThrow();

        verifyBillDTOToBill(returnBillDTO, bill);
    }

    @Test
    @DisplayName("Should save bill with non-empty accountsList in database")
    void shouldSaveBillWithAccountsListInDatabase() {
        // Given
        final var billDTO = BillDTOFixture.getDefault();
        final String testEmail = "test@email.com";
        final String existentEmail = "userdetails@service.com";
        billDTO.setAccountsList(List.of(existentEmail));
        // When
        final BillCompleteDTO returnBillDTO = billFacade.addPersonalBill(testEmail, billDTO);

        // Then
        final var bill = billRepository.findById(returnBillDTO.getId()).orElseThrow();

        verifyBillDTOToBill(returnBillDTO, bill);
        assertThat(returnBillDTO.getAccountsList()).isNotEmpty();
        AssertionsForClassTypes.assertThat(returnBillDTO.getAccountsList().get(0).getAccount().getEmail()).isEqualTo(existentEmail);
        assertThat(returnBillDTO.getAccountsList().get(0).getStatus()).isEqualTo(InvitationStatusEnum.ACCEPTED);
    }

    @Test
    @DisplayName("Should save bill to user with null AccountBill percentage in database")
    void shouldSaveBillNullPercentageToUserInDatabase() {

        // Given
        final var billDTO = BillDTOFixture.getDefault();
        final String testEmail = "test@email.com";

        // When
        final BillCompleteDTO returnBillDTO = billFacade.addPersonalBill(testEmail, billDTO);

        // Then
        final var account = accountRepository.getAccountByEmail(testEmail);

        final var bill = billRepository.findById(returnBillDTO.getId()).orElseThrow();
        final Set<AccountBill> accounts = bill.getAccounts();

        assertEquals(1, accounts.size());
        final AccountBill accountBill = accounts.iterator().next();
        assertNull(accountBill.getPercentage());
        assertEquals(account, accountBill.getAccount());


    }

    @Test
    @DisplayName("Should return an exception if the account does not exist in getAllBills")
    void shouldReturnExceptionIfAccountDoesNotExistInGetAllBills() {
        // Given
        final String testEmail = "abc@123.ca";

        // When/Then
        final ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class,
                () -> billFacade.getAllBillsByEmail(testEmail));
        assertEquals("Account does not exist", resourceNotFoundException.getMessage());
    }

    @Test
    @DisplayName("Should return two mapped BillCompleteDTO in getAllBills")
    void shouldReturn2BillCompleteDTO() {
        //Given
        final var testEmail = "test@email.com";
        final var billIdsToCompare = new HashSet<Long>();
        billIdsToCompare.add(1000L);
        billIdsToCompare.add(1001L);

        //When
        final List<BillCompleteDTO> allBillsByEmail = billFacade.getAllBillsByEmail(testEmail);

        //Then
        assertEquals(2, allBillsByEmail.size());
        final Iterable<Bill> billsByTwoIds = billRepository.findAllById(billIdsToCompare);

        //Assuming both the list + iterator are sorted in the same fashion
        verifyBillDTOToBill(allBillsByEmail.get(0), billsByTwoIds.iterator().next());
        verifyBillDTOToBill(allBillsByEmail.get(1), billsByTwoIds.iterator().next());
    }

    @Test
    @DisplayName("Should return emptyList in getAllBills")
    void shouldReturnEmptyList() {
        //Given
        final var testEmail = "user@user.com";

        //When
        final List<BillCompleteDTO> allBillsByEmail = billFacade.getAllBillsByEmail(testEmail);

        //Then
        assertEquals(0, allBillsByEmail.size());
    }

    private void verifyBillDTOToBill(BillCompleteDTO returnBillDTO, Bill bill) {
        assertEquals(BillStatusEnum.OPEN, bill.getStatus());
        assertEquals(bill.getCategory(), returnBillDTO.getCategory());
        assertEquals(bill.getCompany(), returnBillDTO.getCompany());
        final List<ItemDTO> items = returnBillDTO.getItems();
        final Set<Item> billItems = bill.getItems();
        assertEquals(billItems.size(), items.size());
        assertEquals(returnBillDTO.getAccountsList().size(), bill.getAccounts().size() - 1);

        if (!items.isEmpty()) {
            //for the time being we verify only 1 item. Should be generic when needed.
            final ItemDTO returnItemDTO = items.get(0);
            final Item item = billItems.iterator().next();
            assertEquals(item.getName(), returnItemDTO.getName());
            assertEquals(item.getCost(), returnItemDTO.getCost());
            assertEquals(bill.getName(), returnBillDTO.getName());
            assertEquals(item.getCost(), returnBillDTO.getBalance());
            assertEquals(bill.getId(), returnBillDTO.getId());
            assertEquals(bill.getStatus(), returnBillDTO.getStatus());
            assertEquals(bill.getCreated(), returnBillDTO.getCreated());
            assertEquals(bill.getUpdated(), returnBillDTO.getUpdated());
        } else {
            assertEquals(0, BigDecimal.ZERO.compareTo(returnBillDTO.getBalance()));
        }


        final Account account = bill.getAccounts().stream().map(AccountBill::getAccount)
                .filter(acc -> acc.equals(bill.getCreator()))
                .iterator().next();
        assertEquals(account.getId(), returnBillDTO.getCreator().getId());
        assertEquals(account.getId(), returnBillDTO.getResponsible().getId());
    }


}