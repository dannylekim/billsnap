package proj.kedabra.billsnap.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import proj.kedabra.billsnap.entities.Example;
import proj.kedabra.billsnap.service.ExampleService;

@RestController
@RequestMapping("/example")
public class ExampleController {

    private final ExampleService exampleService;

    @Autowired
    public ExampleController(final ExampleService exampleService) {
        this.exampleService = exampleService;
    }

    //TODO the presentation layer should never just return an entity, additionally should generally call a facade rather than a service
    @GetMapping("/{id}")
    public Example getExample(@PathVariable final Long id) {
        return exampleService.getExample(id);
    }

}
