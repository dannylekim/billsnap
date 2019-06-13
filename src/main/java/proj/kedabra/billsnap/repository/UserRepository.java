package proj.kedabra.billsnap.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import proj.kedabra.billsnap.entities.Account;

@Repository
public interface UserRepository extends CrudRepository<Account, Long> {

    Account getById(Long id);
}
