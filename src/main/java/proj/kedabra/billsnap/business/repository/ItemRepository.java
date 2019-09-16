package proj.kedabra.billsnap.business.repository;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.repository.CrudRepository;

import proj.kedabra.billsnap.business.entities.Item;

public interface ItemRepository extends CrudRepository<Item, Long> {
    Stream<Item> getItemsByIdIn(List<Long> itemIds);
}
