package proj.kedabra.billsnap.business.facade.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.dto.BillDTO;
import proj.kedabra.billsnap.business.facade.BillFacade;
import proj.kedabra.billsnap.business.mapper.BillMapper;
import proj.kedabra.billsnap.business.repository.AccountRepository;

@Service
public class BillFacadeImpl implements BillFacade {

    private final AccountRepository accountRepository;

    private final BillMapper billMapper;

    public BillFacadeImpl(final AccountRepository accountRepository, final BillMapper billMapper) {
        this.accountRepository = accountRepository;
        this.billMapper = billMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillDTO addPersonalBill(final String email, final BillDTO billDTO) {
        return null;
    }
}
