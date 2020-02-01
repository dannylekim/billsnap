package proj.kedabra.billsnap.business.facade.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.dto.PaymentInformationDTO;
import proj.kedabra.billsnap.business.dto.PaymentOwedDTO;
import proj.kedabra.billsnap.business.facade.PaymentFacade;
import proj.kedabra.billsnap.business.service.AccountService;
import proj.kedabra.billsnap.business.service.PaymentService;
import proj.kedabra.billsnap.business.service.impl.BillServiceImpl;

@Service
public class PaymentFacadeImpl implements PaymentFacade {

    private final AccountService accountService;

    private final BillServiceImpl billService;

    private final PaymentService paymentService;


    @Autowired
    public PaymentFacadeImpl(final AccountService accountService, final BillServiceImpl billService, final PaymentService paymentService) {
        this.accountService = accountService;
        this.billService = billService;
        this.paymentService = paymentService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentOwedDTO> getAmountsOwed(final String email) {
        final var account = accountService.getAccount(email);

        return billService.calculateAmountOwed(account);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal payBill(PaymentInformationDTO paymentInfo) {
        final var account = accountService.getAccount(paymentInfo.getEmail());
        final var bill = billService.getBill(paymentInfo.getBillId());
        return paymentService.payBill(account, bill, paymentInfo.getAmount());
    }

}
