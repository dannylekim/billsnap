package proj.kedabra.billsnap.business.service.impl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.dto.AssociateBillDTO;
import proj.kedabra.billsnap.business.dto.BillDTO;
import proj.kedabra.billsnap.business.dto.EditBillDTO;
import proj.kedabra.billsnap.business.dto.GetBillPaginationDTO;
import proj.kedabra.billsnap.business.dto.ItemAssociationDTO;
import proj.kedabra.billsnap.business.dto.ItemPercentageDTO;
import proj.kedabra.billsnap.business.dto.PaymentOwedDTO;
import proj.kedabra.billsnap.business.dto.TaxDTO;
import proj.kedabra.billsnap.business.exception.AccessForbiddenException;
import proj.kedabra.billsnap.business.exception.FunctionalWorkflowException;
import proj.kedabra.billsnap.business.exception.ResourceNotFoundException;
import proj.kedabra.billsnap.business.mapper.BillMapper;
import proj.kedabra.billsnap.business.mapper.PaymentMapper;
import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.model.entities.AccountBill;
import proj.kedabra.billsnap.business.model.entities.AccountItem;
import proj.kedabra.billsnap.business.model.entities.Bill;
import proj.kedabra.billsnap.business.model.entities.Item;
import proj.kedabra.billsnap.business.model.entities.Tax;
import proj.kedabra.billsnap.business.model.projections.PaymentOwed;
import proj.kedabra.billsnap.business.repository.BillRepository;
import proj.kedabra.billsnap.business.repository.PaymentRepository;
import proj.kedabra.billsnap.business.service.BillService;
import proj.kedabra.billsnap.business.service.ItemService;
import proj.kedabra.billsnap.business.service.NotificationService;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.InvitationStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.PaymentStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.SplitByEnum;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;


@Service
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;

    private final PaymentRepository paymentRepository;

    private final BillMapper billMapper;

    private final PaymentMapper paymentMapper;

    private final NotificationService notificationService;

    private final EntityManager entityManager;

    private final ItemService itemService;

    @Autowired
    public BillServiceImpl(
            final BillRepository billRepository,
            final BillMapper billMapper,
            final PaymentMapper paymentMapper,
            final PaymentRepository paymentRepository,
            final NotificationService notificationService,
            final ItemService itemService,
            final EntityManager entityManager) {
        this.billRepository = billRepository;
        this.billMapper = billMapper;
        this.paymentMapper = paymentMapper;
        this.paymentRepository = paymentRepository;
        this.notificationService = notificationService;
        this.entityManager = entityManager;
        this.itemService = itemService;
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
        bill.getTaxes().forEach(t -> t.setBill(bill));
        mapAccountBill(bill, account, null, InvitationStatusEnum.ACCEPTED);

        return billRepository.save(bill);
    }

    @Override
    @Transactional(readOnly = true)
    public Stream<Bill> getAllBillsByAccountPageable(final GetBillPaginationDTO billPaginationDTO) {
        return billRepository.findBillsPageable(
                billPaginationDTO.getStartDate(),
                billPaginationDTO.getEndDate(),
                billPaginationDTO.getCategory(),
                billPaginationDTO.getStatuses(),
                billPaginationDTO.getInvitationStatus(),
                billPaginationDTO.getEmail(),
                billPaginationDTO.getPageable()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Stream<PaymentOwed> getAllAmountOwedByStatusAndAccount(BillStatusEnum status, Account account) {
        return paymentRepository.getAllAmountOwedByStatusAndAccount(status, account);
    }

    @Override
    @Transactional(readOnly = true)
    public Bill getBill(Long id) {
        return billRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(ErrorMessageEnum.BILL_ID_DOES_NOT_EXIST.getMessage(id.toString())));
    }

    @Override
    public void verifyUserIsBillResponsible(Bill bill, String userEmail) {
        if (!bill.getResponsible().getEmail().equals(userEmail)) {
            throw new AccessForbiddenException(ErrorMessageEnum.USER_IS_NOT_BILL_RESPONSIBLE.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Bill startBill(Long id) {
        final Bill bill = getBill(id);
        verifyBillStatus(bill, BillStatusEnum.OPEN);
        bill.setStatus(BillStatusEnum.IN_PROGRESS);
        bill.getAccounts().stream().filter(ab -> ab.getStatus() == InvitationStatusEnum.ACCEPTED).forEach(ab -> ab.setPaymentStatus(PaymentStatusEnum.IN_PROGRESS));
        return bill;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Bill editBill(Long id, Account account, EditBillDTO editBill) {
        final Bill bill = getBill(id);
        verifyBillStatus(bill, BillStatusEnum.OPEN);
        verifyExistenceofTaxesInBill(editBill, bill);

        billMapper.updatebill(bill, editBill);
        bill.getTaxes().forEach(t -> t.setBill(bill));
        final var newResponsible = bill.getAccounts().stream().map(AccountBill::getAccount).filter(acc -> editBill.getResponsible().equals(acc.getEmail())).findFirst().orElseThrow();
        bill.setResponsible(newResponsible);
        setBillTip(bill, editBill);
        itemService.editNewItems(bill, account, editBill);

        return billRepository.save(bill);
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
    public void verifyBillStatus(final Bill bill, final BillStatusEnum status) {
        if (bill.getStatus() != status) {
            throw new FunctionalWorkflowException(ErrorMessageEnum.WRONG_BILL_STATUS.getMessage(status.toString()));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public List<PaymentOwedDTO> calculateAmountOwed(Account account) {
        return getAllAmountOwedByStatusAndAccount(BillStatusEnum.OPEN, account).map(paymentMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Bill associateItemsToAccountBill(AssociateBillDTO associateBillDTO) {
        verifyPercentagesAreIntegerValued(associateBillDTO);
        verifyNoDuplicateEmails(associateBillDTO);
        final var bill = getBill(associateBillDTO.getId());
        final List<ItemAssociationDTO> items = associateBillDTO.getItems();
        verifyExistenceOfAssociateItemInBill(bill, items);
        verifyExistenceOfItemsInBill(bill, items);
        verifyInvitationStatus(bill, items);
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
        entityManager.flush();
    }

    private void verifyInvitationStatus(final Bill bill, final List<ItemAssociationDTO> items) {
        final var declinedEmails = bill.getAccounts().stream().filter(accountBill -> InvitationStatusEnum.DECLINED.equals(accountBill.getStatus())).map(AccountBill::getAccount).map(Account::getEmail).collect(Collectors.toList());
        final var associatedDeclinedEmails = items.stream().map(ItemAssociationDTO::getEmail).filter(declinedEmails::contains).collect(Collectors.toList());

        if (!associatedDeclinedEmails.isEmpty()) {
            throw new IllegalArgumentException(ErrorMessageEnum.LIST_ACCOUNT_DECLINED.getMessage(associatedDeclinedEmails.toString()));
        }
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

    private void verifyExistenceOfAssociateItemInBill(final Bill bill, final List<ItemAssociationDTO> items) {
        final List<String> existingEmailsInList = bill.getAccounts().stream().map(AccountBill::getAccount).map(Account::getEmail).collect(Collectors.toList());
        final String[] nonExistentAccounts = items.stream()
                .map(ItemAssociationDTO::getEmail)
                .filter(Predicate.not(existingEmailsInList::contains))
                .toArray(String[]::new);

        if (nonExistentAccounts.length > 0) {
            throw new IllegalArgumentException(ErrorMessageEnum.SOME_ACCOUNTS_NONEXISTENT_IN_BILL.getMessage(Arrays.toString(nonExistentAccounts)));
        }
    }

    private void verifyExistenceofTaxesInBill(EditBillDTO editBill, Bill bill) {
        final var existentTaxIds = bill.getTaxes().stream().map(Tax::getId).collect(Collectors.toList());
        final var nonExistentTaxIds = editBill.getTaxes().stream().map(TaxDTO::getId).filter(Objects::nonNull).filter(Predicate.not(existentTaxIds::contains)).collect(Collectors.toList());

        if (!nonExistentTaxIds.isEmpty()) {
            throw new ResourceNotFoundException(ErrorMessageEnum.TAX_ID_DOES_NOT_EXIST.getMessage(nonExistentTaxIds.toString()));
        }
    }

    private void verifyPercentagesAreIntegerValued(final AssociateBillDTO associateBillDTO) {
        final var nonIntegerList = associateBillDTO.getItems()
                .stream()
                .map(ItemAssociationDTO::getItems)
                .flatMap(List::stream)
                .map(ItemPercentageDTO::getPercentage)
                .filter(bd -> bd.stripTrailingZeros().scale() > 0)
                .collect(Collectors.toList());

        if (!nonIntegerList.isEmpty()) {
            throw new IllegalArgumentException(ErrorMessageEnum.GIVEN_VALUES_NOT_INTEGER_VALUED.getMessage(nonIntegerList.toString()));
        }
    }

    private void verifyNoDuplicateEmails(final AssociateBillDTO associateBillDTO) {
        final HashSet<String> allEmails = new HashSet<>();
        final Set<String> duplicateSet = associateBillDTO.getItems().stream()
                .map(ItemAssociationDTO::getEmail)
                .filter(email -> !allEmails.add(email)) //Set.add() returns false if the item was already in the set
                .collect(Collectors.toSet());

        if (!duplicateSet.isEmpty()) {
            throw new IllegalArgumentException(ErrorMessageEnum.DUPLICATE_EMAILS_IN_ASSOCIATE_USERS.getMessage(duplicateSet.toString()));
        }
    }

    private void setBillTip(final Bill bill, final EditBillDTO editBill) {
        if ((editBill.getTipAmount() == null) == (editBill.getTipPercent() == null)) {
            throw new IllegalArgumentException(ErrorMessageEnum.MULTIPLE_TIP_METHOD.getMessage());
        }

        bill.setTipPercent(editBill.getTipPercent());
        bill.setTipAmount(editBill.getTipAmount());
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
