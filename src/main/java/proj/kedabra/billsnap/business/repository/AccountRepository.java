package proj.kedabra.billsnap.business.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import proj.kedabra.billsnap.business.entities.Account;

@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {

    boolean existsAccountByEmail(String email);

    Account getAccountByEmail(String email);
}
