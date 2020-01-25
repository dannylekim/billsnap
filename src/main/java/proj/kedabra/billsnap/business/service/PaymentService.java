package proj.kedabra.billsnap.business.service;

import java.math.BigDecimal;

import proj.kedabra.billsnap.business.dto.AccountDTO;
import proj.kedabra.billsnap.business.dto.BillCompleteDTO;

public interface PaymentService {

    BigDecimal payBill(AccountDTO account, BillCompleteDTO bill, BigDecimal paymentAmount);

}
