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
import proj.kedabra.billsnap.business.dto.AssociateBillDTO;
import proj.kedabra.billsnap.business.dto.BillCompleteDTO;
import proj.kedabra.billsnap.business.dto.BillDTO;
import proj.kedabra.billsnap.business.dto.ItemAssociationDTO;
import proj.kedabra.billsnap.business.dto.ItemPercentageDTO;
import proj.kedabra.billsnap.business.entities.Account;
import proj.kedabra.billsnap.business.entities.AccountBill;
import proj.kedabra.billsnap.business.entities.Bill;
import proj.kedabra.billsnap.business.entities.Item;
import proj.kedabra.billsnap.business.facade.BillFacade;
import proj.kedabra.billsnap.business.mapper.AccountMapper;
import proj.kedabra.billsnap.business.mapper.BillMapper;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.business.repository.BillRepository;
import proj.kedabra.billsnap.business.repository.ItemRepository;
import proj.kedabra.billsnap.business.service.BillService;

@Service
public class BillFacadeImpl implements BillFacade {

    private final AccountRepository accountRepository;

    private final ItemRepository itemRepository;

    private final BillRepository billRepository;

    private final BillService billService;

    private final BillMapper billMapper;

    private final AccountMapper accountMapper;

    private static final BigDecimal PERCENTAGE_DIVISOR = BigDecimal.valueOf(100);

    private static final String BILL_CANNOT_BE_MODIFIED = "Bill cannot be modified";

    private static final String ACCOUNT_DOES_NOT_EXIST = "Account does not exist";

    private static final String BILL_DOES_NOT_EXIST = "Bill does not exist";

    private static final String LIST_ACCOUNT_DOES_NOT_EXIST = "One or more accounts in the list of accounts does not exist";

    private static final String LIST_ITEMS_ID_DOES_NOT_EXIST = "One or more item id's in the list of id's does not exist";

    private static final String LIST_CANNOT_CONTAIN_BILL_CREATOR = "List of emails cannot contain bill creator email";

    @Autowired
    public BillFacadeImpl(final AccountRepository accountRepository, final ItemRepository itemRepository, final BillRepository billRepository,
                          final BillService billService, final BillMapper billMapper, final AccountMapper accountMapper) {
        this.itemRepository = itemRepository;
        this.accountRepository = accountRepository;
        this.billRepository = billRepository;
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

    private List<Account> getRepositoryAccountsList(List<String> accountsList) {
        final List<Account> repositoryAccountsList = accountRepository.getAccountsByEmailIn(accountsList).collect(Collectors.toList());

        if (accountsList.size() > repositoryAccountsList.size()) {
            final List<String> accountsStringList = repositoryAccountsList.stream().map(Account::getEmail).collect(Collectors.toList());
            final List<String> nonExistentEmails = new ArrayList<>(accountsList);
            nonExistentEmails.removeAll(accountsStringList);
            throw new ResourceNotFoundException(LIST_ACCOUNT_DOES_NOT_EXIST + ": " + nonExistentEmails.toString());
        }

        return repositoryAccountsList;
    }

    private List<Item> getRepositoryItemsList(List<Long> itemIdList) {
        final List<Item> allItemsList = itemRepository.getItemsByIdIn(itemIdList).collect(Collectors.toList());

        if (itemIdList.size() > allItemsList.size()) {
            final List<Long> itemsLongList = allItemsList.stream().map(Item::getId).collect(Collectors.toList());
            final List<Long> nonExistentItemIds = new ArrayList<>(itemIdList);
            nonExistentItemIds.removeAll(itemsLongList);
            throw new ResourceNotFoundException(LIST_ITEMS_ID_DOES_NOT_EXIST + ": " + nonExistentItemIds.toString());
        }

        return allItemsList;
    }

    private Bill getModifiableBill(Long billId) {
        Bill bill = Optional.ofNullable(billRepository.getBillById(billId)).orElseThrow(() -> new ResourceNotFoundException(BILL_DOES_NOT_EXIST));
        if (!bill.getActive() || bill.getStatus().is("RESOLVED")) {
            throw new IllegalArgumentException(BILL_CANNOT_BE_MODIFIED);
        }
        return bill;
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

    //TODO: discuss implementation
    private void validateAssociateBillDTO(AssociateBillDTO associateBillDTO) {
        final List<ItemAssociationDTO> dtoItems = associateBillDTO.getItems();

        final List<String> accountsList = dtoItems.stream().map(ItemAssociationDTO::getAccountEmail).collect(Collectors.toList());
        final List<Account> repositoryAccountsList = getRepositoryAccountsList(accountsList);

        final List<Long> allItemsIdList = dtoItems.stream().map(ItemAssociationDTO::getItems).flatMap(List::stream)
                .map(ItemPercentageDTO::getItemId).collect(Collectors.toList());
        final List<Item> allItemsList = getRepositoryItemsList(allItemsIdList);

        final Bill bill = getModifiableBill(associateBillDTO.getId());
    }
}
