package proj.kedabra.billsnap.business.service.impl;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.dto.AccountDTO;
import proj.kedabra.billsnap.business.dto.BillCompleteDTO;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.business.repository.PaymentRepository;
import proj.kedabra.billsnap.business.service.PaymentService;
import proj.kedabra.billsnap.utils.ErrorMessageEnum;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    private final AccountRepository accountRepository;

    public PaymentServiceImpl(final PaymentRepository paymentRepository, final AccountRepository accountRepository) {
        this.paymentRepository = paymentRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal payBill(final AccountDTO accountDTO, final BillCompleteDTO billDTO, final BigDecimal paymentAmount) {
        final var totalAmountOwedToBill = paymentRepository.getTotalAmountOwedToBill(accountDTO, billDTO);

        final var account = Optional.ofNullable(accountRepository.getAccountByEmail(accountDTO.getEmail()))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessageEnum.ACCOUNT_DOES_NOT_EXIST.getMessage()));

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
