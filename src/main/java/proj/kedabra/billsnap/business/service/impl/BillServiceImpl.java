package proj.kedabra.billsnap.business.service.impl;

import java.math.BigDecimal;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.dto.BillDTO;
import proj.kedabra.billsnap.business.entities.Account;
import proj.kedabra.billsnap.business.entities.AccountBill;
import proj.kedabra.billsnap.business.entities.AccountItem;
import proj.kedabra.billsnap.business.entities.Bill;
import proj.kedabra.billsnap.business.entities.Item;
import proj.kedabra.billsnap.business.mapper.BillMapper;
import proj.kedabra.billsnap.business.repository.AccountBillRepository;
import proj.kedabra.billsnap.business.repository.BillRepository;
import proj.kedabra.billsnap.business.service.BillService;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.SplitByEnum;

@Service
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;

    private final AccountBillRepository accountBillRepository;

    private final BillMapper billMapper;

    public BillServiceImpl(final BillRepository billRepository, final BillMapper billMapper, final AccountBillRepository accountBillRepository) {
        this.billRepository = billRepository;
        this.billMapper = billMapper;
        this.accountBillRepository = accountBillRepository;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Bill createBillToAccount(BillDTO billDTO, Account account) {
        final Bill bill = billMapper.toEntity(billDTO);
        bill.setStatus(BillStatusEnum.OPEN);
        bill.setResponsible(account);
        bill.setCreator(account);
        bill.setActive(true);
        bill.setSplitBy(SplitByEnum.ITEM);

        bill.getItems().forEach(i -> mapItems(i, bill, account));


        AccountBill accountBill = new AccountBill();
        accountBill.setBill(bill);
        accountBill.setAccount(account);
        accountBill.setPercentage(new BigDecimal(100));
        bill.getAccounts().add(accountBill);

        return billRepository.save(bill);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Stream<Bill> getAllBillsByAccount(Account account) {
        return accountBillRepository.getAllByAccount(account).map(AccountBill::getBill);
    }

    private void mapItems(final Item item, final Bill bill, final Account account) {
        item.setBill(bill);
        var accountItem = new AccountItem();
        accountItem.setAccount(account);
        accountItem.setPercentage(new BigDecimal(100));
        accountItem.setItem(item);
        item.getAccounts().add(accountItem);
    }

}
