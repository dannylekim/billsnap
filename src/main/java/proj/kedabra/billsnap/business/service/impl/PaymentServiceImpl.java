package proj.kedabra.billsnap.business.service.impl;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.model.entities.Bill;
import proj.kedabra.billsnap.business.repository.PaymentRepository;
import proj.kedabra.billsnap.business.service.PaymentService;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentServiceImpl(final PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal payBill(final Account account, final Bill bill, final BigDecimal paymentAmount) {
        final var totalAmountOwedToBill = paymentRepository.getTotalAmountOwedToBill(account, bill);

        final var accountBill = account
                .getBills()
                .stream()
                .filter(ab -> ab.getAccount().equals(account))
                .findFirst()
                .orElseThrow();

        final var amountPaid = Optional.ofNullable(accountBill.getAmountPaid()).orElse(BigDecimal.ZERO);
        final var newAmountPaid = amountPaid.add(paymentAmount);
        final var remainingBalance = totalAmountOwedToBill.subtract(newAmountPaid);

        if (remainingBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(ErrorMessageEnum.CANNOT_PAY_MORE_THAN_OWED.getMessage());
        }

        accountBill.setAmountPaid(newAmountPaid);

        return remainingBalance;
    }

}
