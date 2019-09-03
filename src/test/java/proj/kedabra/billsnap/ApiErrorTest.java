package proj.kedabra.billsnap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import proj.kedabra.billsnap.fixtures.FieldErrorFixture;
import proj.kedabra.billsnap.presentation.ApiError;
import proj.kedabra.billsnap.presentation.ApiSubError;


class ApiErrorTest {


    private static final String TESTMESSAGE = "TESTMESSAGE";
    @Test
    @DisplayName("Should have all fields mapped")
    void shouldHaveAllFieldsMapped() {

        //Given
        List<ObjectError> errors = IntStream.range(0, 5)
                .filter(n -> n % 2 == 0)
                .mapToObj(FieldErrorFixture::getDefault)
                .collect(Collectors.toList());

        //When
        var apiError = new ApiError(HttpStatus.I_AM_A_TEAPOT, TESTMESSAGE, errors);

        //Then
        assertEquals(HttpStatus.I_AM_A_TEAPOT, apiError.getStatus());
        assertEquals(TESTMESSAGE, apiError.getMessage());
        assertEquals(3, apiError.getErrors().size());
        assertEquals(new ApiSubError(FieldErrorFixture.getDefault(0)), apiError.getErrors().get(0));
        assertEquals(new ApiSubError(FieldErrorFixture.getDefault(2)), apiError.getErrors().get(1));
        assertEquals(new ApiSubError(FieldErrorFixture.getDefault(4)), apiError.getErrors().get(2));


    }

    @Test
    @DisplayName("Should only have number of ApiSubErrors equivalent to number of FieldErrors")
    void shouldHaveOnlyFieldErrors() {
        //Given
        var errors = List.of(mock(FieldError.class), mock(ObjectError.class));

        //When
        var apiError = new ApiError(HttpStatus.I_AM_A_TEAPOT, TESTMESSAGE, errors);

        //Then
        assertEquals(1, apiError.getErrors().size());
    }


}