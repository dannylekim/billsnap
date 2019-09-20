package proj.kedabra.billsnap.business.facade.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.Data;

import proj.kedabra.billsnap.business.dto.AccountDTO;
import proj.kedabra.billsnap.business.dto.AssociateBillDTO;
import proj.kedabra.billsnap.business.dto.BillCompleteDTO;
import proj.kedabra.billsnap.business.dto.BillDTO;
import proj.kedabra.billsnap.business.dto.BillSplitDTO;
import proj.kedabra.billsnap.business.dto.ItemAssociationSplitDTO;
import proj.kedabra.billsnap.business.dto.ItemPercentageSplitDTO;
import proj.kedabra.billsnap.business.entities.Account;
import proj.kedabra.billsnap.business.entities.AccountBill;
import proj.kedabra.billsnap.business.entities.AccountItem;
import proj.kedabra.billsnap.business.entities.Bill;
import proj.kedabra.billsnap.business.entities.Item;
import proj.kedabra.billsnap.business.facade.BillFacade;
import proj.kedabra.billsnap.business.mapper.AccountMapper;
import proj.kedabra.billsnap.business.mapper.BillMapper;
import proj.kedabra.billsnap.business.mapper.ItemMapper;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.business.service.BillService;

@Service
public class BillFacadeImpl implements BillFacade {

    private final AccountRepository accountRepository;

    private final BillService billService;

    private final BillMapper billMapper;

    private final AccountMapper accountMapper;

    private final ItemMapper itemMapper;

    private static final BigDecimal PERCENTAGE_DIVISOR = BigDecimal.valueOf(100);

    private static final String ACCOUNT_DOES_NOT_EXIST = "Account does not exist";

    private static final String LIST_ACCOUNT_DOES_NOT_EXIST = "One or more accounts in the list of accounts does not exist: ";

    private static final String LIST_CANNOT_CONTAIN_BILL_CREATOR = "List of emails cannot contain bill creator email";

    private static final String ITEM_PERCENTAGES_MUST_ADD_TO_100 = "The percentage split for this item must add up to 100: {%s, Percentage: %s}";

    private static final String MUST_HAVE_ONLY_ONE_TYPE_OF_TIPPING = "Only one type of tipping is supported. Please make sure only either tip amount or tip percent is set.";

    @Autowired
    public BillFacadeImpl(final AccountRepository accountRepository, final BillService billService, final BillMapper billMapper, final AccountMapper accountMapper, ItemMapper itemMapper) {
        this.accountRepository = accountRepository;
        this.billService = billService;
        this.billMapper = billMapper;
        this.accountMapper = accountMapper;
        this.itemMapper = itemMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillCompleteDTO addPersonalBill(final String email, final BillDTO billDTO) {

        validateBillDTO(email, billDTO);

        final Account account = Optional.ofNullable(accountRepository.getAccountByEmail(email))
                .orElseThrow(() -> new ResourceNotFoundException(ACCOUNT_DOES_NOT_EXIST));
        final List<Account> accountsList = getRepositoryAccountsList(billDTO.getAccountsList());
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillSplitDTO associateAccountsToBill(final AssociateBillDTO associateBillDTO) {
        final Bill bill = billService.associateItemToAccountBill(associateBillDTO);

        return getBillSplitDTO(bill);
    }

    //TODO should move these things into the billMapperObject itself. Mapstruct has a way to add mapping methods.
    private BillCompleteDTO getBillCompleteDTO(Bill bill) {
        final BigDecimal balance = calculateBalance(bill);
        final BillCompleteDTO billCompleteDTO = billMapper.toBillCompleteDTO(bill);

        final List<Account> accountList = bill.getAccounts().stream()
                .map(AccountBill::getAccount)
                .filter(acc -> !acc.getEmail().equals(bill.getCreator().getEmail()))
                .collect(Collectors.toList());
        final List<AccountDTO> accountDTOList = accountList.stream().map(accountMapper::toDTO).collect(Collectors.toList());

        billCompleteDTO.setBalance(balance);
        billCompleteDTO.setAccountsList(accountDTOList);

        return billCompleteDTO;
    }

    private BillSplitDTO getBillSplitDTO(Bill bill) {
        final BillSplitDTO billSplitDTO = billMapper.toBillSplitDTO(bill);
        final BigDecimal totalTip = calculateTip(bill);
        final BigDecimal balance = calculateBalance(bill);
        billSplitDTO.setTotalTip(totalTip);
        billSplitDTO.setBalance(balance);

        mapAccountTotalCostIntoBillSplitDTO(bill, billSplitDTO);

        return billSplitDTO;
    }

    @SuppressWarnings("BigDecimalMethodWithoutRoundingCalled")
    private void mapAccountTotalCostIntoBillSplitDTO(Bill bill, BillSplitDTO billSplitDTO) {
        final HashMap<Account, Pair> accountPairMap = new HashMap<>();
        bill.getAccounts().stream().map(AccountBill::getAccount).forEach(account -> {
            Pair pair = new Pair(BigDecimal.ZERO, new ArrayList<>());
            accountPairMap.put(account, pair);
        });

        final List<ItemAssociationSplitDTO> itemsPerAccount = new ArrayList<>();

        for (Item item : bill.getItems()) {
            BigDecimal itemPercentageSplitTotal = BigDecimal.ZERO;
            for (AccountItem accountItem : item.getAccounts()) {
                final Account thisAccount = accountItem.getAccount();
                final ItemPercentageSplitDTO itemPercentageSplitDTO = itemMapper.toItemPercentageSplitDTO(item);
                BigDecimal itemPercentage = accountItem.getPercentage();
                itemPercentageSplitDTO.setPercentage(itemPercentage);
                itemPercentageSplitTotal = itemPercentageSplitTotal.add(itemPercentage);
                final BigDecimal itemCostForAccount = item.getCost().multiply(itemPercentage.divide(PERCENTAGE_DIVISOR));
                final BigDecimal newAccountTotalCost = accountPairMap.get(thisAccount).getCost().add(itemCostForAccount);
                accountPairMap.get(thisAccount).setCost(newAccountTotalCost);
                accountPairMap.get(thisAccount).getItemList().add(itemPercentageSplitDTO);
            }
            if (itemPercentageSplitTotal.compareTo(BigDecimal.valueOf(100)) != 0) {
                throw new IllegalArgumentException(String.format(ITEM_PERCENTAGES_MUST_ADD_TO_100, item.getName(), itemPercentageSplitTotal));
            }
        }

        accountPairMap.forEach((account, pair) -> {
            final ItemAssociationSplitDTO itemSplitDTO = new ItemAssociationSplitDTO();
            itemSplitDTO.setAccount(accountMapper.toDTO(account));
            itemSplitDTO.setCost(pair.getCost());
            itemSplitDTO.setItems(pair.getItemList());
            itemsPerAccount.add(itemSplitDTO);
        });
        billSplitDTO.setItemsPerAccount(itemsPerAccount);

    }

    @SuppressWarnings("BigDecimalMethodWithoutRoundingCalled")
    private BigDecimal calculateTip(final Bill bill) {
        final BigDecimal subTotal = bill.getItems().stream().map(Item::getCost).reduce(BigDecimal.ZERO, BigDecimal::add);

        final BigDecimal tipAmount = Optional.ofNullable(bill.getTipAmount()).orElse(BigDecimal.ZERO);

        final BigDecimal tipPercentAmount = Optional.ofNullable(bill.getTipPercent())
                .map(tipPercent -> tipPercent.divide(PERCENTAGE_DIVISOR))
                .map(subTotal::multiply)
                .orElse(BigDecimal.ZERO);

        return tipAmount.add(tipPercentAmount);
    }

    @SuppressWarnings("BigDecimalMethodWithoutRoundingCalled")
    private BigDecimal calculateTip(final Bill bill, BigDecimal subTotal) {
        final BigDecimal tipAmount = Optional.ofNullable(bill.getTipAmount()).orElse(BigDecimal.ZERO);

        final BigDecimal tipPercentAmount = Optional.ofNullable(bill.getTipPercent())
                .map(tipPercent -> tipPercent.divide(PERCENTAGE_DIVISOR))
                .map(subTotal::multiply)
                .orElse(BigDecimal.ZERO);

        return tipAmount.add(tipPercentAmount);
    }

    //TODO decide on what rounding we want to use
    private BigDecimal calculateBalance(final Bill bill) {
        final BigDecimal subTotal = bill.getItems().stream().map(Item::getCost).reduce(BigDecimal.ZERO, BigDecimal::add);

        final BigDecimal tipTotal = calculateTip(bill, subTotal);

        return subTotal.add(tipTotal);
    }

    private List<Account> getRepositoryAccountsList(List<String> accountsList) {
        final List<Account> repositoryAccountsList = accountRepository.getAccountsByEmailIn(accountsList).collect(Collectors.toList());

        if (accountsList.size() > repositoryAccountsList.size()) {
            final List<String> accountsStringList = repositoryAccountsList.stream().map(Account::getEmail).collect(Collectors.toList());
            final List<String> nonExistentEmails = new ArrayList<>(accountsList);
            nonExistentEmails.removeAll(accountsStringList);
            throw new ResourceNotFoundException(LIST_ACCOUNT_DOES_NOT_EXIST + nonExistentEmails.toString());
        }

        return repositoryAccountsList;
    }

    private void validateBillDTO(String email, BillDTO billDTO) {
        if ((billDTO.getTipAmount() == null) == (billDTO.getTipPercent() == null)) {
            throw new IllegalArgumentException(MUST_HAVE_ONLY_ONE_TYPE_OF_TIPPING);
        }
        if (billDTO.getAccountsList().contains(email)) {
            throw new IllegalArgumentException(LIST_CANNOT_CONTAIN_BILL_CREATOR);
        }
    }

}

@Data
class Pair {

    private BigDecimal cost;

    private List<ItemPercentageSplitDTO> itemList;

    Pair(BigDecimal cost, List<ItemPercentageSplitDTO> itemList) {
        this.cost = cost;
        this.itemList = itemList;
    }
}
