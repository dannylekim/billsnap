package proj.kedabra.billsnap.repository;

import org.springframework.data.repository.CrudRepository;
import proj.kedabra.billsnap.entities.Example;

public interface ExampleRepository extends CrudRepository<Example, Long> {

    Example getById(Long id);

}
