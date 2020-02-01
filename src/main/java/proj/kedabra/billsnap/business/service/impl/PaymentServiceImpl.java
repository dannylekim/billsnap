package proj.kedabra.billsnap.business.service.impl;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.model.entities.AccountBill;
import proj.kedabra.billsnap.business.model.entities.Bill;
import proj.kedabra.billsnap.business.repository.PaymentRepository;
import proj.kedabra.billsnap.business.service.PaymentService;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;
import proj.kedabra.billsnap.business.utils.enums.PaymentStatusEnum;
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

        final var accountBill = account.getAccountBill(bill)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessageEnum.ACCOUNT_IS_NOT_ASSOCIATED_TO_BILL.getMessage()));

        verifyPaymentEligibility(bill, accountBill);

        final var totalAmountOwedToBill = paymentRepository.getTotalAmountOwedToBill(account, bill);
        final var amountPaid = Optional.ofNullable(accountBill.getAmountPaid()).orElse(BigDecimal.ZERO);
        final var newAmountPaid = amountPaid.add(paymentAmount);
        final var remainingBalance = totalAmountOwedToBill.subtract(newAmountPaid);

        if (remainingBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(ErrorMessageEnum.CANNOT_PAY_MORE_THAN_OWED.getMessage());
        }

        accountBill.setAmountPaid(newAmountPaid);

        if (remainingBalance.compareTo(BigDecimal.ZERO) == 0) {
            accountBill.setPaymentStatus(PaymentStatusEnum.PAID);
        }

        //if everything is paid for by everyone, but the responsible then resolve the bill
        if (bill.getAccounts().stream().allMatch(ab -> PaymentStatusEnum.PAID.equals(ab.getPaymentStatus()) || ab.getAccount().equals(bill.getResponsible()))) {
            bill.setStatus(BillStatusEnum.RESOLVED);
        }

        return remainingBalance;
    }

    private void verifyPaymentEligibility(Bill bill, AccountBill accountBill) {
        if (BillStatusEnum.RESOLVED.equals(bill.getStatus())) {
            throw new IllegalStateException(ErrorMessageEnum.BILL_ALREADY_RESOLVED.getMessage());
        }

        if (PaymentStatusEnum.PAID.equals(accountBill.getPaymentStatus())) {
            throw new IllegalStateException(ErrorMessageEnum.BILL_ALREADY_PAID_FOR.getMessage());
        }
    }

}
