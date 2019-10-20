package proj.kedabra.billsnap.business.repository;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import proj.kedabra.billsnap.business.model.entities.Account;

@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {

    boolean existsAccountByEmail(String email);

    Account getAccountByEmail(String email);

    Stream<Account> getAccountsByEmailIn(List<String> emails);

}
