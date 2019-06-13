package proj.kedabra.billsnap.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import proj.kedabra.billsnap.entities.Example;

@Repository
public interface ExampleRepository extends CrudRepository<Example, Long> {

    Example getById(Long id);

}
