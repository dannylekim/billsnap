package proj.kedabra.billsnap.business.service;

import java.util.stream.Stream;

import proj.kedabra.billsnap.business.dto.BillDTO;
import proj.kedabra.billsnap.business.entities.Account;
import proj.kedabra.billsnap.business.entities.Bill;

public interface BillService {

    Bill createBillToAccount(BillDTO billDTO, Account account);

    Stream<Bill> getAllBillsByAccount(Account account);

}
