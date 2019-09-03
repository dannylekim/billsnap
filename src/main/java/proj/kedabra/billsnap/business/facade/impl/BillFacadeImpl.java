package proj.kedabra.billsnap.business.facade.impl;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import proj.kedabra.billsnap.business.dto.BillCompleteDTO;
import proj.kedabra.billsnap.business.dto.BillDTO;
import proj.kedabra.billsnap.business.entities.Account;
import proj.kedabra.billsnap.business.entities.Bill;
import proj.kedabra.billsnap.business.entities.Item;
import proj.kedabra.billsnap.business.facade.BillFacade;
import proj.kedabra.billsnap.business.mapper.BillMapper;
import proj.kedabra.billsnap.business.repository.AccountRepository;
import proj.kedabra.billsnap.business.service.BillService;

@Service
public class BillFacadeImpl implements BillFacade {

    private final AccountRepository accountRepository;

    private final BillService billService;

    private final BillMapper billMapper;

    private static final BigDecimal PERCENTAGE_DIVISOR = BigDecimal.valueOf(100);


    @Autowired
    public BillFacadeImpl(final AccountRepository accountRepository, final BillService billService, final BillMapper billMapper) {
        this.accountRepository = accountRepository;
        this.billService = billService;
        this.billMapper = billMapper;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillCompleteDTO addPersonalBill(final String email, final BillDTO billDTO) {

        //done in bill facade
        if ((billDTO.getTipAmount() == null) == (billDTO.getTipPercent() == null)) {
            throw new IllegalArgumentException("Only one type of tipping is supported. " +
                    "Please make sure only either tip amount or tip percent is set.");
        }

        final Account account = Optional.ofNullable(accountRepository.getAccountByEmail(email))
                .orElseThrow(() -> new ResourceNotFoundException("Account does not exist"));

        final Bill bill = billService.createBillToAccount(billDTO, account);

        final BillCompleteDTO billCompleteDTO = billMapper.toDTO(bill);
        final BigDecimal balance = calculateBalance(bill);
        billCompleteDTO.setBalance(balance);

        return billCompleteDTO;
    }

    //TODO decide on what rounding we want to use
    @SuppressWarnings("BigDecimalMethodWithoutRoundingCalled")
    private BigDecimal calculateBalance(final Bill bill) {
        final BigDecimal subTotal = bill.getItems().stream().map(Item::getCost).reduce(BigDecimal.ZERO, BigDecimal::add);



        final BigDecimal tipAmount = Optional.ofNullable(bill.getTipAmount()).orElse(BigDecimal.ZERO);

        final BigDecimal tipPercentAmount = Optional.ofNullable(bill.getTipPercent())
                .map(tipPercent -> tipPercent.divide(PERCENTAGE_DIVISOR))
                .map(subTotal::multiply)
                .orElse(BigDecimal.ZERO);

        return subTotal.add(tipAmount).add(tipPercentAmount);

    }
}
