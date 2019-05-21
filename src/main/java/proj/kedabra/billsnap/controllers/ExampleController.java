package proj.kedabra.billsnap.controllers;

import org.springframework.web.bind.annotation.*;
import proj.kedabra.billsnap.entities.Example;
import proj.kedabra.billsnap.service.ExampleService;

@RestController
@RequestMapping("/example")
public class ExampleController {

    private final ExampleService exampleService;

    public ExampleController(final ExampleService exampleService){
        this.exampleService = exampleService;
    }

    //TODO the presentation layer should never just return an entity
    @GetMapping
    @RequestMapping("/{id}")
    public Example getExample(@PathVariable final Long id){
        return exampleService.getExample(id);
    }

}
