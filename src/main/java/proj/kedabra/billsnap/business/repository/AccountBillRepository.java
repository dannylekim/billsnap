package proj.kedabra.billsnap.business.repository;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.repository.CrudRepository;

import proj.kedabra.billsnap.business.model.entities.Account;
import proj.kedabra.billsnap.business.model.entities.AccountBill;
import proj.kedabra.billsnap.business.model.entities.AccountBillId;
import proj.kedabra.billsnap.business.model.entities.Bill;

public interface AccountBillRepository extends CrudRepository<AccountBill, AccountBillId> {

    Stream<AccountBill> getAllByAccount(Account account);

    Optional<AccountBill> getAccountBillByBillAndAccount(Bill bill, Account account);

}
