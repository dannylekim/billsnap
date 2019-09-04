package proj.kedabra.billsnap.business.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import proj.kedabra.billsnap.business.entities.Account;

@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {

    boolean existsAccountByEmail(String email);

    Account getAccountByEmail(String email);

    List<Account> getAccountsByEmailIn(List<String> emails);

}
