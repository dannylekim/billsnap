package proj.kedabra.billsnap.business.repository;

import java.util.stream.Stream;

import org.springframework.data.repository.CrudRepository;

import proj.kedabra.billsnap.business.entities.Account;
import proj.kedabra.billsnap.business.entities.Bill;
import proj.kedabra.billsnap.business.utils.enums.BillStatusEnum;

public interface BillRepository extends CrudRepository<Bill, Long> {

    Stream<Bill> getBillsByStatusAndAccounts_AccBill_Account(BillStatusEnum status, Account account);

}
