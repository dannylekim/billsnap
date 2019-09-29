package proj.kedabra.billsnap.business.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.dto.ItemDTO;
import proj.kedabra.billsnap.model.entities.Account;
import proj.kedabra.billsnap.model.entities.AccountBill;
import proj.kedabra.billsnap.model.entities.AccountItem;
import proj.kedabra.billsnap.model.entities.Bill;
import proj.kedabra.billsnap.model.entities.Item;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.business.repository.BillRepository;
import proj.kedabra.billsnap.fixtures.AccountEntityFixture;
import proj.kedabra.billsnap.fixtures.BillDTOFixture;
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
        assertEquals(accountBill.getPercentage(), new BigDecimal(100));
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
        final List<Account> accountsList = List.of(AccountEntityFixture.getDefaultAccount());

        //When
        final Bill bill = billService.createBillToAccount(billDTO, account, accountsList);

        //Then
        assertEquals(bill, billRepository.findById(bill.getId()).orElse(null));
    }


    @Test
    @DisplayName("Should return all bills saved in database to account")
    void shouldReturnAllBillsInDb() {
        //Given
        //Account with 2 bills
        final Account account = accountRepository.getAccountByEmail("test@email.com");

        //When
        final Stream<Bill> allBillsByAccount = billService.getAllBillsByAccount(account);

        //Then
        assertEquals(2, allBillsByAccount.count());
    }

    @Test
    @DisplayName("Should return empty stream if no bills in account")
    void shouldReturnEmptyList() {
        //Given
        //Account with 0 bills
        final Account account = accountRepository.getAccountByEmail("userdetails@service.com");

        //When
        final Stream<Bill> allBillsByAccount = billService.getAllBillsByAccount(account);

        //Then
        assertEquals(0, allBillsByAccount.count());
    }

}