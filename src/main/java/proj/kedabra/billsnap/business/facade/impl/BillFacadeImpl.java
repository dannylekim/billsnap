package proj.kedabra.billsnap.business.facade.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.dto.AssociateBillDTO;
import proj.kedabra.billsnap.business.dto.BillCompleteDTO;
import proj.kedabra.billsnap.business.dto.BillDTO;
import proj.kedabra.billsnap.business.dto.BillSplitDTO;
import proj.kedabra.billsnap.business.dto.CostItemsPair;
import proj.kedabra.billsnap.business.dto.EditBillDTO;
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
import proj.kedabra.billsnap.business.service.CalculatePaymentService;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.InvitationStatusEnum;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;
import proj.kedabra.billsnap.utils.tuples.AccountStatusPair;

@Service
public class BillFacadeImpl implements BillFacade {

    private static final BigDecimal PERCENTAGE_DIVISOR = BigDecimal.valueOf(100);

    private static final String ITEM_PERCENTAGES_MUST_ADD_TO_100 = "The percentage split for this item must add up to 100: {%s, Percentage: %s}";

    private final BillService billService;

    private final AccountService accountService;

    private final BillMapper billMapper;

    private final AccountMapper accountMapper;

    private final ItemMapper itemMapper;

    private final CalculatePaymentService calculatePaymentService;


    @Autowired
    public BillFacadeImpl(final BillService billService, final AccountService accountService, final BillMapper billMapper, final AccountMapper accountMapper, final ItemMapper itemMapper, final CalculatePaymentService calculatePaymentService) {
        this.billService = billService;
        this.accountService = accountService;
        this.billMapper = billMapper;
        this.accountMapper = accountMapper;
        this.itemMapper = itemMapper;
        this.calculatePaymentService = calculatePaymentService;
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
        final var bill = billService.getBill(associateBillDTO.getId());
        billService.verifyBillStatus(bill, BillStatusEnum.OPEN);
        final Bill associatedBill = billService.associateItemsToAccountBill(associateBillDTO);

        return getBillSplitDTO(associatedBill);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PendingRegisteredBillSplitDTO inviteRegisteredToBill(final Long billId, final String userEmail, final List<String> accounts) {
        final var bill = billService.getBill(billId);
        billService.verifyBillStatus(bill, BillStatusEnum.OPEN);
        billService.verifyUserIsBillResponsible(bill, userEmail);

        final List<Account> accountsList = accountService.getAccounts(accounts);
        final List<String> emailsList = accountsList.stream().map(Account::getEmail).collect(Collectors.toList());
        final List<String> commonEmailsList = bill.getAccounts().stream()
                .map(AccountBill::getAccount).map(Account::getEmail)
                .filter(emailsList::contains).collect(Collectors.toList());
        if (!commonEmailsList.isEmpty()) {
            throw new IllegalArgumentException(ErrorMessageEnum.LIST_ACCOUNT_ALREADY_IN_BILL.getMessage(commonEmailsList.toString()));
        }

        billService.inviteRegisteredToBill(bill, accountsList);

        final var pendingAccounts = bill.getAccounts().stream()
                .filter(a -> a.getStatus().equals(InvitationStatusEnum.PENDING))
                .map(AccountBill::getAccount).map(Account::getEmail).collect(Collectors.toList());

        final var billSplitDTO = getBillSplitDTO(bill);
        final var pendingRegisteredBillSplitDTO = billMapper.toPendingRegisteredBillSplitDTO(billSplitDTO);
        pendingRegisteredBillSplitDTO.setPendingAccounts(pendingAccounts);
        return pendingRegisteredBillSplitDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillSplitDTO getDetailedBill(final Long billId, final String userEmail) {
        final var bill = billService.getBill(billId);
        final boolean isNotCreator = !bill.getCreator().getEmail().equals(userEmail);
        final boolean isNotInBillsAccount = bill.getAccounts().stream()
                .map(AccountBill::getAccount)
                .map(Account::getEmail)
                .noneMatch(email -> email.equals(userEmail));

        if (isNotCreator && isNotInBillsAccount) {
            throw new AccessForbiddenException(ErrorMessageEnum.ACCOUNT_IS_NOT_ASSOCIATED_TO_BILL.getMessage());
        }
        return getBillSplitDTO(bill);
    }
    @Override
    public BillSplitDTO startBill(Long billId, String userEmail) {
        final Bill bill = billService.startBill(billId, userEmail);
        return getBillSplitDTO(bill);
    }

    @Override
    public BillSplitDTO editBill(final Long billId, final String email, final EditBillDTO editBill) {
        final var account = accountService.getAccount(email);
        final Bill bill = billService.editBill(billId, account, editBill);
        return getBillSplitDTO(bill);
    }

    private BillCompleteDTO getBillCompleteDTO(Bill bill) {
        final BillCompleteDTO billCompleteDTO = billMapper.toBillCompleteDTO(bill);

        final List<AccountStatusPair> accountStatusList = new ArrayList<>();
        bill.getAccounts().forEach(accountBill -> {
            var pair = new AccountStatusPair(accountMapper.toDTO(accountBill.getAccount()), accountBill.getStatus());
            accountStatusList.add(pair);
        });

        final BigDecimal balance = calculatePaymentService.calculateBalance(bill);
        billCompleteDTO.setBalance(balance);
        billCompleteDTO.setAccountsList(accountStatusList);

        return billCompleteDTO;
    }

    @Override
    public BillSplitDTO getBillSplitDTO(Bill bill) {
        final BillSplitDTO billSplitDTO = billMapper.toBillSplitDTO(bill);
        final var billSubTotal = calculatePaymentService.calculateSubTotal(bill);
        final BigDecimal totalTip = calculatePaymentService.calculateTip(bill.getTipAmount(), bill.getTipPercent(), billSubTotal);
        final BigDecimal balance = calculatePaymentService.calculateBalance(bill);
        billSplitDTO.setTotalTip(totalTip);
        billSplitDTO.setBalance(balance);

        mapAccountSubTotalCostIntoBillSplitDTO(bill, billSplitDTO);

        billSplitDTO.getInformationPerAccount()
                .stream()
                .filter(information -> BigDecimal.ZERO.compareTo(information.getSubTotal()) < 0)
                .forEach(item -> {
                    item.setTaxes(calculatePaymentService.calculateTaxes(item.getSubTotal(), bill.getTaxes()));
                    final var accountTip = item.getSubTotal().divide(billSubTotal, RoundingMode.HALF_UP).multiply(totalTip).setScale(CalculatePaymentService.DOLLAR_SCALE, RoundingMode.HALF_UP);
                    item.setTip(accountTip);
                });

        return billSplitDTO;
    }

    private void mapAccountSubTotalCostIntoBillSplitDTO(Bill bill, BillSplitDTO billSplitDTO) {
        final List<ItemAssociationSplitDTO> itemsPerAccount = new ArrayList<>();
        final HashMap<Account, CostItemsPair> accountPairMap = new HashMap<>();
        bill.getAccounts().stream().map(AccountBill::getAccount).forEach(account -> {
            final var costItemsPair = new CostItemsPair(BigDecimal.ZERO, new ArrayList<>());
            accountPairMap.put(account, costItemsPair);
        });
        mapAllBillAccountItemsIntoHashMap(bill, accountPairMap);
        mapHashMapIntoItemsPerAccount(accountPairMap, itemsPerAccount);
        billSplitDTO.setInformationPerAccount(itemsPerAccount);
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
            itemSplitDTO.setSubTotal(costItemsPair.getCost().setScale(CalculatePaymentService.DOLLAR_SCALE, RoundingMode.HALF_UP));
            itemSplitDTO.setItems(costItemsPair.getItemList());
            itemsPerAccount.add(itemSplitDTO);
        });
    }

    private void mapAccountItemIntoHashMap(Item item, AccountItem accountItem, HashMap<Account, CostItemsPair> accountPairMap) {
        final Account thisAccount = accountItem.getAccount();
        final ItemPercentageSplitDTO itemPercentageSplitDTO = itemMapper.toItemPercentageSplitDTO(item);
        final BigDecimal itemPercentage = accountItem.getPercentage().setScale(CalculatePaymentService.PERCENT_SCALE, RoundingMode.HALF_UP);
        itemPercentageSplitDTO.setPercentage(itemPercentage);

        final BigDecimal itemCostForAccount = item.getCost().multiply(itemPercentage.divide(PERCENTAGE_DIVISOR, RoundingMode.HALF_UP));
        final BigDecimal newAccountTotalCost = accountPairMap.get(thisAccount).getCost().add(itemCostForAccount);
        accountPairMap.get(thisAccount).setCost(newAccountTotalCost);
        accountPairMap.get(thisAccount).getItemList().add(itemPercentageSplitDTO);
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

