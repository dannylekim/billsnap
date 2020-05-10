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

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.dto.AssociateBillDTO;
import proj.kedabra.billsnap.business.dto.BillDTO;
import proj.kedabra.billsnap.business.dto.ItemAssociationDTO;
import proj.kedabra.billsnap.business.dto.ItemPercentageDTO;
import proj.kedabra.billsnap.business.dto.PaymentOwedDTO;
import proj.kedabra.billsnap.business.exception.AccessForbiddenException;
import proj.kedabra.billsnap.business.exception.FunctionalWorkflowException;
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
import proj.kedabra.billsnap.business.service.NotificationService;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.InvitationStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.SplitByEnum;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;


@Service
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;

    private final AccountBillRepository accountBillRepository;

    private final PaymentRepository paymentRepository;

    private final BillMapper billMapper;

    private final PaymentMapper paymentMapper;

    private final NotificationService notificationService;

    public BillServiceImpl(
            final BillRepository billRepository,
            final BillMapper billMapper,
            final AccountBillRepository accountBillRepository,
            final PaymentMapper paymentMapper,
            final PaymentRepository paymentRepository,
            final NotificationService notificationService) {
        this.billRepository = billRepository;
        this.billMapper = billMapper;
        this.accountBillRepository = accountBillRepository;
        this.paymentMapper = paymentMapper;
        this.paymentRepository = paymentRepository;
        this.notificationService = notificationService;
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
        inviteRegisteredToBill(bill, accountList);
        mapAccountBill(bill, account, null, InvitationStatusEnum.ACCEPTED);

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
    @Override
    public Bill getBill(Long id) {
        return billRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(ErrorMessageEnum.BILL_DOES_NOT_EXIST.getMessage()));
    }

    @Override
    public void verifyUserIsBillResponsible(Bill bill, String userEmail) {
        if (!bill.getResponsible().getEmail().equals(userEmail)) {
            throw new AccessForbiddenException(ErrorMessageEnum.USER_IS_NOT_BILL_RESPONSIBLE.getMessage());
        }
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Bill startBill(Long id, String userEmail) {
        final Bill bill = getBill(id);
        verifyUserIsBillResponsible(bill, userEmail);
        verifyBillIsOpen(bill);
        bill.setStatus(BillStatusEnum.IN_PROGRESS);
        return bill;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Bill inviteRegisteredToBill(final Bill bill, final List<Account> accounts) {
        accounts.forEach(acc -> {
            notificationService.createNotification(bill, acc);
            mapAccountBill(bill, acc, null, InvitationStatusEnum.PENDING);
        });

        return bill;
    }
    @Override
    public void verifyBillIsOpen(Bill bill) {
        if (bill.getStatus() != BillStatusEnum.OPEN) {
            throw new FunctionalWorkflowException(ErrorMessageEnum.BILL_IS_NOT_OPEN.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public List<PaymentOwedDTO> calculateAmountOwed(Account account) {
        return getAllAmountOwedByStatusAndAccount(BillStatusEnum.OPEN, account).map(paymentMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Bill associateItemsToAccountBill(AssociateBillDTO associateBillDTO) {
        final var bill = billRepository.findById(associateBillDTO.getId()).orElseThrow(() -> new IllegalArgumentException(ErrorMessageEnum.BILL_ID_DOES_NOT_EXIST.getMessage()));
        final List<ItemAssociationDTO> items = associateBillDTO.getItems();
        verifyExistenceOfAccountsInBill(bill, items);
        verifyExistenceOfItemsInBill(bill, items);
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

    private void verifyExistenceOfItemsInBill(Bill bill, List<ItemAssociationDTO> items) {

        final Set<Item> billItems = bill.getItems();
        final Set<Long> billItemsId = billItems.stream().map(Item::getId).collect(HashSet::new, HashSet::add, HashSet::addAll);

        final Long[] nonExistentListIds = items.stream()
                .map(ItemAssociationDTO::getItems)
                .flatMap(List::stream)
                .map(ItemPercentageDTO::getItemId)
                .filter(Predicate.not(billItemsId::contains))
                .toArray(Long[]::new);

        if (nonExistentListIds.length > 0) {
            throw new IllegalArgumentException(ErrorMessageEnum.SOME_ITEMS_NONEXISTENT_IN_BILL.getMessage(Arrays.toString(nonExistentListIds)));
        }

    }
    private void verifyExistenceOfAccountsInBill(Bill bill, List<ItemAssociationDTO> items) {
        final List<String> existingEmailsInList = bill.getAccounts().stream().map(AccountBill::getAccount).map(Account::getEmail).collect(Collectors.toList());
        final String[] nonExistentAccounts = items.stream()
                .map(ItemAssociationDTO::getEmail)
                .filter(Predicate.not(existingEmailsInList::contains))
                .toArray(String[]::new);

        if (nonExistentAccounts.length > 0) {
            throw new IllegalArgumentException(ErrorMessageEnum.SOME_ACCOUNTS_NONEXISTENT_IN_BILL.getMessage(Arrays.toString(nonExistentAccounts)));
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

    private void mapAccountBill(final Bill bill, final Account account, BigDecimal percentage, InvitationStatusEnum status) {
        var accountBill = new AccountBill();
        accountBill.setAccount(account);
        accountBill.setBill(bill);
        accountBill.setPercentage(percentage);
        accountBill.setStatus(status);
        bill.getAccounts().add(accountBill);
    }

}
