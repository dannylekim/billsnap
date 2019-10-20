package proj.kedabra.billsnap.business.facade.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import proj.kedabra.billsnap.business.dto.PaymentOwedDTO;
import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.facade.PaymentFacade;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.business.service.impl.BillServiceImpl;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentFacadeImpl implements PaymentFacade {

    private final AccountRepository accountRepository;

    private final BillServiceImpl billService;

    private static final String ACCOUNT_DOES_NOT_EXIST = "Account does not exist";

    @Autowired
    public PaymentFacadeImpl(final AccountRepository accountRepository, final BillServiceImpl billService) {
        this.accountRepository = accountRepository;
        this.billService = billService;
    }

    @Override
    public List<PaymentOwedDTO> getAmountsOwed(String email) {
        final Account account = Optional.ofNullable(accountRepository.getAccountByEmail(email))
                .orElseThrow(() -> new ResourceNotFoundException(ACCOUNT_DOES_NOT_EXIST));

        return billService.calculateAmountOwed(account);
    }
}
