package proj.kedabra.billsnap.business.service.impl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.dto.AssociateBillDTO;
import proj.kedabra.billsnap.business.dto.BillDTO;
import proj.kedabra.billsnap.business.dto.ItemAssociationDTO;
import proj.kedabra.billsnap.business.dto.ItemPercentageDTO;
import proj.kedabra.billsnap.business.dto.PaymentOwedDTO;
import proj.kedabra.billsnap.business.mapper.BillMapper;
import proj.kedabra.billsnap.business.mapper.PaymentMapper;
import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.model.entities.AccountBill;
import proj.kedabra.billsnap.business.model.entities.AccountItem;
import proj.kedabra.billsnap.business.model.entities.Bill;
import proj.kedabra.billsnap.business.model.entities.Item;
import proj.kedabra.billsnap.business.model.projections.PaymentOwed;
import proj.kedabra.billsnap.business.repository.AccountBillRepository;
import proj.kedabra.billsnap.business.repository.BillRepository;
import proj.kedabra.billsnap.business.repository.PaymentRepository;
import proj.kedabra.billsnap.business.service.BillService;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.InvitationStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.SplitByEnum;

@Service
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;

    private final AccountBillRepository accountBillRepository;

    private final PaymentRepository paymentRepository;

    private final BillMapper billMapper;

    private final PaymentMapper paymentMapper;

    public BillServiceImpl(
            final BillRepository        billRepository,
            final BillMapper            billMapper,
            final AccountBillRepository accountBillRepository,
            final PaymentMapper         paymentMapper,
            final PaymentRepository     paymentRepository ) {
        this.billRepository = billRepository;
        this.billMapper = billMapper;
        this.accountBillRepository = accountBillRepository;
        this.paymentMapper = paymentMapper;
        this.paymentRepository = paymentRepository;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Bill createBillToAccount(BillDTO billDTO, Account account, List<Account> accountList) {
        final Bill bill = billMapper.toEntity(billDTO);
        bill.setStatus(BillStatusEnum.OPEN);
        bill.setResponsible(account);
        bill.setCreator(account);
        bill.setActive(true);
        bill.setSplitBy(SplitByEnum.ITEM);
        bill.getItems().forEach(i -> mapItems(i, bill, account, 100));
        accountList.forEach(acc -> mapAccount(bill, acc, null));
        mapAccount(bill, account, null);

        return billRepository.save(bill);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Stream<Bill> getAllBillsByAccount(Account account) {
        return accountBillRepository.getAllByAccount(account).map(AccountBill::getBill);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Stream<PaymentOwed> getAllAmountOwedByStatusAndAccount(BillStatusEnum status, Account account) {
        return paymentRepository.getAllAmountOwedByStatusAndAccount(status, account);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<PaymentOwedDTO> calculateAmountOwed(Account account) {
        return getAllAmountOwedByStatusAndAccount(BillStatusEnum.OPEN, account).map(paymentMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Bill associateItemsToAccountBill(AssociateBillDTO associateBillDTO) {
        final var bill = billRepository.findById(associateBillDTO.getId()).orElseThrow(() -> new IllegalArgumentException("No bill with that id exists"));
        final List<ItemAssociationDTO> items = associateBillDTO.getItems();
        verifyExistenceOfAccountsInBill(bill, items);
        verifyExistenceofItemsInBill(bill, items);
        removeReferencedAccountItems(bill, items);
        addNewAssociations(bill, items);
        return bill;
    }

    private void addNewAssociations(Bill bill, List<ItemAssociationDTO> items) {
        final var itemMap = bill.getItems().stream().collect(Collectors.toMap(Item::getId, Function.identity()));
        final var accountMap = bill.getAccounts().stream().map(AccountBill::getAccount).collect(Collectors.toMap(Account::getEmail, Function.identity()));


        items.forEach(item -> {
            final var account = accountMap.get(item.getEmail());
            item.getItems().forEach(itemPercentageDTO -> {
                final var itemEntity = itemMap.get(itemPercentageDTO.getItemId());
                this.mapItems(itemEntity, bill, account, itemPercentageDTO.getPercentage().intValue());
            });
        });
    }


    private void removeReferencedAccountItems(Bill bill, List<ItemAssociationDTO> items) {
        final var list = items.stream()
                .map(ItemAssociationDTO::getItems)
                .flatMap(List::stream)
                .map(ItemPercentageDTO::getItemId)
                .collect(Collectors.toList());

        bill.getItems().stream().filter(i -> list.contains(i.getId())).map(Item::getAccounts).forEach(Set::clear);
    }

    private void verifyExistenceofItemsInBill(Bill bill, List<ItemAssociationDTO> items) {

        final Set<Item> billItems = bill.getItems();
        final Set<Long> billItemsId = billItems.stream().map(Item::getId).collect(HashSet::new, HashSet::add, HashSet::addAll);

        final Long[] nonExistentListIds = items.stream()
                .map(ItemAssociationDTO::getItems)
                .flatMap(List::stream)
                .map(ItemPercentageDTO::getItemId)
                .filter(Predicate.not(billItemsId::contains))
                .toArray(Long[]::new);

        if (nonExistentListIds.length > 0) {
            throw new IllegalArgumentException(String.format("Not all items exists: %s", Arrays.toString(nonExistentListIds)));
        }

    }
    private void verifyExistenceOfAccountsInBill(Bill bill, List<ItemAssociationDTO> items) {
        final List<String> existingEmailsInList = bill.getAccounts().stream().map(AccountBill::getAccount).map(Account::getEmail).collect(Collectors.toList());
        final String[] nonExistentAccounts = items.stream()
                .map(ItemAssociationDTO::getEmail)
                .filter(Predicate.not(existingEmailsInList::contains))
                .toArray(String[]::new);

        if (nonExistentAccounts.length > 0) {
            throw new IllegalArgumentException(String.format("Not all accounts are in the bill: %s", Arrays.toString(nonExistentAccounts)));
        }
    }

    private void mapItems(final Item item, final Bill bill, final Account account, int percentage) {
        item.setBill(bill);
        var accountItem = new AccountItem();
        accountItem.setAccount(account);
        accountItem.setPercentage(new BigDecimal(percentage));
        accountItem.setItem(item);
        item.getAccounts().add(accountItem);
    }

    private void mapAccount(final Bill bill, final Account account, BigDecimal percentage) {
        var accountBill = new AccountBill();
        accountBill.setAccount(account);
        accountBill.setBill(bill);
        accountBill.setPercentage(percentage);
        accountBill.setStatus(InvitationStatusEnum.ACCEPTED);
        bill.getAccounts().add(accountBill);
    }

}
