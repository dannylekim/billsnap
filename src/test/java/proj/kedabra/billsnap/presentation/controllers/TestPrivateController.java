package proj.kedabra.billsnap.presentation.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestPrivateController {

    @GetMapping("/api-test/private-api-test-controller")
    public String getMessage() {
        return "Private API controller";
    }

}