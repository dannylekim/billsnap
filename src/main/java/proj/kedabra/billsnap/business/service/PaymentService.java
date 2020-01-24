package proj.kedabra.billsnap.business.service;

import java.math.BigDecimal;

import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.model.entities.Bill;

public interface PaymentService {

    BigDecimal payBill(Account account, Bill bill, BigDecimal paymentAmount);

}
