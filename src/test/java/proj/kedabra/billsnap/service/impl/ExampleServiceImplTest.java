package proj.kedabra.billsnap.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import proj.kedabra.billsnap.entities.Example;
import proj.kedabra.billsnap.service.ExampleService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test")
@Tag("integration")
class ExampleServiceImplTest {

    private final ExampleService exampleService;

    @Autowired
    ExampleServiceImplTest(ExampleService exampleService) {
        this.exampleService = exampleService;
    }

    @Test
    @DisplayName("Should throw exception if null id")
    void shouldThrowExceptionIfNullId() {
        assertThrows(NullPointerException.class, () -> exampleService.getExample(null));
    }


    @Test
    @DisplayName("Should get instance of example")
    void shouldGetInstanceOfExample() {

        Example example = exampleService.getExample(1L);

        assertEquals("Aliko", example.getFirstName());
        assertEquals("Dangote", example.getLastName());
        assertEquals("Billionaire Industrialist", example.getCareer());
        assertEquals(Long.valueOf(1), example.getId());
    }

}