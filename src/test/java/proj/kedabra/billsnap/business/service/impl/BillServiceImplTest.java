package proj.kedabra.billsnap.business.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import proj.kedabra.billsnap.business.dto.ItemDTO;
import proj.kedabra.billsnap.business.entities.Account;
import proj.kedabra.billsnap.business.entities.AccountBill;
import proj.kedabra.billsnap.business.entities.AccountItem;
import proj.kedabra.billsnap.business.entities.Bill;
import proj.kedabra.billsnap.business.entities.Item;
import proj.kedabra.billsnap.business.mapper.BillMapper;
import proj.kedabra.billsnap.business.repository.AccountBillRepository;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.business.repository.BillRepository;
import proj.kedabra.billsnap.fixtures.AccountEntityFixture;
import proj.kedabra.billsnap.fixtures.BillDTOFixture;
import proj.kedabra.billsnap.fixtures.BillEntityFixture;

class BillServiceImplTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private AccountBillRepository accountBillRepository;

    @Mock
    private BillMapper billMapper;

    @Mock
    private AccountRepository accountRepository;

    private BillServiceImpl billService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        billService = new BillServiceImpl(billRepository, billMapper, accountBillRepository);
    }

    @Test
    @DisplayName("Should return default bill")
    void ShouldReturnDefaultBill() {
        //Given
        when(accountRepository.getAccountByEmail(any())).thenReturn(AccountEntityFixture.getDefaultAccount());
        when(billMapper.toEntity(any())).thenReturn(BillEntityFixture.getMappedBillDTOFixture());
        when(billRepository.save(any(Bill.class))).thenAnswer(a -> a.getArguments()[0]);

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
    @DisplayName("Should return default bill with valid supplied accounts list")
    void ShouldReturnDefaultBillWithAccountsList() {
        //Given
        final var billDTO = BillDTOFixture.getDefault();
        final var accountEntity = AccountEntityFixture.getDefaultAccount();
        final var billEntity = BillEntityFixture.getMappedBillDTOFixture();
        final var secondAccountEntity = AccountEntityFixture.getDefaultAccount();
        final String someEmail = "some@email.com";
        secondAccountEntity.setEmail(someEmail);

        when(accountRepository.getAccountByEmail(any())).thenReturn(accountEntity);
        when(billMapper.toEntity(any())).thenReturn(billEntity);
        when(billRepository.save(any(Bill.class))).thenAnswer(a -> a.getArguments()[0]);

        final Account account = accountRepository.getAccountByEmail("test@email.com");

        //When
        final Bill bill = billService.createBillToAccount(billDTO, account, List.of(secondAccountEntity));

        //Then
        final Set<AccountBill> accounts = bill.getAccounts();
        assertEquals(2, accounts.size());
        final AccountBill creatorAccountBill = accounts.stream()
                .filter(a -> a.getAccount().equals(account))
                .iterator().next();
        assertEquals(account, creatorAccountBill.getAccount());
        assertTrue(bill.getActive());
        assertEquals(billDTO.getCategory(), bill.getCategory());
        assertEquals(billDTO.getCompany(), bill.getCompany());
        assertEquals(billDTO.getName(), bill.getName());
        assertEquals(billDTO.getTipAmount(), bill.getTipAmount());
        assertEquals(billDTO.getTipPercent(), bill.getTipPercent());
        assertEquals(billDTO.getItems().size(), bill.getItems().size());
        assertEquals(creatorAccountBill.getPercentage(), new BigDecimal(100));
        assertEquals(creatorAccountBill.getBill(), bill);
        final AccountBill secondAccountBill = accounts.stream()
                .filter(a -> !a.getAccount().equals(account))
                .iterator().next();
        assertEquals(someEmail, secondAccountBill.getAccount().getEmail());
        final ItemDTO itemDTO = billDTO.getItems().get(0);
        final Item itemReturned = bill.getItems().iterator().next();
        assertEquals(itemDTO.getName(), itemReturned.getName());
        assertEquals(itemDTO.getCost(), itemReturned.getCost());
        final AccountItem accountItem = itemReturned.getAccounts().iterator().next();
        assertEquals(accountItem.getAccount(), account);
        assertEquals(accountItem.getPercentage(), new BigDecimal(100));
    }
}