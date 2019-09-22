package proj.kedabra.billsnap.business.service;

import java.util.List;
import java.util.stream.Stream;

import proj.kedabra.billsnap.business.dto.BillDTO;
import proj.kedabra.billsnap.business.entities.Account;
import proj.kedabra.billsnap.business.entities.Bill;
import proj.kedabra.billsnap.business.entities.IPaymentOwed;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;

public interface BillService {

    Bill createBillToAccount(BillDTO billDTO, Account account, List<Account> accountList);

    Stream<Bill> getAllBillsByAccount(Account account);

    Stream<IPaymentOwed> getAllAmountOwedByStatusAndAccount(BillStatusEnum status, Account account);

}
