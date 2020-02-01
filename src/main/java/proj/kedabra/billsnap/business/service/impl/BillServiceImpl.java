package proj.kedabra.billsnap.business.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.dto.BillDTO;
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
import proj.kedabra.billsnap.utils.ErrorMessageEnum;

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
        bill.getItems().forEach(i -> mapItems(i, bill, account));
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
    @Override
    public Bill getBill(Long id) {
        return billRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(ErrorMessageEnum.BILL_DOES_NOT_EXIST.getMessage()));
    }

    @Transactional(rollbackFor = Exception.class)
    public List<PaymentOwedDTO> calculateAmountOwed(Account account) {
        return getAllAmountOwedByStatusAndAccount(BillStatusEnum.OPEN, account).map(paymentMapper::toDTO).collect(Collectors.toList());
    }

    private void mapItems(final Item item, final Bill bill, final Account account) {
        item.setBill(bill);
        var accountItem = new AccountItem();
        accountItem.setAccount(account);
        accountItem.setPercentage(new BigDecimal(100));
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
