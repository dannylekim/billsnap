package proj.kedabra.billsnap.business.repository;

import java.util.stream.Stream;

import org.springframework.data.repository.CrudRepository;

import proj.kedabra.billsnap.model.entities.Account;
import proj.kedabra.billsnap.model.entities.AccountBill;
import proj.kedabra.billsnap.model.entities.AccountBillId;

public interface AccountBillRepository extends CrudRepository<AccountBill, AccountBillId> {

    Stream<AccountBill> getAllByAccount(Account account);
}
