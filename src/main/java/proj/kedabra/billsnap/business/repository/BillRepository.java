package proj.kedabra.billsnap.business.repository;

import org.springframework.data.repository.CrudRepository;

import proj.kedabra.billsnap.model.entities.Bill;

public interface BillRepository extends CrudRepository<Bill, Long> {



}
