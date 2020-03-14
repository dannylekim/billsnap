package proj.kedabra.billsnap.business.facade.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.dto.AssociateBillDTO;
import proj.kedabra.billsnap.business.dto.BillCompleteDTO;
import proj.kedabra.billsnap.business.dto.BillDTO;
import proj.kedabra.billsnap.business.dto.BillSplitDTO;
import proj.kedabra.billsnap.business.dto.CostItemsPair;
import proj.kedabra.billsnap.business.dto.ItemAssociationSplitDTO;
import proj.kedabra.billsnap.business.dto.ItemPercentageSplitDTO;
import proj.kedabra.billsnap.business.dto.PendingRegisteredBillSplitDTO;
import proj.kedabra.billsnap.business.exception.AccessForbiddenException;
import proj.kedabra.billsnap.business.facade.BillFacade;
import proj.kedabra.billsnap.business.mapper.AccountMapper;
import proj.kedabra.billsnap.business.mapper.BillMapper;
import proj.kedabra.billsnap.business.mapper.ItemMapper;
import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.model.entities.AccountBill;
import proj.kedabra.billsnap.business.model.entities.AccountItem;
import proj.kedabra.billsnap.business.model.entities.Bill;
import proj.kedabra.billsnap.business.model.entities.Item;
import proj.kedabra.billsnap.business.service.AccountService;
import proj.kedabra.billsnap.business.service.BillService;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;
import proj.kedabra.billsnap.utils.tuples.AccountStatusPair;

@Service
public class BillFacadeImpl implements BillFacade {

    private final BillService billService;

    private final AccountService accountService;

    private final BillMapper billMapper;

    private final AccountMapper accountMapper;

    private final ItemMapper itemMapper;

    private static final BigDecimal PERCENTAGE_DIVISOR = BigDecimal.valueOf(100);

    private static final String ITEM_PERCENTAGES_MUST_ADD_TO_100 = "The percentage split for this item must add up to 100: {%s, Percentage: %s}";

    @Autowired
    public BillFacadeImpl(final BillService billService, final AccountService accountService, final BillMapper billMapper, final AccountMapper accountMapper, final ItemMapper itemMapper) {
        this.billService = billService;
        this.accountService = accountService;
        this.billMapper = billMapper;
        this.accountMapper = accountMapper;
        this.itemMapper = itemMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillCompleteDTO addPersonalBill(final String email, final BillDTO billDTO) {
        validateBillDTO(email, billDTO);
        final var account = accountService.getAccount(email);
        final var billAccountsList = accountService.getAccounts(billDTO.getAccountsList());
        final Bill bill = billService.createBillToAccount(billDTO, account, billAccountsList);

        return getBillCompleteDTO(bill);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BillSplitDTO> getAllBillsByEmail(final String email) {
        final var account = accountService.getAccount(email);
        return billService.getAllBillsByAccount(account).map(this::getBillSplitDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillSplitDTO associateAccountsToBill(final AssociateBillDTO associateBillDTO) {
        final Bill bill = billService.associateItemsToAccountBill(associateBillDTO);

        return getBillSplitDTO(bill);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PendingRegisteredBillSplitDTO inviteRegisteredToBill(final Long billId, final String userEmail, final List<String> accounts) {

        final var bill = billService.getBill(billId);
        if (!bill.getResponsible().getEmail().equals(userEmail)) {
            throw new AccessForbiddenException(ErrorMessageEnum.USER_IS_NOT_BILL_RESPONSIBLE.getMessage());
        }

        final List<Account> accountsList = accountService.getAccounts(accounts);
        final List<String> emailsList = accountsList.stream().map(Account::getEmail).collect(Collectors.toList());
        final List<String> commonEmailsList = bill.getAccounts().stream()
                .map(AccountBill::getAccount).map(Account::getEmail)
                .filter(emailsList::contains).collect(Collectors.toList());
        if (!commonEmailsList.isEmpty()) {
            throw new IllegalArgumentException(ErrorMessageEnum.LIST_ACCOUNT_ALREADY_IN_BILL.getMessage(commonEmailsList.toString()));
        }

        billService.inviteRegisteredToBill(bill, accountsList);

        final var billSplitDTO = getBillSplitDTO(bill);
        final var pendingRegisteredBillSplitDTO = billMapper.toPendingRegisteredBillSplitDTO(billSplitDTO);
        pendingRegisteredBillSplitDTO.setPendingAccounts(accounts);
        return pendingRegisteredBillSplitDTO;
    }

    //TODO should move these things into the billMapperObject itself. Mapstruct has a way to add mapping methods.
    private BillCompleteDTO getBillCompleteDTO(Bill bill) {
        final BigDecimal balance = calculateBalance(bill);
        final BillCompleteDTO billCompleteDTO = billMapper.toBillCompleteDTO(bill);

        final List<AccountStatusPair> accountStatusList = new ArrayList<>();
        bill.getAccounts().stream()
                .filter(accBill -> !accBill.getAccount().getEmail().equals(bill.getCreator().getEmail()))
                .forEach(accountBill -> {
                    var pair = new AccountStatusPair(accountMapper.toDTO(accountBill.getAccount()), accountBill.getStatus());
                    accountStatusList.add(pair);
                });

        billCompleteDTO.setBalance(balance);
        billCompleteDTO.setAccountsList(accountStatusList);

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

    private void mapAccountTotalCostIntoBillSplitDTO(Bill bill, BillSplitDTO billSplitDTO) {
        final List<ItemAssociationSplitDTO> itemsPerAccount = new ArrayList<>();
        final HashMap<Account, CostItemsPair> accountPairMap = new HashMap<>();
        bill.getAccounts().stream().map(AccountBill::getAccount).forEach(account -> {
            final var costItemsPair = new CostItemsPair(BigDecimal.ZERO, new ArrayList<>());
            accountPairMap.put(account, costItemsPair);
        });
        mapAllBillAccountItemsIntoHashMap(bill, accountPairMap);
        mapHashMapIntoItemsPerAccount(accountPairMap, itemsPerAccount);
        billSplitDTO.setItemsPerAccount(itemsPerAccount);
    }

    private void mapAllBillAccountItemsIntoHashMap(Bill bill, HashMap<Account, CostItemsPair> accountPairMap) {
        bill.getItems().forEach(item -> {
            final BigDecimal percentageSum = item.getAccounts().stream()
                    .peek(accountItem -> mapAccountItemIntoHashMap(item, accountItem, accountPairMap))
                    .map(AccountItem::getPercentage).reduce(BigDecimal.ZERO, BigDecimal::add);

            verifyItemPercentageSum(item, percentageSum);
        });
    }

    private void verifyItemPercentageSum(final Item item, final BigDecimal percentage) {
        if (percentage.compareTo(BigDecimal.valueOf(100)) != 0) {
            throw new IllegalArgumentException(String.format(ITEM_PERCENTAGES_MUST_ADD_TO_100, item.getName(), percentage));
        }
    }

    private void mapHashMapIntoItemsPerAccount(HashMap<Account, CostItemsPair> accountPairMap, List<ItemAssociationSplitDTO> itemsPerAccount) {
        accountPairMap.forEach((account, costItemsPair) -> {
            final var itemSplitDTO = new ItemAssociationSplitDTO();
            itemSplitDTO.setAccount(accountMapper.toDTO(account));
            itemSplitDTO.setCost(costItemsPair.getCost());
            itemSplitDTO.setItems(costItemsPair.getItemList());
            itemsPerAccount.add(itemSplitDTO);
        });
    }

    @SuppressWarnings("BigDecimalMethodWithoutRoundingCalled")
    private void mapAccountItemIntoHashMap(Item item, AccountItem accountItem, HashMap<Account, CostItemsPair> accountPairMap) {
        final Account thisAccount = accountItem.getAccount();
        final ItemPercentageSplitDTO itemPercentageSplitDTO = itemMapper.toItemPercentageSplitDTO(item);
        final BigDecimal itemPercentage = accountItem.getPercentage();
        itemPercentageSplitDTO.setPercentage(itemPercentage);

        final BigDecimal itemCostForAccount = item.getCost().multiply(itemPercentage.divide(PERCENTAGE_DIVISOR));
        final BigDecimal newAccountTotalCost = accountPairMap.get(thisAccount).getCost().add(itemCostForAccount);
        accountPairMap.get(thisAccount).setCost(newAccountTotalCost);
        accountPairMap.get(thisAccount).getItemList().add(itemPercentageSplitDTO);
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


    private void validateBillDTO(String email, BillDTO billDTO) {
        if ((billDTO.getTipAmount() == null) == (billDTO.getTipPercent() == null)) {
            throw new IllegalArgumentException(ErrorMessageEnum.MULTIPLE_TIP_METHOD.getMessage());
        }
        if (billDTO.getAccountsList().contains(email)) {
            throw new IllegalArgumentException(ErrorMessageEnum.LIST_CANNOT_CONTAIN_BILL_CREATOR.getMessage());
        }
    }

}

