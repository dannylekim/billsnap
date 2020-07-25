package proj.kedabra.billsnap.business.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.dto.AssociateBillDTO;
import proj.kedabra.billsnap.business.dto.EditBillDTO;
import proj.kedabra.billsnap.business.dto.ItemAssociationDTO;
import proj.kedabra.billsnap.business.dto.ItemDTO;
import proj.kedabra.billsnap.business.dto.ItemPercentageDTO;
import proj.kedabra.billsnap.business.exception.AccessForbiddenException;
import proj.kedabra.billsnap.business.exception.FunctionalWorkflowException;
import proj.kedabra.billsnap.business.exception.ResourceNotFoundException;
import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.model.entities.AccountBill;
import proj.kedabra.billsnap.business.model.entities.AccountItem;
import proj.kedabra.billsnap.business.model.entities.Bill;
import proj.kedabra.billsnap.business.model.entities.Item;
import proj.kedabra.billsnap.business.model.entities.Tax;
import proj.kedabra.billsnap.business.model.projections.PaymentOwed;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.business.repository.BillRepository;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.InvitationStatusEnum;
import proj.kedabra.billsnap.fixtures.AccountEntityFixture;
import proj.kedabra.billsnap.fixtures.AssociateBillDTOFixture;
import proj.kedabra.billsnap.fixtures.BillDTOFixture;
import proj.kedabra.billsnap.fixtures.BillEntityFixture;
import proj.kedabra.billsnap.fixtures.EditBillDTOFixture;
import proj.kedabra.billsnap.fixtures.GetBillPaginationDTOFixture;
import proj.kedabra.billsnap.fixtures.ItemAssociationDTOFixture;
import proj.kedabra.billsnap.fixtures.ItemPercentageDTOFixture;
import proj.kedabra.billsnap.presentation.resources.OrderByEnum;
import proj.kedabra.billsnap.presentation.resources.SortByEnum;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;
import proj.kedabra.billsnap.utils.SpringProfiles;

@Tag("integration")
@ActiveProfiles(SpringProfiles.TEST)
@SpringBootTest
@Transactional
@AutoConfigureTestDatabase
class BillServiceImplIT {

    @Autowired
    private BillServiceImpl billService;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    @DisplayName("Should return a default bill")
    void shouldReturnDefaultBill() {
        //Given
        final var billDTO = BillDTOFixture.getDefault();
        final Account account = accountRepository.getAccountByEmail("test@email.com");

        //When
        final Bill bill = billService.createBillToAccount(billDTO, account, new ArrayList<>());

        //Then
        final Set<AccountBill> accounts = bill.getAccounts();
        assertEquals(1, accounts.size());

        final AccountBill accountBill = accounts.iterator().next();
        assertEquals(account, accountBill.getAccount());
        assertTrue(bill.getActive());
        assertEquals(billDTO.getCategory(), bill.getCategory());
        assertEquals(billDTO.getCompany(), bill.getCompany());
        assertEquals(billDTO.getName(), bill.getName());
        assertEquals(billDTO.getTipAmount(), bill.getTipAmount());
        assertEquals(billDTO.getTipPercent(), bill.getTipPercent());
        assertEquals(billDTO.getItems().size(), bill.getItems().size());
        assertNull(accountBill.getPercentage());
        assertEquals(InvitationStatusEnum.ACCEPTED, accountBill.getStatus());
        assertEquals(accountBill.getBill(), bill);

        final ItemDTO itemDTO = billDTO.getItems().get(0);
        final Item itemReturned = bill.getItems().iterator().next();
        assertEquals(itemDTO.getName(), itemReturned.getName());
        assertEquals(itemDTO.getCost(), itemReturned.getCost());

        final AccountItem accountItem = itemReturned.getAccounts().iterator().next();
        assertEquals(accountItem.getAccount(), account);
        assertEquals(accountItem.getPercentage(), new BigDecimal(100));
    }

    @Test
    @DisplayName("Should have bill saved in the database")
    void shouldSaveBillinDb() {
        //Given
        final var billDTO = BillDTOFixture.getDefault();
        final Account account = accountRepository.getAccountByEmail("test@email.com");

        //When
        final Bill bill = billService.createBillToAccount(billDTO, account, new ArrayList<>());

        //Then
        assertEquals(bill, billRepository.findById(bill.getId()).orElse(null));
    }

    @Test
    @DisplayName("Should save bill with non-empty accountsList in database")
    void shouldSaveBillWithAccountsListInDatabase() {
        //Given
        final var billDTO = BillDTOFixture.getDefault();
        final Account account = accountRepository.getAccountByEmail("test@email.com");
        final List<Account> accountsList = List.of(accountRepository.getAccountByEmail("nobills@inthisemail.com"));

        //When
        final Bill bill = billService.createBillToAccount(billDTO, account, accountsList);

        //Then
        assertEquals(bill, billRepository.findById(bill.getId()).orElse(null));
    }

    @Test
    @DisplayName("Should return bill according to pagination when sorted by creation")
    void shouldReturnBillAccordingToPaginationWhenSortedByCreationAndAccepted() {
        //Given
        final var billPagination1 = GetBillPaginationDTOFixture.getDefault();

        final List<SortByEnum> sortByList = new ArrayList<>();
        sortByList.add(SortByEnum.CREATED);
        final var billPagination2 = GetBillPaginationDTOFixture.getCustom("restaurant", OrderByEnum.ASC, sortByList, 1, 2);
        final var billPagination3 = GetBillPaginationDTOFixture.getCustom("restaurant", OrderByEnum.DESC, sortByList, 0, 2);

        //When
        final List<Bill> result1 = billService.getAllBillsByAccountPageable(billPagination1).collect(Collectors.toList());
        final List<Bill> result2 = billService.getAllBillsByAccountPageable(billPagination2).collect(Collectors.toList());
        final List<Bill> result3 = billService.getAllBillsByAccountPageable(billPagination3).collect(Collectors.toList());

        //Then
        assertThat(result1).hasSize(2);
        assertThat(result1.get(0).getName()).isEqualTo("bill pagination 2");
        assertThat(result1.get(1).getName()).isEqualTo("bill pagination 3");

        assertThat(result2).hasSize(1);
        assertThat(result2.get(0).getName()).isEqualTo("bill pagination 4");

        assertThat(result3).hasSize(2);
        assertThat(result3.get(0).getName()).isEqualTo("bill pagination 4");
        assertThat(result3.get(1).getName()).isEqualTo("bill pagination 3");

        assertThat(result1.stream().map(Bill::getAccounts).findFirst().orElse(Set.of()).stream().map(AccountBill::getStatus).allMatch(InvitationStatusEnum.ACCEPTED::equals)).isTrue();
        assertThat(result2.stream().map(Bill::getAccounts).findFirst().orElse(Set.of()).stream().map(AccountBill::getStatus).allMatch(InvitationStatusEnum.ACCEPTED::equals)).isTrue();
        assertThat(result3.stream().map(Bill::getAccounts).findFirst().orElse(Set.of()).stream().map(AccountBill::getStatus).allMatch(InvitationStatusEnum.ACCEPTED::equals)).isTrue();

    }

    @Test
    @DisplayName("Should return bill according to pagination when sorted by status")
    void shouldReturnBillAccordingToPaginationWhenSortedByStatus() {
        //Given
        final List<SortByEnum> sortByList = new ArrayList<>();
        sortByList.add(SortByEnum.STATUS);
        final var billPagination = GetBillPaginationDTOFixture.getCustom("restaurant" , OrderByEnum.DESC, sortByList, 0, 2);
        final var billPagination1 = GetBillPaginationDTOFixture.getCustom("restaurant" , OrderByEnum.ASC, sortByList, 0, 5);

        //When
        final List<Bill> result = billService.getAllBillsByAccountPageable(billPagination).collect(Collectors.toList());
        final List<Bill> result1 = billService.getAllBillsByAccountPageable(billPagination1).collect(Collectors.toList());

        //Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("bill pagination 4");

        assertThat(result1).hasSize(3);
        assertThat(result1.get(0).getName()).isEqualTo("bill pagination 3");
    }

    @Test
    @DisplayName("Should return bill according to pagination when sorted by category")
    void shouldReturnBillAccordingToPaginationWhenSortedByCategory() {
        //Given
        final List<SortByEnum> sortByList = new ArrayList<>();
        sortByList.add(SortByEnum.CATEGORY);
        final var billPagination = GetBillPaginationDTOFixture.getCustom(null, OrderByEnum.ASC, sortByList, 0, 2);

        //When
        final List<Bill> result = billService.getAllBillsByAccountPageable(billPagination).collect(Collectors.toList());

        //Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("bill pagination 5");
    }

    @Test
    @DisplayName("Should return pending bills")
    void shouldReturnBillPendingInvitationStatus() {
        //Given
        final var billPagination = GetBillPaginationDTOFixture.getDefault();
        billPagination.setInvitationStatus(InvitationStatusEnum.PENDING);

        //When
        final List<Bill> result = billService.getAllBillsByAccountPageable(billPagination).collect(Collectors.toList());

        //Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("bill pagination 7");
    }

    @Test
    @DisplayName("Should return correct summation amount of amount owed per email")
    void shouldReturnCorrectSummationAmountOfOwedPerEmail() {
        //Given
        var account = AccountEntityFixture.getDefaultAccount();
        account.setId(4000L);

        //When
        final List<PaymentOwed> paymentOwedList = billService.getAllAmountOwedByStatusAndAccount(BillStatusEnum.OPEN, account).collect(Collectors.toList());

        //Then
        assertThat(paymentOwedList.get(0).getEmail()).isEqualTo("user@user.com");
        assertThat(paymentOwedList.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("133.00"));
        assertThat(paymentOwedList.get(1).getEmail()).isEqualTo("userdetails@service.com");
        assertThat(paymentOwedList.get(1).getAmount()).isEqualByComparingTo(new BigDecimal("489.00"));
    }

    @Test
    @DisplayName("Should return empty array for payments owed to oneself")
    void shouldReturnEmptyListIfSoleResponsible() {
        //Given
        var account = AccountEntityFixture.getDefaultAccount();
        account.setId(8000L);

        //When
        final List<PaymentOwed> paymentOwedList = billService.getAllAmountOwedByStatusAndAccount(BillStatusEnum.OPEN, account).collect(Collectors.toList());

        //Then
        assertThat(paymentOwedList.size()).isZero();
    }

    @Test
    @DisplayName("Should return exception if bill does not exist")
    void shouldReturnExceptionIfBillDoesNotExist() {
        //Given/when/then
        assertThrows(ResourceNotFoundException.class, () -> billService.getBill(12366L));
    }

    @Test
    @DisplayName("Should return bill")
    void shouldReturnTargettedBill() {
        //Given
        final long id = 1000L;

        //When
        final Bill bill = billService.getBill(id);

        //Then
        assertThat(bill.getId()).isEqualTo(id);
    }


    @Test
    @DisplayName("Should throw error if referenced bill does not exist")
    void shouldThrowErrorIfBillDoesNotExist() {
        //Given
        final var associateBill = AssociateBillDTOFixture.getDefault();
        final Long id = 185102L;
        associateBill.setId(id);

        //When/Then
        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() -> billService.associateItemsToAccountBill(associateBill)).withMessage(ErrorMessageEnum.BILL_ID_DOES_NOT_EXIST.getMessage(id.toString()));
    }

    @Test
    @DisplayName("Should throw error on specific item/s if does not exist")
    void shouldThrowErrorIfItemDoesNotExistInBill() {
        //Given
        final AssociateBillDTO associateBillDTO = AssociateBillDTOFixture.getDefault();
        final List<ItemPercentageDTO> items = associateBillDTO.getItems().get(0).getItems();
        items.get(0).setItemId(999L);

        final ItemPercentageDTO itemPercentageDTO = ItemPercentageDTOFixture.getDefault();
        itemPercentageDTO.setItemId(172L);
        items.add(itemPercentageDTO);

        //When/Then
        final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> billService.associateItemsToAccountBill(associateBillDTO));
        assertThat(illegalArgumentException.getMessage()).isEqualTo("Not all items exist in the bill: [999, 172]");

    }

    @Test
    @DisplayName("Should throw error if a specified account is not found on the bill")
    void shouldThrowErrorIfAccountDoesNotExistInBill() {
        //Given
        final AssociateBillDTO associateBillDTO = AssociateBillDTOFixture.getDefault();
        associateBillDTO.getItems().get(0).setEmail("fakeEmails@fake.com");
        final ItemAssociationDTO itemAssociationDTO = ItemAssociationDTOFixture.getDefault();
        itemAssociationDTO.setEmail("anotherFakeEmail@emails.com");
        associateBillDTO.getItems().add(itemAssociationDTO);


        //When/Then
        final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> billService.associateItemsToAccountBill(associateBillDTO));
        assertThat(illegalArgumentException.getMessage()).isEqualTo("Not all accounts are in the bill: [fakeEmails@fake.com, anotherFakeEmail@emails.com]");

    }

    @Test
    @DisplayName("Should modify AccountItem if DTO references an old item that's been associated already")
    void shouldModifyAccountItemIfDTOReferencesOldItemThatHasBeenAssociatedAlready() {
        //Given
        final var associateBillDTO = new AssociateBillDTO();
        associateBillDTO.setId(1005L);

        final var itemAssociationDTO = new ItemAssociationDTO();
        final var accountEmail1 = "user@hasbills.com";
        itemAssociationDTO.setEmail("user@hasbills.com");

        final var itemPercentage1 = new ItemPercentageDTO();
        itemPercentage1.setItemId(1006L);
        itemPercentage1.setPercentage(BigDecimal.valueOf(67));

        final var itemPercentage2Max = new ItemPercentageDTO();
        itemPercentage2Max.setItemId(1007L);
        itemPercentage2Max.setPercentage(BigDecimal.valueOf(100));

        final List<ItemPercentageDTO> itemPercentagesForUser1 = List.of(itemPercentage1, itemPercentage2Max);
        itemAssociationDTO.setItems(itemPercentagesForUser1);

        final var itemAssociationDTO2 = new ItemAssociationDTO();
        final var accountEmail2 = "user@withABill.com";
        itemAssociationDTO2.setEmail("user@withABill.com");

        final var itemPercentageMin3 = new ItemPercentageDTO();
        itemPercentageMin3.setItemId(1006L);
        itemPercentageMin3.setPercentage(BigDecimal.valueOf(33));

        itemAssociationDTO2.setItems(List.of(itemPercentageMin3));

        associateBillDTO.setItems(List.of(itemAssociationDTO, itemAssociationDTO2));

        //When
        final Bill bill = billService.associateItemsToAccountBill(associateBillDTO);

        //Then
        final Item item = bill.getItems().stream().filter(i -> i.getId() == 1006L).findFirst().orElseThrow();
        assertThat(item.getAccounts().size()).isEqualTo(2);
        assertThat(item.getAccounts().stream()
                .filter(ai -> ai.getAccount().getEmail().equals(accountEmail1))
                .findFirst()
                .orElseThrow()
                .getPercentage()).isEqualTo(BigDecimal.valueOf(67));
        assertThat(item.getAccounts().stream()
                .filter(ai -> ai.getAccount().getEmail().equals(accountEmail2))
                .findFirst()
                .orElseThrow()
                .getPercentage()).isEqualTo(BigDecimal.valueOf(33));

        final Item item2 = bill.getItems().stream().filter(i -> i.getId() == 1007L).findFirst().orElseThrow();
        assertThat(item2.getAccounts().size()).isEqualTo(1);
        final AccountItem accountItem2 = item2.getAccounts().iterator().next();
        assertThat(accountItem2.getAccount().getEmail()).isEqualTo(accountEmail1);
        assertThat(accountItem2.getPercentage()).isEqualTo(BigDecimal.valueOf(100));

    }

    @Test
    @DisplayName("Should associate 1 item to 1 account by addition of AccountItem")
    void shouldAssociateAnItemToAnAccount() {
        //Given
        final var associateBill = AssociateBillDTOFixture.getDefault();

        //When
        billService.associateItemsToAccountBill(associateBill);

        //Then
        //knowing that there is only 1 email in this fixture
        final var bill = billRepository.getBillById(associateBill.getId());
        final ItemPercentageDTO itemPercentageDTO = associateBill.getItems().get(0).getItems().get(0);
        final Long associateItemId = itemPercentageDTO.getItemId();
        final BigDecimal percentage = itemPercentageDTO.getPercentage();
        final List<AccountItem> listOfExistingItems = bill.getItems().stream().map(Item::getAccounts).flatMap(Set::stream).filter(accountItem -> accountItem.getItem().getId().equals(associateItemId)).collect(Collectors.toList());

        assertThat(listOfExistingItems.size()).isEqualTo(1);
        assertThat(listOfExistingItems.get(0).getPercentage()).isEqualTo(percentage);
    }

    @Test
    @DisplayName("Should not modify AccountItems that aren't referenced in the object")
    void shouldNotModifyUnReferencedAccountItems() {
        //Given
        final var associateBill = AssociateBillDTOFixture.getDefault();
        associateBill.setId(1251L);
        associateBill.getItems().get(0).setEmail("associateitem2@test.com");
        associateBill.getItems().get(0).getItems().get(0).setItemId(1010L);

        //When
        billService.associateItemsToAccountBill(associateBill);

        //Then
        final var account = accountRepository.getAccountByEmail("associateitem3@test.com");
        final var accountItems = account.getItems();
        final Long associateItemId = 1009L;

        final List<AccountItem> listOfExistingItems = accountItems.stream()
                .filter(accountItem -> accountItem.getItem().getId().equals(associateItemId)).collect(Collectors.toList());

        assertThat(listOfExistingItems.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should create a Notification when inviting a Registered user to Bill that is not already part of the Bill")
    void shouldCreateNotificationWhenInviteRegisteredOneUser() {
        //Given account with no notifications
        final var bill = BillEntityFixture.getDefault();
        bill.setId(1000L);
        final var accountNotInBill = AccountEntityFixture.getDefaultAccount();
        final var emailNotInBill = "nobills@inthisemail.com";
        accountNotInBill.setEmail(emailNotInBill);
        final var accountsList = List.of(accountNotInBill);
        final var originalAccountNotificationsSize = accountNotInBill.getNotifications().size();
        final var originalBillNotificationsSize = bill.getNotifications().size();

        //When
        billService.inviteRegisteredToBill(bill, accountsList);

        //Then
        final var notification = bill.getNotifications().iterator().next();
        assertThat(accountNotInBill.getNotifications().size()).isEqualTo(originalAccountNotificationsSize + 1);
        assertThat(bill.getNotifications().size()).isEqualTo(originalBillNotificationsSize + 1);
        assertThat(notification.getAccount()).isEqualTo(accountNotInBill);
        assertThat(notification.getBill()).isEqualTo(bill);
        assertThat(notification.getTimeSent()).isCloseTo(ZonedDateTime.now(ZoneId.systemDefault()), within(200, ChronoUnit.MILLIS));
    }

    @Test
    @DisplayName("Should create a Notification when inviting many Registered users to Bill that are not already part of the Bill")
    void shouldCreateNotificationWhenInviteRegisteredManyUsers() {
        //Given
        final var bill = BillEntityFixture.getDefault();
        bill.setId(1000L);
        final var accountNotInBill = AccountEntityFixture.getDefaultAccount();
        final var emailNotInBill = "nobills@inthisemail.com";
        accountNotInBill.setEmail(emailNotInBill);
        final var secondAccountNotInBill = AccountEntityFixture.getDefaultAccount();
        final var secondEmailNotInBill = "user@hasbills.com";
        secondAccountNotInBill.setEmail(secondEmailNotInBill);
        final var accountsList = List.of(accountNotInBill, secondAccountNotInBill);

        //When
        billService.inviteRegisteredToBill(bill, accountsList);

        //Then
        assertThat(bill.getNotifications().size()).isEqualTo(2);
        assertThat(bill.getNotifications().stream().anyMatch(n -> n.getAccount().equals(accountNotInBill))).isTrue();
        assertThat(bill.getNotifications().stream().anyMatch(n -> n.getAccount().equals(secondAccountNotInBill))).isTrue();

    }

    @Test
    @DisplayName("Should create an AccountBill with Pending status when inviting a Registered user to Bill that is not already part of the Bill")
    void shouldCreateAccountBillWhenInviteRegisteredOneUser() {
        //Given an account with no bills
        final var bill = BillEntityFixture.getDefault();
        bill.setId(1000L);
        final var accountNotInBill = AccountEntityFixture.getDefaultAccount();
        final var emailNotInBill = "nobills@inthisemail.com";
        accountNotInBill.setEmail(emailNotInBill);
        final var accountsList = List.of(accountNotInBill);
        final var originalBillAccountBillSize = bill.getAccounts().size();

        //When
        billService.inviteRegisteredToBill(bill, accountsList);

        //Then
        assertThat(bill.getAccounts().size()).isEqualTo(originalBillAccountBillSize + 1);
        final var accountBill = bill.getAccounts().stream().filter(ab -> ab.getAccount().equals(accountNotInBill)).findFirst().orElseThrow();
        assertThat(accountBill.getStatus()).isEqualTo(InvitationStatusEnum.PENDING);
        assertThat(accountBill.getPercentage()).isNull();

    }

    @Test
    @DisplayName("Should create an AccountBill with Pending status when inviting many Registered users to Bill that are not already part of the Bill")
    void shouldCreateAccountBillWhenInviteRegisteredManyUsers() {
        //Given
        final var bill = BillEntityFixture.getDefault();
        bill.setId(1000L);
        final var accountNotInBill = AccountEntityFixture.getDefaultAccount();
        final var emailNotInBill = "nobills@inthisemail.com";
        accountNotInBill.setEmail(emailNotInBill);
        final var secondAccountNotInBill = AccountEntityFixture.getDefaultAccount();
        final var secondEmailNotInBill = "user@hasbills.com";
        secondAccountNotInBill.setEmail(secondEmailNotInBill);
        final var accountsList = List.of(accountNotInBill, secondAccountNotInBill);
        final var originalBillAccountBillSize = bill.getAccounts().size();

        //When
        billService.inviteRegisteredToBill(bill, accountsList);

        //Then
        assertThat(bill.getAccounts().size()).isEqualTo(originalBillAccountBillSize + 2);
        bill.getAccounts().stream()
                .filter(ab -> ab.getAccount().equals(accountNotInBill) || ab.getAccount().equals(secondAccountNotInBill))
                .forEach(accountBill -> {
                    assertThat(accountBill.getStatus()).isEqualTo(InvitationStatusEnum.PENDING);
                    assertThat(accountBill.getPercentage()).isNull();
                });
    }

    @Test
    @DisplayName("Should throw Exception if Associate Users Bill call contains non-integer valued percentages")
    void shouldThrowExceptionIfAssociateCallHasNonIntegerPercentages() {
        //Given bill with 2 users
        final String billResponsible = "user@withABill.com";
        final BigDecimal nonIntegerValue = BigDecimal.valueOf(50.5);
        final long existentBillId = 1003L;

        final var itemAssociationDTO1 = ItemAssociationDTOFixture.getDefault();
        itemAssociationDTO1.setEmail(billResponsible);
        itemAssociationDTO1.getItems().forEach(itemPercentageDTO -> {
            itemPercentageDTO.setItemId(9001L);
            itemPercentageDTO.setPercentage(nonIntegerValue);
        });
        final var itemAssociationDTO2 = ItemAssociationDTOFixture.getDefault();

        final var associateBillDTO = AssociateBillDTOFixture.getDefault();
        associateBillDTO.setItems(List.of(itemAssociationDTO1, itemAssociationDTO2));
        associateBillDTO.setId(existentBillId);

        //When/Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> billService.associateItemsToAccountBill(associateBillDTO))
                .withMessage(ErrorMessageEnum.GIVEN_VALUES_NOT_INTEGER_VALUED.getMessage(List.of(nonIntegerValue).toString()));
    }

    @Test
    @DisplayName("Should throw Exception if /PATCH Associate Users Bill call contains duplicate email")
    void shouldThrowExceptionIfAssociateBillCallContainsDuplicateEmail() {
        //Given bill with 2 users
        final String billResponsible = "user@withABill.com";
        final BigDecimal fifty = BigDecimal.valueOf(50);

        final var itemAssociationDTO1 = ItemAssociationDTOFixture.getDefault();
        itemAssociationDTO1.setEmail(billResponsible);
        itemAssociationDTO1.getItems().forEach(itemPercentageDTO -> {
            itemPercentageDTO.setItemId(9001L);
            itemPercentageDTO.setPercentage(fifty);
        });
        final var itemAssociationDTO2 = ItemAssociationDTOFixture.getDefault();
        itemAssociationDTO2.setEmail(billResponsible);
        itemAssociationDTO2.getItems().forEach(itemPercentageDTO -> {
            itemPercentageDTO.setItemId(8999L);
            itemPercentageDTO.setPercentage(fifty);
        });

        final var associateBillDTO = AssociateBillDTOFixture.getDefault();
        associateBillDTO.setItems(List.of(itemAssociationDTO1, itemAssociationDTO2));

        //When/Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> billService.associateItemsToAccountBill(associateBillDTO))
                .withMessage(ErrorMessageEnum.DUPLICATE_EMAILS_IN_ASSOCIATE_USERS.getMessage(List.of(billResponsible).toString()));
    }

    @Test
    @DisplayName("Should throw Exception if /PATCH Associate Users Bill call contains user not in bill")
    void shouldThrowExceptionIfAssociateBillCallContainsUserNotInBill() {
        //Given bill with 2 users
        final String billResponsible = "user@withABill.com";
        final String userNotInBill = "notinbill@email.com";
        final BigDecimal fifty = BigDecimal.valueOf(50);
        final long existentBillId = 1003L;

        final var itemAssociationDTO1 = ItemAssociationDTOFixture.getDefault();
        itemAssociationDTO1.setEmail(billResponsible);
        itemAssociationDTO1.getItems().forEach(itemPercentageDTO -> {
            itemPercentageDTO.setItemId(9001L);
            itemPercentageDTO.setPercentage(fifty);
        });
        final var itemAssociationDTO2 = ItemAssociationDTOFixture.getDefault();
        itemAssociationDTO2.setEmail(userNotInBill);
        itemAssociationDTO2.getItems().forEach(itemPercentageDTO -> {
            itemPercentageDTO.setItemId(8999L);
            itemPercentageDTO.setPercentage(fifty);
        });

        final var associateBillDTO = AssociateBillDTOFixture.getDefault();
        associateBillDTO.setItems(List.of(itemAssociationDTO1, itemAssociationDTO2));
        associateBillDTO.setId(existentBillId);

        //When/Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> billService.associateItemsToAccountBill(associateBillDTO))
                .withMessage(ErrorMessageEnum.SOME_ACCOUNTS_NONEXISTENT_IN_BILL.getMessage(List.of(userNotInBill).toString()));
    }

    @Test
    @DisplayName("Should throw Exception if /PATCH Associate Users Bill call contains item not in bill")
    void shouldThrowExceptionIfAssociateBillCallContainsItemNotInBill() {
        //Given bill with 2 users
        final String billResponsible = "user@withABill.com";
        final String billUser = "user@hasbills.com";
        final long nonExistentItemId = 9999L;
        final long existentItemId = 1004L;
        final long existentBillId = 1003L;
        final BigDecimal fifty = BigDecimal.valueOf(50);

        final var itemAssociationDTO1 = ItemAssociationDTOFixture.getDefault();
        itemAssociationDTO1.setEmail(billResponsible);
        itemAssociationDTO1.getItems().forEach(itemPercentageDTO -> {
            itemPercentageDTO.setItemId(existentItemId);
            itemPercentageDTO.setPercentage(fifty);
        });
        final var itemAssociationDTO2 = ItemAssociationDTOFixture.getDefault();
        itemAssociationDTO2.setEmail(billUser);
        itemAssociationDTO2.getItems().forEach(itemPercentageDTO -> {
            itemPercentageDTO.setItemId(nonExistentItemId);
            itemPercentageDTO.setPercentage(fifty);
        });

        final var associateBillDTO = AssociateBillDTOFixture.getDefault();
        associateBillDTO.setItems(List.of(itemAssociationDTO1, itemAssociationDTO2));
        associateBillDTO.setId(existentBillId);

        //When/Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> billService.associateItemsToAccountBill(associateBillDTO))
                .withMessage(ErrorMessageEnum.SOME_ITEMS_NONEXISTENT_IN_BILL.getMessage(List.of(nonExistentItemId).toString()));
    }

    @Test
    @DisplayName("Should return Bill with new Associations in /PATCH bills")
    void shouldReturnBillOnSuccessPATCHAssociateBill() {
        //Given bill with 2 users
        final String billResponsible = "user@withABill.com";
        final String billUser = "user@hasbills.com";
        final long existentItemId = 1004L;
        final long existentBillId = 1003L;
        final BigDecimal fifty = BigDecimal.valueOf(50);

        final var itemAssociationDTO1 = ItemAssociationDTOFixture.getDefault();
        itemAssociationDTO1.setEmail(billResponsible);
        itemAssociationDTO1.getItems().forEach(itemPercentageDTO -> {
            itemPercentageDTO.setItemId(existentItemId);
            itemPercentageDTO.setPercentage(fifty);
        });
        final var itemAssociationDTO2 = ItemAssociationDTOFixture.getDefault();
        itemAssociationDTO2.setEmail(billUser);
        itemAssociationDTO2.getItems().forEach(itemPercentageDTO -> {
            itemPercentageDTO.setItemId(existentItemId);
            itemPercentageDTO.setPercentage(fifty);
        });

        final var associateBillDTO = AssociateBillDTOFixture.getDefault();
        associateBillDTO.setItems(List.of(itemAssociationDTO1, itemAssociationDTO2));
        associateBillDTO.setId(existentBillId);

        //When
        final Bill returnedBill = billService.associateItemsToAccountBill(associateBillDTO);

        //Then
        final List<AccountItem> listAccountItems = returnedBill.getItems().stream()
                .map(Item::getAccounts).flatMap(Set::stream)
                .filter(ai -> ai.getPercentage().compareTo(fifty) == 0).collect(Collectors.toList());
        assertThat(listAccountItems.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should edit bill successfully with tip percentage")
    void shouldEditBillSuccessfullyWithTipPercentage() {
        //Given
        final Account account = accountRepository.getAccountByEmail("editbill@email.com");
        final EditBillDTO editBill = EditBillDTOFixture.getDefault();
        editBill.setResponsible("editbill@email.com");
        editBill.setTipPercent(null);
        editBill.setTipAmount(BigDecimal.valueOf(15));
        editBill.getTaxes().clear();
        final var existentBillId = 2001L;

        final var billInsideDb = billRepository.getBillById(existentBillId);
        final var item = new Item();
        item.setBill(billInsideDb);
        item.setName("");
        item.setCost(BigDecimal.TEN);
        item.setId(1000L);
        billInsideDb.getItems().add(item);
        billRepository.save(billInsideDb);

        //When
        final Bill editedBill = billService.editBill(existentBillId, account, editBill);

        //Then
        assertThat(editedBill.getName()).isEqualTo(editBill.getName());
        assertThat(editedBill.getResponsible().getEmail()).isEqualTo(editBill.getResponsible());
        assertThat(editedBill.getCompany()).isEqualTo(editBill.getCompany());
        assertThat(editedBill.getCategory()).isEqualTo(editBill.getCategory());
        assertThat(editedBill.getTipPercent()).isEqualTo(editBill.getTipPercent());

        final var items = new ArrayList<>(editedBill.getItems());
        assertThat(items.size()).isEqualTo(editBill.getItems().size());
        if (items.get(0).getId().equals(editBill.getItems().get(0).getId())) {
            assertThat(items.get(0).getId()).isEqualTo(editBill.getItems().get(0).getId());
            assertThat(items.get(0).getCost()).isEqualByComparingTo(BigDecimal.TEN);
            assertThat(items.get(1).getId()).isNotNull();
            assertThat(items.get(1).getCost()).isEqualByComparingTo(editBill.getItems().get(1).getCost());
        } else {
            assertThat(items.get(0).getId()).isNotNull();
            assertThat(items.get(0).getCost()).isEqualByComparingTo(editBill.getItems().get(1).getCost());
            assertThat(items.get(1).getId()).isEqualTo(editBill.getItems().get(0).getId());
            assertThat(items.get(1).getCost()).isEqualByComparingTo(BigDecimal.TEN);

        }
    }

    @Test
    @DisplayName("Should edit bill successfully with tip amount")
    void shouldEditBillSuccessfullyTipAmount() {
        //Given
        final Account account = accountRepository.getAccountByEmail("editbill@email.com");
        final EditBillDTO editBill = EditBillDTOFixture.getDefault();
        editBill.setResponsible("editbill@email.com");
        editBill.setTipPercent(null);
        editBill.setTipAmount(BigDecimal.valueOf(15));
        final var existentBillId = 2001L;

        final var billInsideDb = billRepository.getBillById(existentBillId);
        final var item = new Item();
        item.setBill(billInsideDb);
        item.setName("");
        item.setCost(BigDecimal.TEN);
        item.setId(1000L);
        billInsideDb.getItems().add(item);
        final var tax = new Tax();
        tax.setId(123L);
        tax.setName("Tax 1");
        tax.setPercentage(BigDecimal.TEN);
        tax.setBill(billInsideDb);
        billInsideDb.getTaxes().add(tax);
        billRepository.save(billInsideDb);

        //When
        final Bill editedBill = billService.editBill(existentBillId, account, editBill);

        //Then
        assertThat(editedBill.getName()).isEqualTo(editBill.getName());
        assertThat(editedBill.getResponsible().getEmail()).isEqualTo(editBill.getResponsible());
        assertThat(editedBill.getCompany()).isEqualTo(editBill.getCompany());
        assertThat(editedBill.getCategory()).isEqualTo(editBill.getCategory());
        assertThat(editedBill.getTipAmount()).isEqualTo(editBill.getTipAmount());

        final var items = new ArrayList<>(editedBill.getItems());
        assertThat(items.size()).isEqualTo(editBill.getItems().size());
        final var firstItemDTO = editBill.getItems().get(0);
        final var secondItemDTO = editBill.getItems().get(1);
        if (items.get(0).getId().equals(firstItemDTO.getId())) {
            assertThat(items.get(0).getId()).isEqualTo(firstItemDTO.getId());
            assertThat(items.get(0).getCost()).isEqualByComparingTo(firstItemDTO.getCost());
            assertThat(items.get(1).getId()).isNotNull();
            assertThat(items.get(1).getCost()).isEqualByComparingTo(secondItemDTO.getCost());
        } else {
            assertThat(items.get(0).getId()).isNotNull();
            assertThat(items.get(0).getCost()).isEqualByComparingTo(secondItemDTO.getCost());
            assertThat(items.get(1).getId()).isEqualTo(firstItemDTO.getId());
            assertThat(items.get(1).getCost()).isEqualByComparingTo(firstItemDTO.getCost());

        }
    }

    @Test
    @DisplayName("Should throw exception when account is not part of the bill")
    void shouldThrowExceptionWhenAccountIsNotPartOfTheBill() {
        //Given
        final Account account = accountRepository.getAccountByEmail("nobills@inthisemail.com");
        final EditBillDTO editBill = EditBillDTOFixture.getDefault();
        final var existentBillId = 1000L;

        //When/Then
        assertThatExceptionOfType(AccessForbiddenException.class)
                .isThrownBy(() -> billService.editBill(existentBillId, account, editBill))
                .withMessage(ErrorMessageEnum.USER_IS_NOT_BILL_RESPONSIBLE.getMessage(List.of(existentBillId).toString()));
    }

    @Test
    @DisplayName("Should throw exception when bill already started")
    void shouldThrowExceptionWhenBillAlreadyStarted() {
        //Given
        final Account account = accountRepository.getAccountByEmail("test@email.com");
        final EditBillDTO editBill = EditBillDTOFixture.getDefault();
        final var existentBillId = 1000L;
        billService.startBill(1000L, "test@email.com");

        //When/Then
        assertThatExceptionOfType(FunctionalWorkflowException.class)
                .isThrownBy(() -> billService.editBill(existentBillId, account, editBill))
                .withMessage(ErrorMessageEnum.WRONG_BILL_STATUS.getMessage(BillStatusEnum.OPEN.toString()));
    }

    @Test
    @DisplayName("Should throw exception when responsible is not part of bill")
    void shouldThrowExceptionWhenResponsibleIsNotPartOfBill() {
        //Given
        final Account account = accountRepository.getAccountByEmail("test@email.com");
        final EditBillDTO editBill = EditBillDTOFixture.getDefault();
        editBill.setResponsible("random@email.com");
        final var existentBillId = 1000L;

        //When/Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> billService.editBill(existentBillId, account, editBill))
                .withMessage(ErrorMessageEnum.SOME_ACCOUNTS_NONEXISTENT_IN_BILL.getMessage("random@email.com"));
    }

    @Test
    @DisplayName("Should throw exception if have both tip values for edit bill")
    void shouldThrowExceptionIfTipFormatHasBothTipValues() {
        final Account account = accountRepository.getAccountByEmail("test@email.com");
        final EditBillDTO editBill = EditBillDTOFixture.getDefault();
        editBill.getTaxes().clear();
        editBill.setTipPercent(BigDecimal.ONE);
        editBill.setTipAmount(BigDecimal.valueOf(69));
        final var existentBillId = 1000L;

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> billService.editBill(existentBillId, account, editBill))
                .withMessage(ErrorMessageEnum.MULTIPLE_TIP_METHOD.getMessage());
    }

    @Test
    @DisplayName("Should throw exception if both tip is null")
    void shouldThrowExceptionIfTipFormatIsIncorrectTipAmount() {
        final Account account = accountRepository.getAccountByEmail("editbill@email.com");
        final EditBillDTO editBill = EditBillDTOFixture.getDefault();
        editBill.setTipAmount(null);
        editBill.setTipPercent(null);
        editBill.getTaxes().clear();
        editBill.setResponsible("editbill@email.com");
        final var existentBillId = 2001L;

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> billService.editBill(existentBillId, account, editBill))
                .withMessage(ErrorMessageEnum.MULTIPLE_TIP_METHOD.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when edit bill does not have referenced item")
    void shouldThrowExceptionWhenEditBillDoesNotHaveReferencedItem() {
        //Given
        final Account account = accountRepository.getAccountByEmail("test@email.com");
        final EditBillDTO editBill = EditBillDTOFixture.getDefault();
        editBill.getTaxes().clear();
        editBill.getItems().get(0).setId(1L);
        final var existentBillId = 1000L;

        //When/then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> billService.editBill(existentBillId, account, editBill))
                .withMessage(ErrorMessageEnum.ITEM_ID_DOES_NOT_EXIST.getMessage("1"));
    }
}