package proj.kedabra.billsnap.business.facade.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import proj.kedabra.billsnap.business.dto.PaymentOwedDTO;
import proj.kedabra.billsnap.business.facade.PaymentFacade;
import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.business.service.impl.BillServiceImpl;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;

@Service
public class PaymentFacadeImpl implements PaymentFacade {

    private final AccountRepository accountRepository;

    private final BillServiceImpl billService;


    @Autowired
    public PaymentFacadeImpl(final AccountRepository accountRepository, final BillServiceImpl billService) {
        this.accountRepository = accountRepository;
        this.billService = billService;
    }

    @Override
    public List<PaymentOwedDTO> getAmountsOwed(String email) {
        final Account account = Optional.ofNullable(accountRepository.getAccountByEmail(email))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessageEnum.ACCOUNT_DOES_NOT_EXIST.getMessage()));

        return billService.calculateAmountOwed(account);
    }
}
