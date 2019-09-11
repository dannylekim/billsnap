package proj.kedabra.billsnap.business.repository;

import java.util.stream.Stream;

import org.springframework.data.repository.CrudRepository;

import proj.kedabra.billsnap.business.entities.Account;
import proj.kedabra.billsnap.business.entities.AccountBill;
import proj.kedabra.billsnap.business.entities.AccountBillId;

public interface AccountBillRepository extends CrudRepository<AccountBill, AccountBillId> {

    Stream<AccountBill> getAllByAccount(Account account);
}
