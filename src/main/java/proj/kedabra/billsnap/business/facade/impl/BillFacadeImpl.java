package proj.kedabra.billsnap.business.facade.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.dto.AccountDTO;
import proj.kedabra.billsnap.business.dto.BillCompleteDTO;
import proj.kedabra.billsnap.business.dto.BillDTO;
import proj.kedabra.billsnap.business.entities.Account;
import proj.kedabra.billsnap.business.entities.AccountBill;
import proj.kedabra.billsnap.business.entities.Bill;
import proj.kedabra.billsnap.business.entities.Item;
import proj.kedabra.billsnap.business.facade.BillFacade;
import proj.kedabra.billsnap.business.mapper.AccountMapper;
import proj.kedabra.billsnap.business.mapper.BillMapper;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.business.service.BillService;

@Service
public class BillFacadeImpl implements BillFacade {

    private final AccountRepository accountRepository;

    private final BillService billService;

    private final BillMapper billMapper;

    private final AccountMapper accountMapper;

    private static final BigDecimal PERCENTAGE_DIVISOR = BigDecimal.valueOf(100);

    private static final String ACCOUNT_DOES_NOT_EXIST = "Account does not exist";

    private static final String LIST_ACCOUNT_DOES_NOT_EXIST = "One or more accounts in the list of accounts does not exist";

    private static final String LIST_CANNOT_CONTAIN_BILL_CREATOR = "List of emails cannot contain bill creator email";

    @Autowired
    public BillFacadeImpl(final AccountRepository accountRepository, final BillService billService, final BillMapper billMapper, final AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.billService = billService;
        this.billMapper = billMapper;
        this.accountMapper = accountMapper;
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillCompleteDTO addPersonalBill(final String email, final BillDTO billDTO) {

        validateBillDTO(email, billDTO);

        final Account account = Optional.ofNullable(accountRepository.getAccountByEmail(email))
                .orElseThrow(() -> new ResourceNotFoundException(ACCOUNT_DOES_NOT_EXIST));
        final List<Account> accountsList = accountRepository.getAccountsByEmailIn(billDTO.getAccountsList()).collect(Collectors.toList());
        final List<String> billDTOAccounts = billDTO.getAccountsList();

        if (billDTOAccounts.size() > accountsList.size()) {
            final List<String> accountsStringList = accountsList.stream().map(Account::getEmail).collect(Collectors.toList());
            final List<String> nonExistentEmails = new ArrayList<>(billDTOAccounts);
            nonExistentEmails.removeAll(accountsStringList);
            throw new ResourceNotFoundException(LIST_ACCOUNT_DOES_NOT_EXIST + ": " + nonExistentEmails.toString());
        }

        final Bill bill = billService.createBillToAccount(billDTO, account, accountsList);

        return getBillCompleteDTO(bill);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BillCompleteDTO> getAllBillsByEmail(String email) {
        final Account account = Optional.ofNullable(accountRepository.getAccountByEmail(email))
                .orElseThrow(() -> new ResourceNotFoundException(ACCOUNT_DOES_NOT_EXIST));

        return billService.getAllBillsByAccount(account).map(this::getBillCompleteDTO).collect(Collectors.toList());
    }

    //TODO should move these things into the billMapperObject itself. Mapstruct has a way to add mapping methods.
    private BillCompleteDTO getBillCompleteDTO(Bill bill) {
        final BigDecimal balance = calculateBalance(bill);
        final BillCompleteDTO billCompleteDTO = billMapper.toDTO(bill);

        final List<Account> accountList = bill.getAccounts().stream()
                .map(AccountBill::getAccount)
                .filter(acc -> !acc.getEmail().equals(bill.getCreator().getEmail()))
                .collect(Collectors.toList());
        final List<AccountDTO> accountDTOList = accountList.stream().map(accountMapper::toDTO).collect(Collectors.toList());

        billCompleteDTO.setBalance(balance);
        billCompleteDTO.setAccountsList(accountDTOList);

        return billCompleteDTO;
    }

    //TODO decide on what rounding we want to use
    @SuppressWarnings("BigDecimalMethodWithoutRoundingCalled")
    private BigDecimal calculateBalance(final Bill bill) {
        final BigDecimal subTotal = bill.getItems().stream().map(Item::getCost).reduce(BigDecimal.ZERO, BigDecimal::add);

        final BigDecimal tipAmount = Optional.ofNullable(bill.getTipAmount()).orElse(BigDecimal.ZERO);

        final BigDecimal tipPercentAmount = Optional.ofNullable(bill.getTipPercent())
                .map(tipPercent -> tipPercent.divide(PERCENTAGE_DIVISOR))
                .map(subTotal::multiply)
                .orElse(BigDecimal.ZERO);

        return subTotal.add(tipAmount).add(tipPercentAmount);

    }

    private void validateBillDTO(String email, BillDTO billDTO) {
        if ((billDTO.getTipAmount() == null) == (billDTO.getTipPercent() == null)) {
            throw new IllegalArgumentException("Only one type of tipping is supported. " +
                    "Please make sure only either tip amount or tip percent is set.");
        }
        if (billDTO.getAccountsList().contains(email)) {
            throw new IllegalArgumentException(LIST_CANNOT_CONTAIN_BILL_CREATOR);
        }
    }
}
