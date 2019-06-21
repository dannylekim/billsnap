package proj.kedabra.billsnap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import proj.kedabra.billsnap.exception.FieldValidationException;

class BillSnapExceptionHandlerTest {

    private BillSnapExceptionHandler billSnapExceptionHandler = new BillSnapExceptionHandler();

    @Test
    @DisplayName("Should return ResponseEntity with FieldValidation")
    void shouldReturnFieldValidationResponseEntity() {
        //Given
        var fieldError = FieldErrorFixture.getDefault(0);
        var ex = new FieldValidationException(List.of(fieldError));

        //When
        ResponseEntity<ApiError> response = billSnapExceptionHandler.handleFieldValidation(ex);

        //Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ex.getMessage(), Objects.requireNonNull(response.getBody()).getMessage());
        assertEquals(new ApiSubError(fieldError), response.getBody().getErrors().get(0));
    }

    @Test
    @DisplayName("Should return ResponseEntity with error 500 and unknown error message")
    void shouldReturnUnknownResponseEntity() {
        //Given
        var ex = new UnsupportedOperationException("NOT THIS ERROR MESSAGE");

        //When
        ResponseEntity<ApiError> response = billSnapExceptionHandler.handleUnknownException(ex);

        //Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unknown error occurred. Please try again later", Objects.requireNonNull(response.getBody()).getMessage());
    }

}