package proj.kedabra.billsnap.business.facade.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.dto.BillDTO;
import proj.kedabra.billsnap.business.entities.Account;
import proj.kedabra.billsnap.business.entities.AccountBill;
import proj.kedabra.billsnap.business.entities.Bill;
import proj.kedabra.billsnap.business.facade.BillFacade;
import proj.kedabra.billsnap.business.mapper.BillMapper;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.business.repository.BillRepository;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;

@Service
public class BillFacadeImpl implements BillFacade {

    private final AccountRepository accountRepository;

    private final BillRepository billRepository;

    private final BillMapper billMapper;

    @Autowired
    public BillFacadeImpl(final AccountRepository accountRepository, final BillRepository billRepository, final BillMapper billMapper) {
        this.accountRepository = accountRepository;
        this.billRepository = billRepository;
        this.billMapper = billMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillDTO addPersonalBill(final String email, final BillDTO billDTO) {

        final Account account = Optional.ofNullable(accountRepository.getAccountByEmail(email))
                .orElseThrow(() -> new ResourceNotFoundException("Account does not exist"));


        final Bill bill = billMapper.toEntity(billDTO);
        bill.setStatus(BillStatusEnum.OPEN);

        AccountBill accountBill = new AccountBill();
        accountBill.setBill(bill);
        accountBill.setAccount(account);
        accountBill.setPercentage(BigDecimal.valueOf(100));
        account.getBills().add(accountBill);

        bill.setAccounts(List.of(accountBill));

        return billMapper.toDTO(billRepository.save(bill));

    }
}
