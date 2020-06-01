package proj.kedabra.billsnap.business.repository;

import org.springframework.data.repository.CrudRepository;

import proj.kedabra.billsnap.business.model.entities.AccountItem;
import proj.kedabra.billsnap.business.model.entities.AccountItemId;

public interface AccountItemRepository extends CrudRepository<AccountItem, AccountItemId> {
}
