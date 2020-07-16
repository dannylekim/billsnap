package proj.kedabra.billsnap.business.repository;

import org.springframework.data.repository.CrudRepository;

import proj.kedabra.billsnap.business.model.entities.AccountBill;
import proj.kedabra.billsnap.business.model.entities.AccountBillId;

public interface AccountBillRepository extends CrudRepository<AccountBill, AccountBillId> {

}
