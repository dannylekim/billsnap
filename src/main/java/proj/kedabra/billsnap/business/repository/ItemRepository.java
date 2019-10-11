package proj.kedabra.billsnap.business.repository;

import org.springframework.data.repository.CrudRepository;

import proj.kedabra.billsnap.business.model.entities.Item;

public interface ItemRepository extends CrudRepository<Item, Long> {
}
