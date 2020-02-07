package proj.kedabra.billsnap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;

import proj.kedabra.billsnap.business.exception.AccessForbiddenException;
import proj.kedabra.billsnap.business.exception.FieldValidationException;
import proj.kedabra.billsnap.fixtures.FieldErrorFixture;
import proj.kedabra.billsnap.presentation.ApiError;
import proj.kedabra.billsnap.presentation.ApiSubError;

class BillSnapExceptionHandlerTest {

    private BillSnapExceptionHandler billSnapExceptionHandler = new BillSnapExceptionHandler();

    private static final String NOT_THIS_ERROR_MESSAGE = "NOT THIS ERROR MESSAGE";


    @Test
    @DisplayName("Should return ResponseEntity with FieldValidation")
    void shouldReturnFieldValidationResponseEntity() {
        //Given
        var fieldError = FieldErrorFixture.getDefault(0);
        var ex = new FieldValidationException(List.of(fieldError));

        //When
        ApiError response = billSnapExceptionHandler.handleFieldValidation(ex);

        //Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals(ex.getMessage(), response.getMessage());
        assertEquals(new ApiSubError(fieldError), response.getErrors().get(0));
    }

    @Test
    @DisplayName("Should return ResponseEntity with error 500 and unknown error message")
    void shouldReturnUnknownResponseEntity() {
        //Given
        var ex = new UnsupportedOperationException(NOT_THIS_ERROR_MESSAGE);

        //When
        ApiError response = billSnapExceptionHandler.handleUnknownException(ex);

        //Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals("Unknown error occurred. Please try again later", response.getMessage());
    }

    @Test
    @DisplayName("Should return ResponseEntity with error 400 and IllegalArgument's message")
    void shouldReturn400ForIllegalArgumentException() {
        //Given
        var ex = new IllegalArgumentException(NOT_THIS_ERROR_MESSAGE);

        //When
        ApiError response = billSnapExceptionHandler.handleIllegalArgument(ex);

        //Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals(NOT_THIS_ERROR_MESSAGE, response.getMessage());
    }

    @Test
    @DisplayName("Should return ResponseEntity with error 400 and IllegalState's message")
    void shouldReturn400ForIllegalStateException() {
        //Given
        var ex = new IllegalStateException(NOT_THIS_ERROR_MESSAGE);

        //When
        ApiError response = billSnapExceptionHandler.handleIllegalState(ex);

        //Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals(NOT_THIS_ERROR_MESSAGE, response.getMessage());
    }



    @Test
    @DisplayName("Should return ResponseEntity with error 400 and ResourceNotFound's message")
    void shouldReturn400ForResourceNotFoundException() {
        //Given
        var ex = new ResourceNotFoundException(NOT_THIS_ERROR_MESSAGE);

        //When
        ApiError response = billSnapExceptionHandler.handleResourceNotFound(ex);

        //Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals(NOT_THIS_ERROR_MESSAGE, response.getMessage());
    }

    @Test
    @DisplayName("Should return ResponseEntity with error 403 and AccessForbidden's message")
    void shouldReturn403ForAccessForbiddenException() {
        //Given
        var ex = new AccessForbiddenException(NOT_THIS_ERROR_MESSAGE);

        //When
        ApiError response = billSnapExceptionHandler.handleHttpAccessForbidden(ex);

        //Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatus());
        assertEquals(NOT_THIS_ERROR_MESSAGE, response.getMessage());
    }

}