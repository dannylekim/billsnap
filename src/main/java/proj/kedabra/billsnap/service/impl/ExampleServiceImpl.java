package proj.kedabra.billsnap.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import proj.kedabra.billsnap.entities.Example;
import proj.kedabra.billsnap.repository.ExampleRepository;
import proj.kedabra.billsnap.service.ExampleService;

import java.util.Objects;

@Service
public class ExampleServiceImpl implements ExampleService {

    private final ExampleRepository exampleRepository;

    @Autowired
    public ExampleServiceImpl(final ExampleRepository exampleRepository){
        this.exampleRepository = exampleRepository;
    }

    @Override
    public Example getExample(final Long id) {
        Objects.requireNonNull(id);
        /*TODO do note that there are layers of DTOs to pass through. For example's sake, we won't.
        However, the service layer picks up a domain object, then from there returns a DTO to the presentation layer */
        return exampleRepository.getById(id);
    }
}
